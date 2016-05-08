package com.cy.flognizer;


import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;




/**
 * Created by CY on 16/5/7.
 */
public class Tool {

    public static Mat kmeans(Mat mat){

//        Log.v("fuck", "length: "+ img.get(1,1).length);
//        Log.v("fuck", "get 0: "+ img.get(1,1)[0]);
//        Log.v("fuck", "get 1: "+ img.get(1,1)[1]);
//        Log.v("fuck", "get 2: "+ img.get(1,1)[2]);

//        Core.rectangle(img, new Point(0, 0), new Point(100, 200), new Scalar(0, 255, 0), -1);
//        Core.rectangle(img, new Point(100, 0), new Point(200, 200), new Scalar(0, 0, 255), -1);

        // clusters
        Mat cluster = cluster(mat, 4);
        Log.v("fuck", "cluster rows: "+ cluster.rows());
        Log.v("fuck", "cluster cols: "+ cluster.cols());
        Log.v("fuck", "0, 0 length: "+ cluster.get(0, 0).length);
//        Log.v("fuck", "0, 0 : "+ cluster.get(0, 0)[0]);
//        Log.v("fuck", "0, 0 : "+ cluster.get(1, 0)[0]);
//        Log.v("fuck", "0, 0 : "+ cluster.get(2, 0)[0]);
//        Log.v("fuck", "0, 0 : "+ cluster.get(3, 0)[0]);
        return cluster;
    }

    public static Mat cluster(Mat cutout, int k) {
        Mat samples = cutout.reshape(1, cutout.cols() * cutout.rows());
        Mat samples32f = new Mat();
        samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);

        Mat labels = new Mat();
        TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
        Mat centers = new Mat();
        Core.kmeans(samples32f, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);
        return centers;
    }

//    private static List<Mat> showClusters (Mat cutout, Mat labels, Mat centers) {
//        centers.convertTo(centers, CvType.CV_8UC1, 255.0);
//        centers.reshape(2);
//
//        List<Mat> clusters = new ArrayList<Mat>();
//        for(int i = 0; i < centers.rows(); i++) {
//            clusters.add(Mat.zeros(cutout.size(), cutout.type()));
//        }
//
//        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
//        for(int i = 0; i < centers.rows(); i++) counts.put(i, 0);
//
//        int rows = 0;
//        for(int y = 0; y < cutout.rows(); y++) {
//            for(int x = 0; x < cutout.cols(); x++) {
//                int label = (int)labels.get(rows, 0)[0];
//                int r = (int)centers.get(label, 2)[0];
//                int g = (int)centers.get(label, 1)[0];
//                int b = (int)centers.get(label, 0)[0];
//                counts.put(label, counts.get(label) + 1);
//                clusters.get(label).put(y, x, b, g, r);
//                rows++;
//            }
//        }
//        Log.v("fuck", "counts: " + counts);
//        return clusters;
//    }
}
