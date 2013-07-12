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
import com.kulerz.app.views.PinchImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int WORKING_SIZE = 100;
    private static final int COLOR_CIRCLE_SIZE = 15;
    private static final int VISUAL_PADDING = 20;
    private static final String CLUSTERS_KEY = "clusters";
    private static final String WORKING_BITMAP_KEY = "workingBitmap";
    private static final String RANDOM_COLOR_POINTS_KEY = "randomColorPoints";

    private int layoutWidth;
    private int layoutHeight;
    private Bitmap visualBitmap;
    private Bitmap workingBitmap;
    private Bitmap outerBitmap;
    private Random random = new Random();
    private ArrayList<KMeansAlgorithm.Cluster> clusters;
    private ArrayList<int[]> randomColorPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState != null) {
            clusters = (ArrayList<KMeansAlgorithm.Cluster>)savedInstanceState.getSerializable(CLUSTERS_KEY);
            randomColorPoints = (ArrayList<int[]>)savedInstanceState.getSerializable(RANDOM_COLOR_POINTS_KEY);
            workingBitmap = savedInstanceState.getParcelable(WORKING_BITMAP_KEY);
        }
        findViewById(R.id.imageLayout).getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        outerBitmap.recycle();
        System.gc();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CLUSTERS_KEY, clusters);
        outState.putParcelable(WORKING_BITMAP_KEY, workingBitmap);
        outState.putSerializable(RANDOM_COLOR_POINTS_KEY, randomColorPoints);
    }

    @Override
    public void onGlobalLayout() {
        setupLayoutDimensions();
        if(workingBitmap == null) {
            setupWorkingBitmap();
        }
        if(clusters == null) {
            KMeansAlgorithm kMeans = new KMeansAlgorithm(workingBitmap, 5, 1);
            clusters = kMeans.getClusters();
            Collections.sort(clusters);
            randomColorPoints = new ArrayList<int[]>(clusters.size());
            for(KMeansAlgorithm.Cluster cluster : clusters) {
                randomColorPoints.add(cluster.getRandomColor(random));
                cluster.clearPoints();
            }
        }
        setupVisualBitmap();
        renderClusterizationResults();
        visualBitmap.recycle();
    }

    private void setupLayoutDimensions() {
        View imageLayout = findViewById(R.id.imageLayout);
        layoutWidth = imageLayout.getWidth();
        layoutHeight = imageLayout.getHeight();
    }

    private void setupWorkingBitmap() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.image2);
            workingBitmap = createWorkingBitmap(inputStream);
            inputStream.close();
        } catch (IOException e) {
            Log.e(MainActivity.class.toString(), "Fail: " + e.getMessage());
        }
    }

    private void setupVisualBitmap() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.image2);
            visualBitmap = createVisualBitmap(inputStream);
            inputStream.close();
        } catch (IOException e) {
            Log.e(MainActivity.class.toString(), "Fail: " + e.getMessage());
        }
    }

    private void renderClusterizationResults() {
        int dpPadding = SystemHelper.convertDpToPixel(VISUAL_PADDING, this);
        int dpShadowRadius = SystemHelper.convertDpToPixel(10.0f, this);
        int outerWidth = visualBitmap.getWidth();
        int outerHeight = visualBitmap.getHeight();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(dpPadding, dpPadding, outerWidth - dpPadding, outerHeight - dpPadding);
        outerBitmap = Bitmap.createBitmap(outerWidth, outerHeight, Bitmap.Config.ARGB_8888);
        Canvas outerCanvas = new Canvas(outerBitmap);
        paint.setShadowLayer(dpShadowRadius, 0, 0, Color.argb(200, 0, 0, 0));
        outerCanvas.drawRect(rect, paint);
        paint.clearShadowLayer();
        outerCanvas.drawBitmap(visualBitmap, null, rect, null);
        float scaleRatio = BitmapHelper.getScaleRatio(workingBitmap.getWidth(), workingBitmap.getHeight(), rect.width(), rect.height());
        for(int i = 0, j = 1; i < clusters.size(); i++, j++) {
            KMeansAlgorithm.Cluster c = clusters.get(i);
            int rgbColor = Color.rgb(c.center[0], c.center[1], c.center[2]);
            int[] randomClusterColor = randomColorPoints.get(i);
            int x = (int)(randomClusterColor[3] * scaleRatio) + dpPadding;
            int y = (int)(randomClusterColor[4] * scaleRatio) + dpPadding;
            drawColorCircle(outerCanvas, x, y, rgbColor, paint);
            int viewId = getResources().getIdentifier("color_" + String.valueOf(j), "id", getPackageName());
            findViewById(viewId).setBackgroundColor(rgbColor);
        }
        PinchImageView myImage = (PinchImageView) findViewById(R.id.imageView);
        myImage.setImageBitmap(outerBitmap);
    }

    private void drawColorCircle(Canvas canvas, int x, int y, int color, Paint paint) {
        int dpShadowRadius = SystemHelper.convertDpToPixel(2.5f, this);
        int dpShadowDy = SystemHelper.convertDpToPixel(4.0f, this);
        int dpPixels = SystemHelper.convertDpToPixel(COLOR_CIRCLE_SIZE, this);
        int strokePixel = SystemHelper.convertDpToPixel(2, this);

        paint.setShadowLayer(dpShadowRadius, 0, dpShadowDy, Color.argb(130, 0, 0, 0));
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
        int dpWidth = SystemHelper.convertDpToPixel(layoutWidth, this);
        int dpHeight = SystemHelper.convertDpToPixel(layoutHeight, this);
        Bitmap bitmap = BitmapHelper.resample(imageStream, dpWidth, dpHeight, true);
        Bitmap scaledBitmap = BitmapHelper.getScaledBitmap(bitmap, dpWidth, dpHeight);
        bitmap.recycle();
        return scaledBitmap;
    }

}
