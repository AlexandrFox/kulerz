package com.kulerz.app.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kulerz.app.KulerzActivity;
import com.kulerz.app.KulerzFragment;
import com.kulerz.app.R;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import com.kulerz.app.tasks.ClusterizationTask;
import com.kulerz.app.views.ColorView;

import java.util.ArrayList;

public class ColorsFragment extends KulerzFragment implements View.OnClickListener {

    private final static String SELECTED_COLOR_INDEX_KEY = "selectedColorIndex";

    private ArrayList<ColorView> colorsViews;
    private KulerzActivity activity;
    private View layout;
    private LinearLayout colorsLayout;
    private int selectedColorIndex = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colorsViews = new ArrayList<ColorView>();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_COLOR_INDEX_KEY, selectedColorIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_colors, container, false);
        colorsLayout = (LinearLayout)layout.findViewById(R.id.colorsLayout);
        if(savedInstanceState != null) {
            selectedColorIndex = savedInstanceState.getInt(SELECTED_COLOR_INDEX_KEY, -1);
        }
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
            ColorView colorView = getColorView(clusterColor);
            colorsViews.add(colorView);
            colorsLayout.addView(colorView);
        }
        if(selectedColorIndex > 0) {
            colorsViews.get(selectedColorIndex).setColorIsSelected(true);
        }
        layout.setVisibility(View.VISIBLE);
    }

    private ColorView getColorView(int color) {
        ColorView colorView = new ColorView(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT
        );
        layoutParams.weight = 1;
        colorView.setColor(color);
        colorView.setLayoutParams(layoutParams);
        colorView.setOnClickListener(this);
        return colorView;
    }

    @Override
    public void onClick(View view) {
        ColorView selectedView = (ColorView)view;
        for(int i = 0; i < colorsViews.size(); i++) {
            ColorView colorView = colorsViews.get(i);
            colorView.setColorIsSelected(false);
            if(colorView.getColor() == selectedView.getColor()) {
                selectedView.setColorIsSelected(true);
                selectedColorIndex = i;
            }
        }
    }

}
