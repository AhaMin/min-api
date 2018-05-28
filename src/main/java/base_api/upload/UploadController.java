package base_api.upload;

import base_core.constants.helper.FileUploadHelper;
import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * created by ewang on 2018/5/28.
 */
@Controller
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private FileUploadHelper fileUploadHelper;

    @RequestMapping("/image")
    public ResponseWrapper updateAvatar(HttpServletRequest httpServletRequest) {
        MultipartFile file;
        long imageId = 0l;
        if (httpServletRequest instanceof MultipartRequest) {
            file = ((MultipartRequest) httpServletRequest).getFile("file");
            try {
                imageId = fileUploadHelper.upload(file);
            } catch (Exception e) {
                return new ResponseWrapper(ResponseStatus.UploadFail, "上传失败");
            }
        }

        return new ResponseWrapper().addObject("imageId", imageId);
    }
}
