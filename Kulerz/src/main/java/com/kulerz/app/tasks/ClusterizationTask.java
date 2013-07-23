package com.kulerz.app.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.kulerz.app.algorithms.KMeansAlgorithm;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Asynchronous clusterization task
 * @author a.s.mironov
 */
public class ClusterizationTask extends AsyncTask<ClusterizationTask.ClusterizationTaskSettings, Void, ClusterizationTask.ClusterizationResult>
{

    /**
     * Clusterization settings
     */
    public static class ClusterizationTaskSettings
    {

        public Bitmap bitmap;
        public int clustersCount;
        public int minimumDifference;

        public ClusterizationTaskSettings(Bitmap bitmap, int clustersCount, int minimumDifference) {
            this.bitmap = bitmap;
            this.clustersCount = clustersCount;
            this.minimumDifference = minimumDifference;
        }

    }

    /**
     * Clusterization result
     */
    public static class ClusterizationResult implements Serializable
    {

        public ArrayList<KMeansAlgorithm.Cluster> clusters;
        public ArrayList<int[]> randomColorPoints;

        public ClusterizationResult(ArrayList<KMeansAlgorithm.Cluster> clusters, ArrayList<int[]> randomColorPoints) {
            this.clusters = clusters;
            this.randomColorPoints = randomColorPoints;
        }

    }

    /**
     * Clusterization listener interface
     */
    public static interface ClusterizationTaskListener
    {
        public void onBeforeClusterizationStart();
        public void onAfterClusterizationComplete(ClusterizationResult result);
    }

    private ClusterizationTaskListener listener;
    private Random random = new Random();

    public ClusterizationTask() {}
    public ClusterizationTask(ClusterizationTaskListener listener) {
        setListener(listener);
    }

    public void setListener(ClusterizationTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(listener != null) {
            listener.onBeforeClusterizationStart();
        }
    }

    @Override
    protected ClusterizationResult doInBackground(ClusterizationTaskSettings... settingsArray) {
        ClusterizationTaskSettings settings = settingsArray[0];
        KMeansAlgorithm kMeans = new KMeansAlgorithm(settings.bitmap, settings.clustersCount, settings.minimumDifference);
        ArrayList<KMeansAlgorithm.Cluster> clusters = kMeans.getClusters();
        Collections.sort(clusters);
        ArrayList<int[]> randomColorPoints = new ArrayList<int[]>(clusters.size());
        for(KMeansAlgorithm.Cluster cluster : clusters) {
            randomColorPoints.add(cluster.getRandomColor(random));
            cluster.clearPoints();
        }
        return new ClusterizationResult(clusters, randomColorPoints);
    }

    @Override
    protected void onPostExecute(ClusterizationResult result) {
        super.onPostExecute(result);
        if(listener != null) {
            listener.onAfterClusterizationComplete(result);
        }
    }

}