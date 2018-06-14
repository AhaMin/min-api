package base_api.design;

import base_core.design.constants.DesignSide;
import base_core.design.constants.DesignSize;
import base_core.design.dao.DesignDAO;
import base_core.design.dao.DesignOrderDAO;
import base_core.design.dao.DesignPreviewDAO;
import base_core.design.model.Design;
import base_core.design.model.DesignOrder;
import base_core.design.model.DesignPreview;
import base_core.design.service.DesignViewService;
import base_core.design.view.DesignOrderView;
import base_core.design.view.DesignPreviewView;
import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import base_core.user.dao.UserAddressDAO;
import base_core.user.dao.UserDAO;
import base_core.user.model.User;
import base_core.user.model.UserAddress;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * created by ewang on 2018/6/12.
 */
@Controller
@RequestMapping("/design")
public class DesignController {

    @Autowired
    private UserDAO userDAO;

    private DesignDAO designDAO;

    private DesignPreviewDAO designPreviewDAO;

    private DesignOrderDAO orderDAO;

    private UserAddressDAO userAddressDAO;

    @Autowired
    private DesignViewService designViewService;

    @RequestMapping("/create")
    public ResponseWrapper createDesign(@RequestParam("ownerId") long ownerId) {
        User owner = userDAO.getById(ownerId);
        if (owner == null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }
        long designId = designDAO.insert(ownerId);
        return new ResponseWrapper().addObject("designId", designId);
    }

    @RequestMapping("/preview/save")
    public ResponseWrapper saveDesignPreview(@RequestParam("designId") long designId,
                                             @RequestParam("previewImageId") long previewImageId,
                                             @RequestParam("detailImageId") long detailImageId,
                                             @RequestParam("designSide") int designSideValue) {

        long designPreviewId = designPreviewDAO.insert(designId, previewImageId, detailImageId,
                DesignSide.fromValue(designSideValue));
        DesignPreview designPreview = designPreviewDAO.getById(designPreviewId);
        List<DesignPreviewView> designPreviewViewList = designViewService.buildDesignPreviewView(Collections.singletonList(designPreview));
        return new ResponseWrapper().addObject("designPreview", designPreviewViewList.get(0));
    }

    @RequestMapping("/order/create")
    public ResponseWrapper createOrder(@RequestParam("designId") long designId,
                                       @RequestParam("userId") long userId,
                                       @RequestParam("addressId") long addressId,
                                       @RequestParam("price") double price,
                                       @RequestParam("designSize") int sizeValues) {
        User user = userDAO.getById(userId);
        if (user == null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }
        Design design = designDAO.getById(designId);
        if (design == null) {
            return new ResponseWrapper(ResponseStatus.NotFound, "当前订单的作品无效");
        }
        UserAddress userAddress = userAddressDAO.getById(addressId);
        if (userAddress == null || userAddress.getUserId() != userId) {
            return new ResponseWrapper(ResponseStatus.NotFound, "当前订单地址不合法");
        }
        long orderId = orderDAO.insert(designId, userId, addressId, price, DesignSize.fromValue(sizeValues));
        DesignOrder designOrder = orderDAO.getById(orderId);
        List<DesignOrderView> designOrderViewList = designViewService.buildDesignOrderView(Collections.singletonList(designOrder));
        return new ResponseWrapper().addObject("order", designOrderViewList.get(0));
    }

}
