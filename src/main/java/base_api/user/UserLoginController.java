package base_api.user;

import base_core.user.service.UserPasswordService;
import base_core.user.service.UserViewService;
import base_core.user.view.UserView;
import common.DataAttributeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import base_core.user.dao.UserDAO;
import base_core.user.model.User;

import java.util.Collections;
import java.util.List;


/**
 * created by ewang on 2018/3/20.
 */
@Controller
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private UserViewService userViewService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserPasswordService userPasswordService;

    @RequestMapping("/login")
    public ResponseWrapper login(@RequestParam(value = "account") String account,
                                 @RequestParam(value = "password") String password) {
        account = StringUtils.trimToNull(account);
        password = StringUtils.trimToNull(password);
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户名或密码为空");
        }
        User user = userDAO.getByAccount(account);
        if (user == null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }

        if (userPasswordService.verifyPassword(user.getId(), password)) {
            List<UserView> userViewList = userViewService.buildUserView(Collections.singletonList(user));
            return new ResponseWrapper().addObject("user", userViewList.get(0));
        } else {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "密码错误");
        }
    }

    @RequestMapping("/reg")
    public ResponseWrapper reg(@RequestParam(value = "account") String account,
                               @RequestParam(value = "password") String password) {
        account = StringUtils.trimToNull(account);
        password = StringUtils.trimToNull(password);
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户名或密码为空");
        }
        User user = userDAO.getByAccount(account);
        if (user != null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户名已被注册");
        }

        long userId = userDAO.insert(account, "{}");
        userPasswordService.createUserPassword(userId, password);
        user = userDAO.getById(userId);
        List<UserView> userViewList = userViewService.buildUserView(Collections.singletonList(user));
        return new ResponseWrapper().addObject("user", userViewList.get(0));

    }

    @RequestMapping("/update/profile")
    public ResponseWrapper updateUsername(@RequestParam("userId") long userId,
                                          @RequestParam("username") String username,
                                          @RequestParam("imageId") long imageId) {
        username = StringUtils.trimToNull(username);
        if (StringUtils.isBlank(username)) {
            return new ResponseWrapper(ResponseStatus.RequestParamValidationFail, "用户名为空");
        }
        User user = userDAO.getById(userId);
        if (user == null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户不存在");
        }
        DataAttributeBuilder builder = new DataAttributeBuilder(user.getData())
                .add(User.KEY_USERNAME, username)
                .add(User.KEY_AVATAR, imageId);
        userDAO.updateData(userId, builder.buildString(), user.getData());
        user = userDAO.getById(userId);
        List<UserView> userViewList = userViewService.buildUserView(Collections.singletonList(user));
        return new ResponseWrapper().addObject("user", userViewList.get(0));
    }
}
