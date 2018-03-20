package base_www;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import response.ResponseStatus;
import response.ResponseWrapper;
import user.dao.UserDAO;
import user.dao.UserPasswordDAO;
import user.model.User;
import user.model.UserPassword;


/**
 * created by ewang on 2018/3/20.
 */
@Controller
@RequestMapping("/user")
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
                return new ResponseWrapper().addObject("user", user);
            }
        }
        return ResponseWrapper.EMPTY;
    }

    private String encodePwd(String pwd, long updateTime) {
        return DigestUtils.md5Hex(pwd + updateTime);
    }
}
