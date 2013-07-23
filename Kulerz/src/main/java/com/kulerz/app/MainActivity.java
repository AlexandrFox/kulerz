package com.kulerz.app;

import android.app.ProgressDialog;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.helpers.BitmapHelper;
import com.kulerz.app.helpers.SystemHelper;
import com.kulerz.app.tasks.ClusterizationTask;
import com.kulerz.app.views.PinchImageView;
import java.io.IOException;

public class MainActivity extends KulerzActivity
        implements ViewTreeObserver.OnGlobalLayoutListener, ClusterizationTask.ClusterizationTaskListener {

    public static String IMAGE_URI = "imageUri";

    private static final int WORKING_SIZE = 200;
    private static final int COLOR_CIRCLE_SIZE = 10;
    private static final int COLOR_CIRCLE_STROKE_SIZE = 2;
    private static final int VISUAL_PADDING = 15;
    private static final int CLUSTERS_COUNT = 5;
    private static final int MINIMUM_DIFFERENCE = 1;
    private static final String CLUSTERIZATION_RESULT_KEY = "clusterizationResult";
    private static final String WORKING_BITMAP_KEY = "workingBitmap";
    private static final String IMAGE_URI_KEY = "imageUri";

    private int layoutWidth;
    private int layoutHeight;
    private Bitmap workingBitmap;
    private Bitmap outerBitmap;
    private Uri imageUri;
    private ClusterizationTask.ClusterizationResult clusterizationResult;
    private View layout;
    private ProgressDialog dialog;
    private ClusterizationTask clusterizationTask;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize(savedInstanceState);
        layout = findViewById(R.id.imageLayout);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    private void initialize(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            imageUri = getIntent().getParcelableExtra(IMAGE_URI);
        } else {
            clusterizationResult = (ClusterizationTask.ClusterizationResult)savedInstanceState.getSerializable(CLUSTERIZATION_RESULT_KEY);
            workingBitmap = savedInstanceState.getParcelable(WORKING_BITMAP_KEY);
            imageUri = savedInstanceState.getParcelable(IMAGE_URI_KEY);
        }
        if(workingBitmap == null) {
            workingBitmap = createBitmap(WORKING_SIZE, WORKING_SIZE, false);
        }
        clusterizationTask = (ClusterizationTask)getLastNonConfigurationInstance();
        if(clusterizationTask == null) {
            clusterizationTask = new ClusterizationTask();
        }
        clusterizationTask.setListener(this);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return clusterizationTask;
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
        outState.putParcelable(WORKING_BITMAP_KEY, workingBitmap);
        outState.putParcelable(IMAGE_URI_KEY, imageUri);
        outState.putSerializable(CLUSTERIZATION_RESULT_KEY, clusterizationResult);
    }

    @Override
    public void onGlobalLayout() {
        setupLayoutDimensions();
        if(clusterizationTask.getStatus() == AsyncTask.Status.PENDING) {
            ClusterizationTask.ClusterizationTaskSettings settings =
                    new ClusterizationTask.ClusterizationTaskSettings(workingBitmap, CLUSTERS_COUNT, MINIMUM_DIFFERENCE);
            clusterizationTask.execute(settings);
        } else if (clusterizationTask.getStatus() == AsyncTask.Status.FINISHED) {
            onAfterClusterizationComplete(clusterizationResult);
        } else {
            onBeforeClusterizationStart();
        }
        layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @Override
    public void onBeforeClusterizationStart() {
        dialog = ProgressDialog.show(this, null, getString(R.string.crunching), true, false);
    }

    @Override
    public void onAfterClusterizationComplete(ClusterizationTask.ClusterizationResult result) {
        clusterizationResult = result;
        renderClusterizationResult();
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    private void setupLayoutDimensions() {
        View imageLayout = findViewById(R.id.imageLayout);
        layoutWidth = imageLayout.getWidth();
        layoutHeight = imageLayout.getHeight();
    }

    private void renderClusterizationResult() {
        Bitmap visualBitmap = createBitmap(layoutWidth, layoutHeight, true);
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
        for(int i = 0, j = 1; i < clusterizationResult.clusters.size(); i++, j++) {
            KMeansAlgorithm.Cluster c = clusterizationResult.clusters.get(i);
            int clusterColor = Color.rgb(c.center[0], c.center[1], c.center[2]);
            int[] clusterPoint = clusterizationResult.randomColorPoints.get(i);
            int x = (int)(clusterPoint[3] * scaleRatio) + dpPadding;
            int y = (int)(clusterPoint[4] * scaleRatio) + dpPadding;
            drawColorCircle(outerCanvas, x, y, clusterColor, paint);
            int viewId = getResources().getIdentifier("color_" + String.valueOf(j), "id", getPackageName());
            findViewById(viewId).setBackgroundColor(clusterColor);
        }
        PinchImageView myImage = (PinchImageView) findViewById(R.id.imageView);
        myImage.setImageBitmap(outerBitmap);
        visualBitmap.recycle();
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

    private Bitmap createBitmap(int width, int height, boolean immutable) {
        try {
            Bitmap bitmap = BitmapHelper.resample(this, imageUri, width, height, immutable);
            Bitmap result = BitmapHelper.getScaledBitmap(bitmap, width, height);
            bitmap.recycle();
            return result;
        } catch (IOException e) {
            Log.e(Kulerz.TAG, e.toString());
            return null;
        }
    }

}
