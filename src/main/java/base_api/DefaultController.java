package base_api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * created by ewang on 2018/3/22.
 */
@Controller
@RequestMapping("/")
public class DefaultController {

    @RequestMapping
    public void index(HttpServletResponse response) throws IOException {
        response.getWriter().print("To be an artist!");
    }
}
