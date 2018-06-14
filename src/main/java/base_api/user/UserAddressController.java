package base_api.user;

import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import base_core.user.dao.UserAddressDAO;
import base_core.user.dao.UserDAO;
import base_core.user.model.User;
import base_core.user.model.UserAddress;
import base_core.user.service.UserViewService;
import base_core.user.view.UserAddressView;
import common.DataAttributeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * created by ewang on 2018/6/13.
 */
@Controller
@RequestMapping("user/address")
public class UserAddressController {

    private UserAddressDAO userAddressDAO;

    @Autowired
    private UserViewService userViewService;

    @Autowired
    private UserDAO userDAO;

    @RequestMapping("/find")
    public ResponseWrapper findByUser(@RequestParam("userId") long userId) {
        User user = userDAO.getById(userId);
        if (user == null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }
        List<UserAddress> userAddressList = userAddressDAO.findByUser(userId);
        List<UserAddressView> userAddressViewList = userViewService.buildAddressView(userAddressList);
        return new ResponseWrapper().addObject("addressList", userAddressViewList);
    }

    @RequestMapping("/save")
    public ResponseWrapper saveAddress(@RequestParam("userId") long userId,
                                       @RequestParam("province") String province,
                                       @RequestParam("city") String city,
                                       @RequestParam("county") String county,
                                       @RequestParam("street") String street,
                                       @RequestParam("receiverName") String receiverName,
                                       @RequestParam("receiverPhone") String receiverPhone) {
        StringUtils.trimToNull(province);
        StringUtils.trimToNull(city);
        StringUtils.trimToNull(county);
        StringUtils.trimToNull(street);
        StringUtils.trimToNull(receiverName);
        StringUtils.trimToNull(receiverPhone);
        if (province == null || city == null || county == null || street == null ||
                receiverName == null || receiverPhone == null) {
            return new ResponseWrapper(ResponseStatus.RequestParamValidationFail, "地址参数不合法");
        }
        User user = userDAO.getById(userId);
        if (user == null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }
        if (userAddressDAO.findByUser(userId).size() > 5) {
            return new ResponseWrapper(ResponseStatus.ThresholdHit, "所存地址超过上限");
        }
        DataAttributeBuilder builder = new DataAttributeBuilder()
                .add(UserAddress.KEY_PROVINCE, province)
                .add(UserAddress.KEY_CITY, city)
                .add(UserAddress.KEY_COUNTY, county)
                .add(UserAddress.KEY_STREET, street)
                .add(UserAddress.KEY_RECEIVER_NAME, receiverName)
                .add(UserAddress.KEY_RECEIVER_PHONE, receiverPhone);
        long addressId = userAddressDAO.insert(userId, builder.buildString());
        UserAddress userAddress = userAddressDAO.getById(addressId);
        List<UserAddressView> userAddressViewList = userViewService.buildAddressView(Collections.singletonList(userAddress));
        return new ResponseWrapper().addObject("address", userAddressViewList.get(0));
    }
}
