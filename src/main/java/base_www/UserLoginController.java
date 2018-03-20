package base_www;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import base_core.user.dao.UserDAO;
import base_core.user.dao.UserPasswordDAO;
import base_core.user.model.User;
import base_core.user.model.UserPassword;


/**
 * created by ewang on 2018/3/20.
 */
@Controller
@RequestMapping("/base_core/user")
public class UserLoginController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserPasswordDAO userPasswordDAO;

    @RequestMapping("/login")
    public ResponseWrapper login(@RequestParam("account") String account,
                                 @RequestParam("password") String password) {
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户名或密码为空");
        }
        User user = userDAO.getByAccount(account);
        UserPassword userPassword;
        if (user != null) {
            userPassword = userPasswordDAO.getByUser(user.getId());
            if (userPassword.getPassword().equals(encodePwd(password, userPassword.getUpdateTime()))) {
                return new ResponseWrapper().addObject("base_core/user", user);
            }
        }
        return ResponseWrapper.EMPTY;
    }

    @RequestMapping("/reg")
    public ResponseWrapper reg(@RequestParam("account") String account,
                               @RequestParam("password") String password) {
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户名或密码为空");
        }
        User user = userDAO.getByAccount(account);
        if (user != null) {
            return new ResponseWrapper(ResponseStatus.UserIllegal, "用户名已被注册");
        }
        long pwdUpdateTimeMills = System.currentTimeMillis();
        long userId = userDAO.insert(account, "{}");
        userPasswordDAO.insertOrUpdate(userId, encodePwd(password, pwdUpdateTimeMills), pwdUpdateTimeMills);
        user = userDAO.getById(userId);
        return new ResponseWrapper().addObject("base_core/user", user);

    }

    private String encodePwd(String pwd, long updateTime) {
        return DigestUtils.md5Hex(pwd + updateTime);
    }
}
