package base_api.user;

import base_core.user.service.UserPasswordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import base_core.user.dao.UserDAO;
import base_core.user.model.User;


/**
 * created by ewang on 2018/3/20.
 */
@Controller
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserPasswordService userPasswordService;

    @RequestMapping("/login")
    public ResponseWrapper login(@RequestParam(value = "account", required = false) String account,
                                 @RequestParam(value = "password", required = false) String password) {
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
            return new ResponseWrapper().addObject("user", user);
        } else {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "密码错误");
        }
    }

    @RequestMapping("/reg")
    public ResponseWrapper reg(@RequestParam(value = "account", required = false) String account,
                               @RequestParam(value = "password", required = false) String password) {
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
        return new ResponseWrapper().addObject("user", user);

    }
}
