package com.kulerz.app;

import android.graphics.*;
import android.os.Bundle;
import android.app.Activity;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.helpers.BitmapHelper;
import com.kulerz.app.helpers.SystemHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int WORKING_SIZE = 100;
    private static final int COLOR_CIRCLE_SIZE = 10;
    private static final int VISUAL_PADDING = 15;

    private int layoutWidth;
    private int layoutHeight;
    private Bitmap visualBitmap;
    private Bitmap workingBitmap;
    private Bitmap outerBitmap;
    private Random random = new Random();
    private ArrayList<KMeansAlgorithm.Cluster> clusters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.imageLayout).getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        visualBitmap.recycle();
        workingBitmap.recycle();
        outerBitmap.recycle();
        visualBitmap = null;
        workingBitmap = null;
        outerBitmap = null;
        clusters.clear();
        System.gc();
    }

    @Override
    public void onGlobalLayout() {
        setupLayoutDimensions();
        setupBitmaps();
        KMeansAlgorithm kMeans = new KMeansAlgorithm(workingBitmap, 5, 1);
        clusters = kMeans.getClusters();
        Collections.sort(clusters);
        renderClusterizationResults();
    }

    private void setupBitmaps() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.image2);
            workingBitmap = createWorkingBitmap(inputStream);
            visualBitmap = createVisualBitmap(inputStream);
            inputStream.close();
        } catch (IOException e) {
            Log.e(MainActivity.class.toString(), "Fail: " + e.getMessage());
        }
    }

    //@TODO correct the position color marker (not to overflow window and each other) or use image margins and nice shadow
    private void renderClusterizationResults() {
        int dpPadding = SystemHelper.convertDpToPixel(VISUAL_PADDING, this);
        int outerWidth = visualBitmap.getWidth();
        int outerHeight = visualBitmap.getHeight();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(dpPadding, dpPadding, outerWidth - dpPadding, outerHeight - dpPadding);
        outerBitmap = Bitmap.createBitmap(outerWidth, outerHeight, Bitmap.Config.ARGB_8888);
        Canvas outerCanvas = new Canvas(outerBitmap);
        paint.setShadowLayer(3.5f, 0, 1.0f, Color.argb(180, 0, 0, 0));
        outerCanvas.drawRect(rect, paint);
        paint.clearShadowLayer();
        outerCanvas.drawBitmap(visualBitmap, null, rect, null);
        float scaleRatio = BitmapHelper.getScaleRatio(workingBitmap.getWidth(), workingBitmap.getHeight(), rect.width(), rect.height());
        int i = 0;
        for(KMeansAlgorithm.Cluster c : clusters) {
            int rgbColor = Color.rgb(c.center[0], c.center[1], c.center[2]);
            int[] randomClusterColor = c.getRandomColor(random);
            int x = (int)(randomClusterColor[3] * scaleRatio) + dpPadding;
            int y = (int)(randomClusterColor[4] * scaleRatio) + dpPadding;
            drawColorCircle(outerCanvas, x, y, rgbColor, paint);
            int viewId = getResources().getIdentifier("color_" + String.valueOf(++i), "id", getPackageName());
            findViewById(viewId).setBackgroundColor(rgbColor);
        }
        ImageView myImage = (ImageView) findViewById(R.id.imageView);
        myImage.setImageBitmap(outerBitmap);
    }

    private void drawColorCircle(Canvas canvas, int x, int y, int color, Paint paint) {
        int dpPixels = SystemHelper.convertDpToPixel(COLOR_CIRCLE_SIZE, this);
        int strokePixel = SystemHelper.convertDpToPixel(2, this);

        paint.setShadowLayer(2.5f, 0.0f, 4.0f, Color.argb(110, 0, 0, 0));
        canvas.drawCircle(x, y, dpPixels + strokePixel, paint);
        paint.clearShadowLayer();

        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, dpPixels + strokePixel, paint);
        paint.setColor(color);
        canvas.drawCircle(x, y, dpPixels, paint);
    }

    private Bitmap createWorkingBitmap(InputStream imageStream) {
        Bitmap bitmap = BitmapHelper.resample(imageStream, WORKING_SIZE, WORKING_SIZE, false);
        Bitmap scaledBitmap = BitmapHelper.getScaledBitmap(bitmap, WORKING_SIZE, WORKING_SIZE);
        bitmap.recycle();
        return scaledBitmap;
    }

    private Bitmap createVisualBitmap(InputStream imageStream) {
        logMemory("createVisualBitmap");
        int dpWidth = SystemHelper.convertDpToPixel(layoutWidth, this);
        int dpHeight = SystemHelper.convertDpToPixel(layoutHeight, this);
        Bitmap bitmap = BitmapHelper.resample(imageStream, dpWidth, dpHeight, true);
        Bitmap scaledBitmap = BitmapHelper.getScaledBitmap(bitmap, dpWidth, dpHeight);
        bitmap.recycle();
        return scaledBitmap;
    }

    private void setupLayoutDimensions() {
        View imageLayout = findViewById(R.id.imageLayout);
        layoutWidth = imageLayout.getWidth();
        layoutHeight = imageLayout.getHeight();
    }

    long availableJavaMemoryOld, availableNativeMemoryOld;

    private void logMemory(String callingFunction)
    {
        long max = Runtime.getRuntime().maxMemory() / 1024;
        long used = Runtime.getRuntime().totalMemory() / 1024;
        long available = max - used;
        long change = available - availableJavaMemoryOld;
        if (availableJavaMemoryOld != 0)
            Log.i("TEST_MEM", "jMEM M:" + max + ", U:" + used + ", A:" + available + ", C:" + change + ", " + callingFunction);
        availableJavaMemoryOld = available;


        used = Debug.getNativeHeapAllocatedSize() / 1024;
        available = max - used;
        change = available - availableNativeMemoryOld;
        if (availableNativeMemoryOld != 0)
            Log.i("TEST_MEM", "nMEM M:" + max + ", U:" + used + ", A:" + available + ", C:" + change + ", " + callingFunction);
        availableNativeMemoryOld = available;
    }

}
