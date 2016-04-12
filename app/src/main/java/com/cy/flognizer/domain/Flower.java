package com.cy.flognizer.domain;

import android.graphics.Bitmap;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.Ml;

import java.io.Serializable;
import java.util.List;

/**
 * Created by CY on 15/11/13.
 */
public class Flower implements Serializable{

    // ID
    private int id;

    // Picture mat
    private Mat image = new Mat();

    // Bitmap
    private Bitmap bitmap;

    // Key points
    private MatOfKeyPoint keyPoint = new MatOfKeyPoint();

//    // characteristic
//    private double colorInfo;
//    private double perimeter;
//    private double rate;
//    private double shapeArea;

    // Name and details
    private String name;
    private String details;

    public Flower(Mat img, String name){
        this.image = img;
        this.name = name;

        // Get a FeatureDetector object
        FeatureDetector detector =
                FeatureDetector.create(FeatureDetector.ORB);

        // To detect the image
        detector.detect(image, keyPoint);

    }

    public Flower(Bitmap bitmap, String name){
        this.bitmap = bitmap;
        this.name = name;

        // Convert bitmap to mat
        Mat mat = new Mat(bitmap.getHeight(),
                        bitmap.getWidth(), CvType.CV_32F);
        Utils.bitmapToMat(bitmap, mat);

        this.image = mat;

        // Get a FeatureDetector object
        FeatureDetector detector =
                FeatureDetector.create(FeatureDetector.ORB);

        // To detect the image
        detector.detect(image, keyPoint);
    }

    public String keyPointToJSON(){

        MatOfKeyPoint matOfKeyPoint = this.keyPoint;

        if(matOfKeyPoint!=null && !matOfKeyPoint.empty()){

            JSONArray jsonArray = new JSONArray();

            KeyPoint[] keyPointArray = matOfKeyPoint.toArray();

            for(int i = 0; i < keyPointArray.length; i++){

                KeyPoint keyPoint = keyPointArray[i];

                JSONObject jo = new JSONObject();

                try {
                    jo.put("x", keyPoint.pt.x);
                    jo.put("y", keyPoint.pt.y);
                    jo.put("size", keyPoint.size);
                    jo.put("angle", keyPoint.angle);
                    jo.put("response", keyPoint.response);
                    jo.put("octave", keyPoint.octave);
                    jo.put("class_id", keyPoint.class_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArray.put(jo);
            }
            return jsonArray.toString();
        }else {
            Log.v("fuck", "is null");
            return "{}";
        }
    }

    public static MatOfKeyPoint JSONToKeyPoint(String json){

        MatOfKeyPoint result = new MatOfKeyPoint();

        JSONArray jsonArray = null;

        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int size = jsonArray.length();

        KeyPoint[] keyPointArray = new KeyPoint[size];

        try {
            for(int i = 0; i < size; i++){
                KeyPoint keyPoint = new KeyPoint();

                JSONObject jo = (JSONObject)jsonArray.get(i);

                Point point = new Point(jo.getDouble("x"), jo.getDouble("y"));

                keyPoint.pt = point;
                keyPoint.size = (float)jo.getDouble("size");
                keyPoint.angle = (float)jo.getDouble("angle");
                keyPoint.response = (float)jo.getDouble("response");
                keyPoint.octave = (int)jo.getDouble("octave");
                keyPoint.class_id = (int)jo.getDouble("class_id");

                keyPointArray[i] = keyPoint;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        result.fromArray(keyPointArray);

        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Mat getImg() {
        return image;
    }

    public void setImg(Mat img) {
        this.image = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public MatOfKeyPoint getKeyPoint() {
        return keyPoint;
    }

    public void setKeyPoint(MatOfKeyPoint matOfKeyPoint) {
        this.keyPoint = matOfKeyPoint;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "Flower{" + "keyPoint=" + keyPoint + '}';
    }
}
