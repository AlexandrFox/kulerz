package com.kulerz.app.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kulerz.app.KulerzActivity;
import com.kulerz.app.KulerzFragment;
import com.kulerz.app.R;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.tasks.ClusterizationTask;

public class ColorsFragment extends KulerzFragment
{

    private KulerzActivity activity;
    private View layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_colors, container, false);
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (KulerzActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    public void renderClusterizationResult(ClusterizationTask.ClusterizationResult result) {
        for(int i = 0, j = 1; i < result.clusters.size(); i++, j++) {
            KMeansAlgorithm.Cluster c = result.clusters.get(i);
            int clusterColor = Color.rgb(c.center[0], c.center[1], c.center[2]);
            int viewId = getResources().getIdentifier("color_" + String.valueOf(j), "id", activity.getPackageName());
            layout.findViewById(viewId).setBackgroundColor(clusterColor);
        }
        layout.setVisibility(View.VISIBLE);
    }

//    private void fadeOutColorsLayout() {
//        if(colorsLayout.getAlpha() < 1.0f) {
//            return;
//        }
//        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.5f);
//        animation.setInterpolator(new AccelerateInterpolator());
//        animation.setDuration(200);
//        animation.setFillAfter(true);
//        colorsLayout.startAnimation(animation);
//    }
//
//    private void fadeInColorsLayout() {
//        if(colorsLayout.getAlpha() == 1.0f) {
//            return;
//        }
//        AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
//        animation.setInterpolator(new AccelerateInterpolator());
//        animation.setDuration(200);
//        animation.setFillAfter(true);
//        colorsLayout.startAnimation(animation);
//    }

}
