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
import com.cy.flognizer.domain.Flower;

import org.json.JSONObject;
import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfKeyPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public static final int REF = 0;
    public static final int SMP = 1;

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

    private void registLoop(String name, String[] ids, String folder){

        sqldb = database.getWritableDatabase();

        ContentValues cv = new ContentValues();

//        File f = new File(path + "/SimpleDataSet");
//        if (f.exists() && f.isDirectory()){
//            Log.v("fuck", "delete: " + f.getPath() +
//                    "result: " + f.delete());
//        }

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
        sqldb.close();
    }

    public boolean initDataset(){
        if(ProcessActivity.count != 0){
            return false;
        }
        ProcessActivity.count++;

        String[] daisy = {"0806", "0813", "0814", "0831", "0832"};
        String[] wind  = {"1204", "1206", "1213", "1214", "1217"};
        String[] lily  = {"04905", "04906", "04907", "04909", "04940"};
        String[] bush  = {"06098", "06101", "06102", "06108", "06111"};

        String[] daff  = {"0005", "0006", "0014", "0021", "0024"};
        String[] sun   = {"0721", "0723", "0728", "0731", "0737"};
        String[] susan   = {"05849", "05850", "05853", "05861", "05873"};
        String[] cup   = {"04625", "04626", "04628", "04629", "04632"};

        String[] anth   = {"01973", "01978", "01979", "01980", "01981"};
        String[] boi   = {"02759", "02763", "02776", "02781", "02799"};
        String[] geran   = {"02640", "02643", "02649", "02651", "02652"};

        String[] ost   = {"05525", "05529", "05536", "05543", "05561"};
        String[] hibi   = {"02872", "02881", "02889", "02890", "02893"};
        String[] pelar   = {"04700", "04714", "04718", "04720", "04721"};

        registLoop(database.TABLE_NAME_DAISY, daisy, DAISY);
        registLoop(database.TABLE_NAME_WIND, wind, WIND);
        registLoop(database.TABLE_NAME_LILY, lily, LILY);
        registLoop(database.TABLE_NAME_BUSH, bush, BUSH);

        registLoop(database.TABLE_NAME_DAFF, daff, DAFF);
        registLoop(database.TABLE_NAME_SUN, sun, SUN);
        registLoop(database.TABLE_NAME_SUSAN, susan, SUSAN);
        registLoop(database.TABLE_NAME_CUP, cup, CUP);

        registLoop(database.TABLE_NAME_ANTH, anth, ANTH);
        registLoop(database.TABLE_NAME_BOI, boi, BOI);
        registLoop(database.TABLE_NAME_GERAN, geran, GERAN);

        registLoop(database.TABLE_NAME_OST, ost, OST);
        registLoop(database.TABLE_NAME_HIBI, hibi, HIBI);
        registLoop(database.TABLE_NAME_PELAR, pelar, PELAR);

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
