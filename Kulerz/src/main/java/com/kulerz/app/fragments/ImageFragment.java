package com.kulerz.app.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kulerz.app.KulerzActivity;
import com.kulerz.app.KulerzFragment;
import com.kulerz.app.R;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.helpers.BitmapHelper;
import com.kulerz.app.helpers.SystemHelper;
import com.kulerz.app.tasks.ClusterizationTask;
import com.kulerz.app.views.PinchImageView;

public class ImageFragment extends KulerzFragment
{

    private static final float SHADOW_RADIUS = 5.0f;
    private static final int COLOR_CIRCLE_SIZE = 10;
    private static final int COLOR_CIRCLE_STROKE_SIZE = 2;
    private static final int VISUAL_PADDING = 15;

    private View layout;
    private KulerzActivity activity;
    private Bitmap outerBitmap;
    private Uri imageUri;
    private int layoutWidth;
    private int layoutHeight;
    private int workingBitmapWidth;
    private int workingBitmapHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_image, container, false);
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (KulerzActivity)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(outerBitmap != null) {
            outerBitmap.recycle();
            System.gc();
        }
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setLayoutParams(int lWidth, int lHeight) {
        layoutWidth = lWidth;
        layoutHeight = lHeight;
    }

    public void setWorkingBitmapParams(int bWidth, int bHeight) {
        workingBitmapWidth = bWidth;
        workingBitmapHeight = bHeight;
    }

    public void renderClusterizationResult(ClusterizationTask.ClusterizationResult result) {
        Bitmap visualBitmap = BitmapHelper.createBitmap(activity, imageUri, layoutWidth, layoutHeight, true);
        int dpPadding = SystemHelper.convertDpToPixel(VISUAL_PADDING, activity);
        int dpShadowRadius = SystemHelper.convertDpToPixel(SHADOW_RADIUS, activity);
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
        float scaleRatio = BitmapHelper.getScaleRatio(workingBitmapWidth, workingBitmapHeight, rect.width(), rect.height());
        for(int i = 0, j = 1; i < result.clusters.size(); i++, j++) {
            KMeansAlgorithm.Cluster c = result.clusters.get(i);
            int clusterColor = Color.rgb(c.center[0], c.center[1], c.center[2]);
            int[] clusterPoint = result.randomColorPoints.get(i);
            int x = (int)(clusterPoint[3] * scaleRatio) + dpPadding;
            int y = (int)(clusterPoint[4] * scaleRatio) + dpPadding;
            drawColorCircle(outerCanvas, x, y, clusterColor, paint);
        }
        ((PinchImageView)layout.findViewById(R.id.imageView)).setImageBitmap(outerBitmap);
        visualBitmap.recycle();
    }

    private void drawColorCircle(Canvas canvas, int x, int y, int color, Paint paint) {
        int dpShadowRadius = SystemHelper.convertDpToPixel(2.5f, activity);
        int dpShadowDy = SystemHelper.convertDpToPixel(4.0f, activity);
        int dpPixels = SystemHelper.convertDpToPixel(COLOR_CIRCLE_SIZE, activity);
        int strokePixel = SystemHelper.convertDpToPixel(COLOR_CIRCLE_STROKE_SIZE, activity);

        paint.setShadowLayer(dpShadowRadius, 0, dpShadowDy, Color.argb(130, 0, 0, 0));
        canvas.drawCircle(x, y, dpPixels + strokePixel, paint);
        paint.clearShadowLayer();
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, dpPixels + strokePixel, paint);
        paint.setColor(color);
        canvas.drawCircle(x, y, dpPixels, paint);
    }

}
