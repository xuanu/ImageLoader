package utils.cn.zeffect.loaderlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/04/12
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 *         // TODO 用@see描述一下当前类的方法及简单解释
 */

public class ImageUtils {
    public static Bitmap getBitmap(File pFile) {
        if (pFile == null || !pFile.exists() || pFile.isDirectory()) {
            return null;
        }
        return BitmapFactory.decodeFile(pFile.getAbsolutePath());
    }
}
