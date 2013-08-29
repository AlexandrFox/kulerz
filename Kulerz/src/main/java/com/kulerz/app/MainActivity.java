package com.kulerz.app;

import android.app.ProgressDialog;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.fragments.ColorsFragment;
import com.kulerz.app.fragments.ImageFragment;
import com.kulerz.app.helpers.BitmapHelper;
import com.kulerz.app.helpers.SystemHelper;
import com.kulerz.app.tasks.ClusterizationTask;
import com.kulerz.app.views.PinchImageView;
import java.io.IOException;

public class MainActivity extends KulerzActivity
        implements ViewTreeObserver.OnGlobalLayoutListener, ClusterizationTask.ClusterizationTaskListener {

    public static String IMAGE_URI = "imageUri";

    private static final int WORKING_SIZE = 200;
    private static final int CLUSTERS_COUNT = 5;
    private static final int MINIMUM_DIFFERENCE = 1;
    private static final String CLUSTERIZATION_RESULT_KEY = "clusterizationResult";
    private static final String WORKING_BITMAP_KEY = "workingBitmap";
    private static final String IMAGE_URI_KEY = "imageUri";

    private Bitmap workingBitmap;
    private Uri imageUri;
    private ClusterizationTask.ClusterizationResult clusterizationResult;
    private View layout;
    private ProgressDialog dialog;
    private ClusterizationTask clusterizationTask;
    private ColorsFragment colorsFragment;
    private ImageFragment imageFragment;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        colorsFragment = (ColorsFragment)getFragmentManager().findFragmentById(R.id.colorsFragment);
        imageFragment = (ImageFragment)getFragmentManager().findFragmentById(R.id.imageFragment);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        initialize(savedInstanceState);
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
            workingBitmap = BitmapHelper.createBitmap(this, imageUri, WORKING_SIZE, WORKING_SIZE, false);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(WORKING_BITMAP_KEY, workingBitmap);
        outState.putParcelable(IMAGE_URI_KEY, imageUri);
        outState.putSerializable(CLUSTERIZATION_RESULT_KEY, clusterizationResult);
    }

    @Override
    public void onGlobalLayout() {
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
        imageFragment.setLayoutParams(layout.getWidth(), layout.getHeight());
        imageFragment.setWorkingBitmapParams(workingBitmap.getWidth(), workingBitmap.getHeight());
        imageFragment.setImageUri(imageUri);
        imageFragment.renderClusterizationResult(clusterizationResult);
        colorsFragment.renderClusterizationResult(clusterizationResult);
        if(dialog != null) {
            dialog.dismiss();
        }
    }

}
