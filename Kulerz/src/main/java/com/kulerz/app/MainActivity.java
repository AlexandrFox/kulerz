package com.kulerz.app;

import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.helpers.BitmapHelper;
import com.kulerz.app.helpers.SystemHelper;
import com.kulerz.app.views.PinchImageView;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends KulerzActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    public static String IMAGE_URI = "imageUri";

    private static final int WORKING_SIZE = 100;
    private static final int COLOR_CIRCLE_SIZE = 10;
    private static final int COLOR_CIRCLE_STROKE_SIZE = 2;
    private static final int VISUAL_PADDING = 15;
    private static final int CLUSTERS_COUNT = 5;
    private static final int MINIMUM_DIFFERENCE = 1;
    private static final String CLUSTERS_KEY = "clusters";
    private static final String WORKING_BITMAP_KEY = "workingBitmap";
    private static final String RANDOM_COLOR_POINTS_KEY = "randomColorPoints";
    private static final String IMAGE_URI_KEY = "imageUri";

    private int layoutWidth;
    private int layoutHeight;
    private Bitmap visualBitmap;
    private Bitmap workingBitmap;
    private Bitmap outerBitmap;
    private Uri imageUri;
    private Random random = new Random();
    private ArrayList<KMeansAlgorithm.Cluster> clusters;
    private ArrayList<int[]> randomColorPoints;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if(intent != null) {
            imageUri = intent.getParcelableExtra(IMAGE_URI);
        }
        if(savedInstanceState != null) {
            clusters = (ArrayList<KMeansAlgorithm.Cluster>)savedInstanceState.getSerializable(CLUSTERS_KEY);
            randomColorPoints = (ArrayList<int[]>)savedInstanceState.getSerializable(RANDOM_COLOR_POINTS_KEY);
            workingBitmap = savedInstanceState.getParcelable(WORKING_BITMAP_KEY);
            imageUri = savedInstanceState.getParcelable(IMAGE_URI_KEY);
        }
        findViewById(R.id.imageLayout).getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(outerBitmap != null) {
            outerBitmap.recycle();
            System.gc();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CLUSTERS_KEY, clusters);
        outState.putParcelable(WORKING_BITMAP_KEY, workingBitmap);
        outState.putSerializable(RANDOM_COLOR_POINTS_KEY, randomColorPoints);
        outState.putParcelable(IMAGE_URI_KEY, imageUri);
    }

    @Override
    public void onGlobalLayout() {
        try {
            setupLayoutDimensions();
            if(workingBitmap == null) {
                workingBitmap = createBitmap(WORKING_SIZE, WORKING_SIZE, false);
            }
            if(clusters == null || randomColorPoints == null) {
                performClusterAnalysis(workingBitmap);
            }
            visualBitmap = createBitmap(layoutWidth, layoutHeight, true);
            renderClusterizationResults();
            visualBitmap.recycle();
        } catch (IOException e) {
            Log.e(Kulerz.TAG, e.toString());
            finish();
        }
    }

    private void performClusterAnalysis(Bitmap bitmap) {
        KMeansAlgorithm kMeans = new KMeansAlgorithm(bitmap, CLUSTERS_COUNT, MINIMUM_DIFFERENCE);
        clusters = kMeans.getClusters();
        Collections.sort(clusters);
        randomColorPoints = new ArrayList<int[]>(clusters.size());
        for(KMeansAlgorithm.Cluster cluster : clusters) {
            randomColorPoints.add(cluster.getRandomColor(random));
            cluster.clearPoints();
        }
    }

    private void setupLayoutDimensions() {
        View imageLayout = findViewById(R.id.imageLayout);
        layoutWidth = imageLayout.getWidth();
        layoutHeight = imageLayout.getHeight();
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
            int clusterColor = Color.rgb(c.center[0], c.center[1], c.center[2]);
            int[] clusterPoint = randomColorPoints.get(i);
            int x = (int)(clusterPoint[3] * scaleRatio) + dpPadding;
            int y = (int)(clusterPoint[4] * scaleRatio) + dpPadding;
            drawColorCircle(outerCanvas, x, y, clusterColor, paint);
            int viewId = getResources().getIdentifier("color_" + String.valueOf(j), "id", getPackageName());
            findViewById(viewId).setBackgroundColor(clusterColor);
        }
        PinchImageView myImage = (PinchImageView) findViewById(R.id.imageView);
        myImage.setImageBitmap(outerBitmap);
    }

    private void drawColorCircle(Canvas canvas, int x, int y, int color, Paint paint) {
        int dpShadowRadius = SystemHelper.convertDpToPixel(2.5f, this);
        int dpShadowDy = SystemHelper.convertDpToPixel(4.0f, this);
        int dpPixels = SystemHelper.convertDpToPixel(COLOR_CIRCLE_SIZE, this);
        int strokePixel = SystemHelper.convertDpToPixel(COLOR_CIRCLE_STROKE_SIZE, this);

        paint.setShadowLayer(dpShadowRadius, 0, dpShadowDy, Color.argb(130, 0, 0, 0));
        canvas.drawCircle(x, y, dpPixels + strokePixel, paint);
        paint.clearShadowLayer();
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, dpPixels + strokePixel, paint);
        paint.setColor(color);
        canvas.drawCircle(x, y, dpPixels, paint);
    }

    private Bitmap createBitmap(int width, int height, boolean immutable) throws IOException {
        Bitmap bitmap = BitmapHelper.resample(this, imageUri, width, height, immutable);
        Bitmap result = BitmapHelper.getScaledBitmap(bitmap, width, height);
        bitmap.recycle();
        return result;
    }

}
