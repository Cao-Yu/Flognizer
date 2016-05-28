package com.cy.flognizer;


import android.util.Log;

import com.cy.flognizer.model.Singleton;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by CY on 16/5/7.
 */
public class Tool {

    public static Mat grabCut(Mat src){

        // 首先，搞一个矩形框。外边默认是背景，里边的内容将会被执行grabcut。
        int w = src.cols() - 10;
        int h = src.rows() - 10;
        Rect rect = new Rect(5, 5, w, h);


        // 创建两个Mat：前景和背景。注意，前景不等于结果，只是一个内部中间量。
        Mat bg = new Mat();
        Mat fg = new Mat();

        // 创建一个用来装结果的Mat，并开始grabcut：
        Mat result = new Mat();
        Imgproc.grabCut(src, result, rect,
                bg, fg, 3, Imgproc.GC_INIT_WITH_RECT);
//        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Mat source =
                new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));
//        Core.compare(result, new Scalar(Imgproc.GC_PR_FGD),
//                result, Core.CMP_EQ);

        Core.compare(result, source, result, Core.CMP_EQ);

        //
        Mat foreground = new Mat(src.size(),
                CvType.CV_8UC3, new Scalar(0, 0, 0));

        src.copyTo(foreground, result);

        int x1 = foreground.rows() / 4;
        int y1 = foreground.cols() / 4;
        int x2 = x1 * 3;
        int y2 = y1 * 3;

//        Log.v("fuck", "size: " + foreground.size());
//        Log.v("fuck", "x1: " + x1);
//        Log.v("fuck", "y1: " + y1);
//        Log.v("fuck", "x2: " + x2);
//        Log.v("fuck", "y2: " + y2);

        foreground.submat(x1, x2, y1, y2).setTo(new Scalar(0,0,0));

//        Mat mmm = foreground.submat(x1 - 10, x2 + 10, y1 - 10, y2 + 10);
//        Mat mmm = foreground.submat(x1 - 20, x2, y1 - 20, y1);

        return foreground;
    }

    public static void getHSVColor(Mat img){

        // grubcut
        Mat m = Tool.grabCut(img);
        // convert hsv
        Imgproc.cvtColor(m,m,Imgproc.COLOR_RGB2HSV);
        Scalar scalar = Core.mean(m);

//        List<Mat> list = new ArrayList<Mat>();
//        Core.split(m, list);

        double h = scalar.val[0] * 2;
        double s = (scalar.val[1] / 255) * 100;
//        double v = (scalar.val[2] / 255) * 100;
//        Log.v("fuck", "H: " + h);
//        Log.v("fuck", "S: " + s);
//        Log.v("fuck", "V: " + v);

        Singleton.hList.add(h);
        Singleton.sList.add(s);
    }

    public static void colorSimple(Mat img){
        int w = img.rows() / 4;
        int h = img.cols() / 4;
        Mat m = img.submat(w, w * 3, h, h * 3);
        Scalar s = Core.mean(m);
        Singleton.rList.add(s.val[0]);
        Singleton.gList.add(s.val[1]);
        Singleton.bList.add(s.val[2]);
    }

    public static void colorClassification(Mat img){
        // grubcut
        Mat m = Tool.grabCut(img);

        List<Mat> list = new ArrayList<Mat>();
        Core.split(m, list);
        int count = 0;
        double r = 0;
        double g = 0;
        double b = 0;
//        long t1 = System.currentTimeMillis();
        for(int i = 0; i < m.rows(); i++){
            for(int j = 0; j < m.cols(); j++){
                if(m.get(i, j)[0] > 0) {
                    r += m.get(i, j)[0];
                    g += m.get(i, j)[1];
                    b += m.get(i, j)[2];
                    count += 1;
                }
            }
        }
//        long t2 = System.currentTimeMillis() - t1;
//        Log.v("fuck", "interate time: " + t2);

//        Scalar s = Core.mean(m);

        Singleton.rList.add(r / count);
        Singleton.gList.add(g / count);
        Singleton.bList.add(b / count);
    }

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
