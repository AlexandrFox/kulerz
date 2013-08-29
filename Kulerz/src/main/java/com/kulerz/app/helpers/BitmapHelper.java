package com.kulerz.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.kulerz.app.Kulerz;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapHelper
{

    public static float getScaleRatio(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        float scaleX = (float)dstWidth / (float)srcWidth;
        float scaleY = (float)dstHeight / (float)srcHeight;
        return Math.min(scaleX, scaleY);
    }

    public static Bitmap createBitmap(Context context, Uri uri, int width, int height, boolean immutable) {
        try {
            Bitmap bitmap = BitmapHelper.resample(context, uri, width, height, immutable);
            Bitmap result = BitmapHelper.getScaledBitmap(bitmap, width, height);
            bitmap.recycle();
            return result;
        } catch (IOException e) {
            Log.e(Kulerz.TAG, e.toString());
            return null;
        }
    }

    public static Bitmap getScaledBitmap(Bitmap src, int width, int height) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        float ratio = getScaleRatio(srcWidth, srcHeight, width, height);
        int newWidth = Math.round(srcWidth * ratio);
        int newHeight = Math.round(srcHeight * ratio);
        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    public static Bitmap resample(Context context, Uri uri, int width, int height, boolean immutable) throws IOException {
        InputStream stream = getImageInputStream(uri, context);
        if(stream == null) {
            return null;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inMutable = immutable;
        stream.close();
        return BitmapFactory.decodeStream(getImageInputStream(uri, context), null, options);
    }

    private static InputStream getImageInputStream(Uri uri, Context context) {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.e(Kulerz.TAG, e.getMessage());
            return null;
        }
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
