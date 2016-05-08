package com.cy.flognizer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cy.flognizer.ProcessActivity;
import com.cy.flognizer.R;
import com.cy.flognizer.Tool;
import com.cy.flognizer.domain.Flower;

import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CY on 15/11/15.
 */
public class Singleton {

    // Path
    private static final String path = "/storage/sdcard/Pictures";
    private static final String FOLDER_NAME = "/dataset";

    private static final String DAISY = "/daisy";
    private static final String WIND = "/windflower";
    private static final String LILY  = "/arum lily";
    private static final String BUSH  = "/silverbush";

    private static final String DAFF = "/daffodil";
    private static final String SUN = "/sunflower";
    private static final String SUSAN = "/black-eyed susan";
    private static final String CUP = "/buttercup";

    private static final String ANTH = "/anthurium";
    private static final String BOI = "/bishop of llandaff";
    private static final String GERAN = "/geranium";

    private static final String OST = "/osteospermum";
    private static final String HIBI = "/hibiscus";
    private static final String PELAR = "/pelargonium";

    private static Singleton singleton = null;
    private Database database = null;
    private SQLiteDatabase sqldb = null;

    public List<Double> chns = new ArrayList<Double>();
    public double[][][] channels = new double[4][15][3];

    public static List<Double> hList = new ArrayList<Double>();
    public static List<Double> sList = new ArrayList<Double>();

//      this.database.getReadableDatabase();

//    // contains all reference
//    public static Flower[][] references = new Flower[4][5];
//
//    //contains all sample
//    public static Flower[][] samples = new Flower[4][5];

    private Singleton(Context context){
        this.database = new Database(context);
    }

    public static Singleton getSingleton(Context context){
        if(singleton == null){
            singleton = new Singleton(context);
        }
        return singleton;
    }

    private void getColor(Mat img) {
        int w = img.rows() / 4;
        int h = img.cols() / 4;
        Mat m = img.submat(w, w * 2, h, h * 2);

        Scalar chnn = Core.mean(m);
        chns.add(chnn.val[2]);
        chns.add(chnn.val[1]);
        chns.add(chnn.val[0]);
//        Log.v("fuck", "B: " + chnn.val[2]);//B
//        Log.v("fuck", "G: " + chnn.val[1]);//G
//        Log.v("fuck", "R: " + chnn.val[0]);//R

    }

    public void getHSVColor(Mat img){

        int wid = img.rows() / 4;
        int hei = img.cols() / 4;
        Mat cutMat = img.submat(wid, wid * 2, hei, hei * 2);
        // get hsv
        Mat m = new Mat();
        Imgproc.cvtColor(cutMat,m,Imgproc.COLOR_BGRA2RGB);
        Imgproc.cvtColor(m,m,Imgproc.COLOR_RGB2HSV);

        Scalar scalar = Core.mean(m);
        double h = (scalar.val[0] / 179) * 360;
        double s = (scalar.val[1] / 255) * 100;
//        double v = (scalar.val[2] / 255) * 100;
//        Log.v("fuck", "H: " + h);
//        Log.v("fuck", "S: " + s);
//        Log.v("fuck", "V: " + v);

        hList.add(h);
        sList.add(s);
    }

    private void registLoop(String name, String[] ids, String folder){

        sqldb = database.getWritableDatabase();

        ContentValues cv = new ContentValues();

        String realPath = path + FOLDER_NAME +
                folder + "/image_";

        String extension = ".jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for(int i = 0; i < 5; i++) {

            //To convert the bitmap to binary strings
            Bitmap bitmap =
                    BitmapFactory.decodeFile(
                            realPath + ids[i] + extension);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            Mat m = new Mat();
            Utils.bitmapToMat(bitmap, m);
            getColor(m);
            getHSVColor(m);

            byte[] binaryBitmap = baos.toByteArray();

            cv.put("bitmap", binaryBitmap);

            try {
                baos.flush();
                // clean
                baos.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sqldb.insert(name, null, cv);
        }
//        Log.v("fuck", "-------------------------------");
        sqldb.close();
    }

    private void recgLoop(String name, String[] ids, String folder){

        sqldb = database.getReadableDatabase();


        String realPath = path + FOLDER_NAME +
                folder + "/image_";

        String extension = ".jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for(int i = 0; i < 5; i++) {

            //To convert the bitmap to binary strings
            Bitmap bitmap =
                    BitmapFactory.decodeFile(
                            realPath + ids[i] + extension);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] binaryBitmap = baos.toByteArray();

            try {
                baos.flush();
                // clean
                baos.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        sqldb.close();
    }

    public boolean initDataset(){
        if(ProcessActivity.count != 0){
            return false;
        }
        ProcessActivity.count++;

        String[] daisy = {"0806", "0813", "0814", "0831", "0832"};
        String[] wind  = {"1204", "1206", "1213", "1214", "1217"};
        String[] bush  = {"06098", "06101", "06102", "06108", "06111"};

        String[] daff  = {"0005", "0006", "0014", "0021", "0024"};
        String[] sun   = {"0721", "0723", "0728", "0731", "0737"};
        String[] susan   = {"05849", "05850", "05853", "05861", "05873"};

        String[] anth   = {"01973", "01978", "01979", "01980", "01981"};
        String[] boi   = {"02759", "02763", "02776", "02781", "02799"};
        String[] geran   = {"02640", "02643", "02649", "02651", "02652"};

        String[] ost   = {"05525", "05529", "05536", "05543", "05561"};
        String[] hibi   = {"02872", "02881", "02889", "02890", "02893"};
        String[] pelar   = {"04700", "04714", "04718", "04720", "04721"};

        registLoop(database.TABLE_NAME_DAISY, daisy, DAISY);
        registLoop(database.TABLE_NAME_WIND, wind, WIND);
        registLoop(database.TABLE_NAME_BUSH, bush, BUSH);

        registLoop(database.TABLE_NAME_DAFF, daff, DAFF);
        registLoop(database.TABLE_NAME_SUN, sun, SUN);
        registLoop(database.TABLE_NAME_SUSAN, susan, SUSAN);

        registLoop(database.TABLE_NAME_ANTH, anth, ANTH);
        registLoop(database.TABLE_NAME_BOI, boi, BOI);
        registLoop(database.TABLE_NAME_GERAN, geran, GERAN);

        registLoop(database.TABLE_NAME_OST, ost, OST);
        registLoop(database.TABLE_NAME_HIBI, hibi, HIBI);
        registLoop(database.TABLE_NAME_PELAR, pelar, PELAR);

//        Log.v("fuck", "lenth: " + chns.size());

        double minB = 500;
        double minG = 500;
        double minR = 500;
        double maxB = 0;
        double maxG = 0;
        double maxR = 0;
        // white
        for(int i = 0; i < 15; i++){
            channels[0][i][0] = chns.get(i * 3);
            channels[0][i][1] = chns.get(i * 3 + 1);
            channels[0][i][2] = chns.get(i * 3 + 2);
            minB = minB < channels[0][i][0] ?
                    minB : channels[0][i][0];
            minG = minG < channels[0][i][1] ?
                    minG : channels[0][i][1];
            minR = minR < channels[0][i][2] ?
                    minR : channels[0][i][2];
        }
//        Log.v("fuck", "minB: " + minB);
//        Log.v("fuck", "minG: " + minG);
//        Log.v("fuck", "minR: " + minR);
        ProcessActivity.thresholds[0][0] = minB;
        ProcessActivity.thresholds[0][1] = minG;
        ProcessActivity.thresholds[0][2] = minR;
        minB = 500;
        minG = 500;
        minR = 500;
        maxB = 0;
        maxG = 0;
        maxR = 0;

        // yellow
        for(int i = 0; i < 15; i++){
            channels[1][i][0] = chns.get(i * 3 + 45);
            channels[1][i][1] = chns.get(i * 3 + 1 + 45);
            channels[1][i][2] = chns.get(i * 3 + 2 + 45);
            maxB = maxB > channels[1][i][0] ?
                    maxB : channels[1][i][0];
            minG = minG < channels[1][i][1] ?
                    minG : channels[1][i][1];
        }
//        Log.v("fuck", "maxB: " + maxB);
//        Log.v("fuck", "minG: " + minG);
        ProcessActivity.thresholds[1][0] = minB;
        ProcessActivity.thresholds[1][1] = minG;
        minB = 500;
        minG = 500;
        minR = 500;
        maxB = 0;
        maxG = 0;
        maxR = 0;

        // red
        for(int i = 0; i < 15; i++){
            channels[2][i][0] = chns.get(i * 3 + 90);
            channels[2][i][1] = chns.get(i * 3 + 1 + 90);
            channels[2][i][2] = chns.get(i * 3 + 2 + 90);
            maxG = maxG > channels[2][i][1] ?
                    maxG : channels[2][i][1];
            minR = minR < channels[2][i][2] ?
                    minR : channels[2][i][2];
        }
//        Log.v("fuck", "maxG: " + maxG);
//        Log.v("fuck", "minR: " + minR);
        ProcessActivity.thresholds[2][1] = minG;
        ProcessActivity.thresholds[2][2] = minR;
        minB = 500;
        minG = 500;
        minR = 500;
        maxB = 0;
        maxG = 0;
        maxR = 0;

        // pink
        for(int i = 0; i < 15; i++){
            channels[3][i][0] = chns.get(i * 3 + 135);
            channels[3][i][1] = chns.get(i * 3 + 1 + 135);
            channels[3][i][2] = chns.get(i * 3 + 2 + 135);
            minB = minB < channels[3][i][0] ?
                    minB : channels[3][i][0];
            minR = minR < channels[3][i][2] ?
                    minR : channels[3][i][2];
        }
//        Log.v("fuck", "minB: " + minB);
//        Log.v("fuck", "minR: " + minR);
        ProcessActivity.thresholds[3][0] = minB;
        ProcessActivity.thresholds[3][2] = minR;

        Log.v("fuck", "-----------------*-*-*-");
        Mat m = Mat.zeros(60, 2, CvType.CV_32F);

        for(int i = 0; i < 60; i++){
            Log.v("fuck", "H: " + hList.get(i));
            m.get(i, 0)[0] = hList.get(i);
        }

        for(int i = 0; i < 60; i++){
            Log.v("fuck", "S: " + sList.get(i));
            m.get(i, 1)[0] = sList.get(i);
        }

        Tool.kmeans(m);

        return true;
    }

//    public boolean registerFlower(Flower flower){
//
//        sqldb = this.database.getWritableDatabase();
//
//        ContentValues cv = new ContentValues();
//
//        // To convert the bitmap to binary
//        Bitmap bitmap = flower.getBitmap();
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//        byte[] binaryBitmap = baos.toByteArray();
//
//        cv.put("keypoint", flower.keyPointToJSON());
//        cv.put("bitmap", binaryBitmap);
//
//        sqldb.insert("daisy", null, cv);
//
//        sqldb.close();
//
//        return true;
//    }

    public Flower getFlower(String name, int id){

        sqldb = database.getReadableDatabase();

        String[] cols = {"bitmap"};

        Cursor c = sqldb.query(name, cols,
                "id = " + id, null, null, null, null, null);

        c.moveToLast();

        byte[] binaryBitmap = c.getBlob(0);
        Bitmap bitmap = BitmapFactory.decodeByteArray(
                binaryBitmap, 0, binaryBitmap.length);

//        String json = c.getString(1);

//        MatOfKeyPoint matOfKeyPoint = Flower.JSONToKeyPoint(json);

        Flower flower = new Flower(bitmap, name);

        c.close();
        sqldb.close();

        return flower;
    }

    public Flower[] getFlowers(String name, int type){

        sqldb = database.getReadableDatabase();

        String[] cols = {"bitmap"};

        Flower[] flowers = new Flower[5];

        Cursor c = sqldb.query(name, cols,
                null, null, null, null, null, null);

        for (int i = 0; i < 5; i++){
            c.moveToNext();

            byte[] binaryBitmap = c.getBlob(0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    binaryBitmap, 0, binaryBitmap.length);
            flowers[i] = new Flower(bitmap, name);
        }

//        String json = c.getString(1);

//        MatOfKeyPoint matOfKeyPoint = Flower.JSONToKeyPoint(json);

        c.close();
        sqldb.close();

        return flowers;
    }

    public List<Integer> queryAll(){

        List<Integer> list = new LinkedList<Integer>();

        Cursor c = sqldb.query("daisy", null, null, null, null, null, null);

        c.moveToFirst();

        sqldb.close();

//        while(c.moveToNext()){
//            result = c.getBlob(1);
//            ByteArrayInputStream bais = new ByteArrayInputStream(result);
//            ObjectInputStream ois = null;
//            try{
//                ois = new ObjectInputStream(bais);
//
//                flower = (Flower)ois.readObject();
//
//                list.add(flower);
//
//            } catch(Exception e){
//                e.printStackTrace();
//            }finally {
//                try {
//                    ois.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    bais.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        return list;
    }

    public boolean deleteFlower(int id){

        String[] args = {String.valueOf(id)};

        if(sqldb.delete("registration", "id = ?", args) == 1){
            return true;
        }

        return false;
    }

    private void deleteFlowerById(int id) {
        String whereClause =  "id = ?" ;
        String[] whereArgs = {id + ""};
        sqldb.delete("daisy", whereClause, whereArgs);
    }

    public int clean(){
        int number = 0;
        SQLiteDatabase sqldb = this.database.getWritableDatabase();
        while(queryAll().size() != 0){
            number = sqldb.delete("registration", null, null);
        }
        return number;
    }
}
