package utils.cn.zeffect.loaderlibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static Bitmap loadImageFromLocal(final String path,
                                            final ImageView imageView) {
        Bitmap bm;
        // 加载图片
        // 图片的压缩
        // 1、获得图片需要显示的大小
        ImageSizeUtil.ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
        // 2、压缩图片
        bm = decodeSampledBitmapFromPath(path, imageSize.width,
                imageSize.height);
        return bm;
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    protected static Bitmap decodeSampledBitmapFromPath(String path, int width,
                                                        int height) {
        // 获得图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
                width, height);

        // 使用获得到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    public static Bitmap getBitmap(File pFile) {
        if (pFile == null || !pFile.exists() || pFile.isDirectory()) {
            return null;
        }
        return BitmapFactory.decodeFile(pFile.getAbsolutePath());
    }

    /**
     * Md5 32位 or 16位 加密（全部返回）
     *
     * @param plainText 待加密内容
     * @return 32位加密
     */
    public static String Md5PassWord(String plainText) {
        StringBuffer buf = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] b = md.digest();
            int i;
            buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            // Log.e("555","result: " + buf.toString());//32位的加密
            // Log.e("555","result: " + buf.toString().substring(8,24));//16位的加密
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (buf == null) {
            return null;
        }
        return buf.toString().toLowerCase();
    }
}
