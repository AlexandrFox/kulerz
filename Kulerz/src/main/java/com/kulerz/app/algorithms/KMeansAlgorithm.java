package com.kulerz.app.algorithms;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Specified realization of K-Means algorithm
 * to clusterize RGB-colors (3D) of a Bitmap image
 *
 * @author a.s.mironov
 */
public class KMeansAlgorithm
{

    private static final int MAX_LOOP_COUNT = 15;
    private static final int UNREACHABLE_DISTANCE = 100000000;
    private Bitmap bitmap;
    private int clustersCount;
    private int minDifference;
    private Random random = new Random();

    public static class Cluster implements Comparable, Serializable
    {

        public int[] center;
        public ArrayList<int[]> points;

        public Cluster() {
            points = new ArrayList<int[]>();
        }

        public Cluster(int[] center, ArrayList<int[]> points) {
            this.center = center;
            this.points = points;
        }

        public int[] getRandomColor(Random random) {
            if(points.size() == 0) {
                return null;
            }
            int idx = random.nextInt(points.size());
            return points.get(idx);
        }

        public void clearPoints() {
            points.clear();
        }

        @Override
        public int compareTo(Object o) {
            if(o == this) {
                return 0;
            }
            Cluster other = (Cluster)o;
            int size = this.points.size();
            int otherSize = other.points.size();
            if(size > otherSize) {
                return 1;
            } else if (size < otherSize) {
                return -1;
            }
            return 0;
        }

    }

    public KMeansAlgorithm(Bitmap bitmap, int clustersCount, int minDifference) {
        this.bitmap = bitmap;
        this.clustersCount = clustersCount;
        this.minDifference = minDifference;
    }

    public ArrayList<Cluster> getClusters() {
        int loopCount = 1;
        ArrayList<int[]> points = getPointsFromBitmap();
        ArrayList<Cluster> clusters = getRandomClusters(points);
        while (true) {
            ArrayList<ArrayList<int[]>> pointsLists = getNewPointsLists();
            mapPointsToLists(points, clusters, pointsLists);
            double difference = recalculateClustersCenters(clusters, pointsLists);
            if((difference < minDifference) || (loopCount >= MAX_LOOP_COUNT)) {
                break;
            }
            loopCount++;
        }
        points.clear();
        return clusters;
    }

    private ArrayList<int[]> getPointsFromBitmap() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        ArrayList<int[]> points = new ArrayList<int[]>(width * height);
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int color = bitmap.getPixel(x, y);
                points.add(new int[]{ (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, x, y });
            }
        }
        return points;
    }

    private ArrayList<Cluster> getRandomClusters(ArrayList<int[]> points) {
        int pointsCount = points.size();
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        ArrayList<Integer> seenIndexes = new ArrayList<Integer>();
        while (clusters.size() < clustersCount) {
            int idx = (int)(random.nextDouble() * pointsCount);
            int[] color = points.get(idx);
            if(!seenIndexes.contains(idx)) {
                ArrayList<int[]> clusterPoints = new ArrayList<int[]>();
                clusterPoints.add(color);
                Cluster newCluster = new Cluster(color, clusterPoints);
                clusters.add(newCluster);
                seenIndexes.add(idx);
            }
        }
        return clusters;
    }

    private ArrayList<ArrayList<int[]>> getNewPointsLists() {
        ArrayList<ArrayList<int[]>> plists = new ArrayList<ArrayList<int[]>>(clustersCount);
        for(int i = 0; i < clustersCount; i++) {
            plists.add(new ArrayList<int[]>());
        }
        return plists;
    }

    private void mapPointsToLists(ArrayList<int[]> points, ArrayList<Cluster> clusters, ArrayList<ArrayList<int[]>> lists) {
        for (int[] point : points) {
            double smallestDistance = UNREACHABLE_DISTANCE;
            int idx = 0;
            for (int i = 0; i < clustersCount; i++) {
                double distance = euclidean(point, clusters.get(i).center);
                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    idx = i;
                }
            }
            lists.get(idx).add(point);
        }
    }

    private double recalculateClustersCenters(ArrayList<Cluster> clusters, ArrayList<ArrayList<int[]>> lists) {
        double diff = 0;
        for (int i = 0; i < clustersCount; i++) {
            Cluster oldCluster = clusters.get(i);
            int[] center = calculateClusterCenter(lists.get(i), 3);
            Cluster newCluster = new Cluster(center, lists.get(i));
            double dist = euclidean(oldCluster.center, center);
            clusters.set(i, newCluster);
            diff = diff > dist ? diff : dist;
        }
        return diff;
    }

    private int[] calculateClusterCenter(ArrayList<int[]> points, int length) {
        int[] center = new int[length];
        int plen = 0;
        for (int[] point : points) {
            plen++;
            for (int j = 0; j < length; j++) {
                center[j] += point[j];
            }
        }
        for(int i = 0; i < length; i++) {
            center[i] = center[i] / plen;
        }
        return center;
    }

    private double euclidean(int[] p1, int[] p2) {
        return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]) + (p1[2] - p2[2]) * (p1[2] - p2[2]));
    }

}
