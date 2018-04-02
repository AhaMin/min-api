package base_api.file;

import base_core.constants.Constants;
import base_core.image.dao.ImageDAO;
import base_core.image.model.Image;
import base_core.response.ResponseStatus;
import base_core.response.ResponseWrapper;
import common.DataAttributeBuilder;
import magick.MagickException;
import magick.MagickImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * created by ewang on 2018/3/23.
 */
@Controller
@RequestMapping("/file/upload")
public class FileUploadController {

    @Autowired
    private ImageDAO imageDAO;

    @RequestMapping
    public ResponseWrapper image(HttpEntity<byte[]> httpEntity) throws IOException {
        byte[] image = httpEntity.getBody();
        if (image != null && image.length > 0) {
            long id = imageDAO.insert("{}");
            boolean success;
            MagickImage magickImage = null;
            Dimension dimension;
            String format;
            try {
                // 获取尺寸和格式信息
                magickImage = new MagickImage(new magick.ImageInfo(), image);
                dimension = magickImage.getDimension();
                format = magickImage.getImageFormat();
            } catch (Exception e) {
                return new ResponseWrapper(ResponseStatus.UploadFail, "上传失败");
            } finally {
                if (magickImage != null) {
                    magickImage.destroyImages();
                    magickImage = null;
                }
            }
            String imagePath = Constants.ImagePath.getValue() + id + "." + format;
            File file = new File(imagePath);
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os.write(image);
                success = true;
            } catch (Exception e) {
                return new ResponseWrapper(ResponseStatus.UploadFail, "上传失败");
            } finally {
                os.close();
            }
            if (success) {
                DataAttributeBuilder builder = new DataAttributeBuilder()
                        .add(Image.KEY_HEIGHT, dimension.height)
                        .add(Image.KEY_WIDTH, dimension.width)
                        .add(Image.KEY_PATH, imagePath)
                        .add(Image.KEY_TYPE, format);
                imageDAO.update(id, "{}", builder.buildString());
                Image finalImage = imageDAO.getById(id);
                return new ResponseWrapper().addObject("imagePath", finalImage.getPath());
            } else {
                imageDAO.delete(id);
            }
        }
        return new ResponseWrapper(ResponseStatus.RequestParamValidationFail, "没有选择文件");
    }
}
