package com.kulerz.app.helpers;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

public class BitmapHelper
{

    public static float getScaleRatio(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        float scaleX = (float)dstWidth / (float)srcWidth;
        float scaleY = (float)dstHeight / (float)srcHeight;
        return Math.min(scaleX, scaleY);
    }

    public static Bitmap getScaledBitmap(Bitmap src, int width, int height) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        float ratio = getScaleRatio(srcWidth, srcHeight, width, height);
        int newWidth = Math.round(srcWidth * ratio);
        int newHeight = Math.round(srcHeight * ratio);
        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    public static Bitmap resample(InputStream stream, int width, int height, boolean immutable) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inMutable = immutable;
        return BitmapFactory.decodeStream(stream, null, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
