package com.cy.flognizer;

import android.animation.AnimatorSet;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.widget.Toast;

import com.cy.flognizer.domain.Flower;
import com.cy.flognizer.model.Database;
import com.cy.flognizer.model.Singleton;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class ProcessActivity extends ActionBarActivity {

    public static final String PHOTO_FILE_EXTENSION = ".png";
    public static final String PHOTO_MIME_TYPE = "image/png";

    public static final String EXTRA_PHOTO_URI =
            "com.cy.flognizer.view.ProcessActivity.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH =
            "com.cy.flognizer.view.ProcessActivity.extra.PHOTO_DATA_PATH";

    private Uri uri;
    private String dataPath;
    public static final int IMPORT_PHOTO = 0;

    // Save the mat image that got from import pic.
    private Mat img;

    // Save the mat image that processed.
    private Mat processedImg;

    // Save the image that got from camera activity.
    private ImageView imageView;

    private boolean isGray = false;

    private Bitmap thisBitmap;

    private Mat imgToGrab;

    public static int count = 0;

    // Singleton
    private Singleton singleton = Singleton.getSingleton(ProcessActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        // get the intent to deliver the picture.
        uri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
        dataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
        imageView = new ImageView(this);
        imageView.setImageURI(uri);

        Bitmap bitmap = convertImgViewToBmp(imageView);

        img = new Mat(bitmap.getHeight(),
                bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, img);

        setContentView(imageView);
        registerForContextMenu(imageView);

        singleton.initDataset();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        imageView = new ImageView(this);

        if(requestCode == IMPORT_PHOTO){
            try {
                // Code to load image into a Bitmap and
                // convert it to a Mat for processing.

                if(data == null){
                    return;
                }

                final Uri uri = data.getData();

                final InputStream imageStream =
                        getContentResolver().openInputStream(uri);

                Bitmap selectedImage = null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;

                // limit the size of a picture.
                // If greater than 1.2m minification
                try {
                    if(imageStream.available() >= 1200000){
                        selectedImage =
                                BitmapFactory.decodeStream(imageStream,
                                        null, options);
                    } else {
                        selectedImage =
                                BitmapFactory.decodeStream(imageStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Add image to the activity.
                // And judge the orientation of image.
                if(selectedImage.getHeight() > selectedImage.getWidth()){

                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    selectedImage = Bitmap.createBitmap(selectedImage, 0, 0,
                    selectedImage.getWidth(), selectedImage.getHeight(),
                            matrix, true);
                }

                imageView.setImageBitmap(selectedImage);
                setContentView(imageView);
                this.thisBitmap = selectedImage;

                img = new Mat(selectedImage.getHeight(),
                        selectedImage.getWidth(), CvType.CV_8UC4);
//                        selectedImage.getWidth(), CvType.CV_32F);

                imgToGrab = new Mat(selectedImage.getHeight(),
                        selectedImage.getWidth(), CvType.CV_8UC3);

                Utils.bitmapToMat(selectedImage, img);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(3, 200, Menu.NONE, "DELETE");
        menu.add(3, 201, Menu.NONE, "EDIT");
        menu.add(3, 202, Menu.NONE, "SHARE");
        menu.add(3, 203, Menu.NONE, "SHARP");
        super.onCreateContextMenu(menu, view, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 200:
                deletePhoto();
                break;
            case 201:
                editPhoto();
                break;
            case 202:
                sharePhoto();
                break;
            case 203:
//                sharp();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_process, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_import:
                Intent photoPickerIntent =
                        new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, IMPORT_PHOTO);
                return true;
//            case R.id.menu_blur:
//                blur();
//                return true;
//            case R.id.menu_threshold:
//                thresholding();
//                return true;
//            case R.id.menu_contours:
//                contours(this.img);
//                return true;
//            case R.id.menu_sobel:
//                sobel();
//                return true;
            case R.id.menu_register:
                getColor();
//                  grabCut(imgToGrab);
//                register(img);
//                Flower flw = new Flower(this.img, "ref");
//                Flower flw2 = singleton.getFlower("daisy", 1);
//                matchTwoFlowers(flw2, flw);
                return true;
            case R.id.menu_match:
                Flower flower = new Flower(this.img, "test");
                double[] mean = {0, 0, 0, 0};
                double min = 500;
                double now = 500;
                String result = "";

                int color = getColor();

                switch (color){
                    case 0:
                        for(int i = 0; i < 3; i++) {
                            now = matchAllRef(flower, Database.tabelNames[i]);
                            if (now < min) {
                                result = Database.tabelNames[i];
                                min = now;
                            }
                        }
                        break;
                    case 1:
                        for(int i = 0; i < 3; i++) {
                            now = matchAllRef(flower, Database.tabelNames[i + 3]);
                            if (now < min) {
                                result = Database.tabelNames[i + 3];
                                min = now;
                            }
                        }
                        break;
                    case 2:
                        for(int i = 0; i < 3; i++) {
                            now = matchAllRef(flower, Database.tabelNames[i + 6]);
                            if (now < min) {
                                result = Database.tabelNames[i + 6];
                                min = now;
                            }
                        }
                        break;
                    case 3:
                        for(int i = 0; i < 3; i++) {
                            now = matchAllRef(flower, Database.tabelNames[i + 9]);
                            if (now < min) {
                                result = Database.tabelNames[i + 9];
                                min = now;
                            }
                        }
                        break;
                    case 4:
                        Log.v("fuck", "other");
                        for(int i = 0; i < 12; i++) {
                            now = matchAllRef(flower, Database.tabelNames[i]);
                            if (now < min) {
                                result = Database.tabelNames[i];
                                min = now;
                            }
                        }
                        break;
                }

                Toast.makeText(this, "This is a " + result,
                            Toast.LENGTH_LONG).show();
                Log.v("fuck", "decision is ***************" + result);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Bitmap convertImgViewToBmp(ImageView imageView){

        Drawable drawable = imageView.getDrawable();
        if(drawable == null){
            Log.d("debug", "drawable is null--------");
            this.imageView = new ImageView(this);
            this.imageView.setImageResource(R.drawable.sunflower);

            drawable = this.imageView.getDrawable();

            Bitmap exampleBitmap = convertImgViewToBmp(this.imageView);

            img = new Mat(exampleBitmap.getHeight(),
                    exampleBitmap.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(exampleBitmap, img);

            setContentView(this.imageView);
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
//        if(bitmapDrawable == null){
//            Log.d("debug", "bitmapDrawable is null--------");
//        }
        Bitmap sequenceBitmap = bitmapDrawable.getBitmap();

//        if(sequenceBitmap == null){
//            Log.d("debug", "sequenceBitmap is null--------");
//        }
        return sequenceBitmap;
    }

    // After prcess, convert the imageView
    private void afterProcess(Mat image, Mat procImg){

        this.img = procImg;

        // Create a frame to save bitmap
        Bitmap bitmap = Bitmap.createBitmap(image.cols(),
                image.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(procImg, bitmap);
        imageView.setImageBitmap(bitmap);
    }

    private void grabCut(Mat srcImg){

//        Log.v("fuck", "srcImgC3 chnn: " + srcImg.channels());
//
//        List<Mat> rgba = new ArrayList<Mat>(3);
//        Core.split(srcImg, rgba);
//        Log.v("fuck", "rgba count:" + rgba.size());
//        rgba.remove(3);
//        List<Mat> rgb = rgba;
//        Log.v("fuck", "rgba count:" + rgb.size());
//
//        Mat dst = new Mat(srcImg.cols(), srcImg.rows(),
//                CvType.CV_8UC3);
//        Core.merge(rgb, dst);
//        Log.v("fuck", "dst chnn" + dst.channels());

//        Mat alpha = new Mat();
//        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.threshold(src, alpha, 100, 255,
//                Imgproc.THRESH_BINARY);

//        Mat bgd = new Mat();
//        bgd.setTo(new Scalar(255, 255, 255));
//        Mat fgd = new Mat();
//        fgd.setTo(new Scalar(255, 255, 255));
//
//        Mat mask = new Mat();
//        mask.setTo(new Scalar(125));
//
//        // Rect
//        int ro = dst.rows();
//        int co = dst.cols();
//        Point p1 = new Point(co/5, ro/5);
//        Point p2 = new Point(co - co / 5, ro - ro / 8);
//        Rect rect = new Rect(p1, p2);
//
//        Imgproc.grabCut(
//                dst,
//                mask,
//                rect,
//                bgd, fgd, Imgproc.GC_INIT_WITH_RECT);
//
//        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3));
//
//        Core.compare(mask, source, mask, Core.CMP_EQ);
////
//        Mat foreground = new Mat(dst.size(), CvType.CV_8UC3,
//                new Scalar(255, 255, 255));
////                new Scalar(0, 0, 0));
//        dst.copyTo(foreground, mask);

//        Core.rectangle(img, p1, p2, new Scalar(255, 0, 0, 255));

//        Mat background = new Mat();
//        try {
//            background = Utils.loadResource(getApplicationContext(),
//                    R.drawable.sunflower );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Mat tmp = new Mat();
//        Imgproc.resize(background, tmp, img.size());
//
//        background = tmp;
//
//        Mat tempMask = new Mat(foreground.size(), CvType.CV_8UC1,
//                new Scalar(255, 255, 255));
//        Imgproc.cvtColor(foreground, tempMask, 6/* COLOR_BGR2GRAY */);
//        //Imgproc.threshold(tempMask, tempMask, 254, 255, 1 /* THRESH_BINARY_INV */);
//
//        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
//        Mat dst = new Mat();
//        background.setTo(vals, tempMask);
//        Imgproc.resize(foreground, tmp, mask.size());
//        foreground = tmp;
//        Core.add(background, foreground, dst, tempMask);

//        Log.v("fuck", "mask count: " + mask.total());


//        Scalar chnn = Core.mean(mask);
//        Log.v("fuck", "" + chnn.val[0]);
//        Log.v("fuck", "" + chnn.val[1]);
//        Log.v("fuck", "" + chnn.val[2]);

        Imgproc.cvtColor(srcImg, srcImg, Imgproc.COLOR_BGR2RGB);
        Bitmap b = Bitmap.createBitmap(srcImg.cols(),
                srcImg.rows(), Bitmap.Config.RGB_565);

        Utils.matToBitmap(srcImg, b);
        imageView.setImageBitmap(b);
    }

    /*
    * The blur method.
    *
    * Use a 3x3 kernel to blur the img to processedImg.
    * */
    private void blur() {
        if(processedImg == null){
            processedImg = new Mat();
        }

        Imgproc.GaussianBlur(img, processedImg, new Size(3, 3), 0);

        afterProcess(img, processedImg);
    }

    private int getColor() {
        int w = img.rows() / 4;
        int h = img.cols() / 4;
        Mat m = img.submat(w, w * 2, h, h * 2);

        Scalar chnn = Core.mean(m);
        Log.v("fuck", "" + chnn.val[2]);//B
        Log.v("fuck", "" + chnn.val[1]);//G
        Log.v("fuck", "" + chnn.val[0]);//R

        if(chnn.val[2] < 86.00 &&
                chnn.val[1] > 68.99) {
            // Yellow
            Log.v("fuck", "yellow");
            return 1;
        }else if (chnn.val[2] > 131.19 &&
                chnn.val[0] > 122.21){
            // pink
            Log.v("fuck", "pink");
            return 3;
        }else if (chnn.val[0] > 143.82 &&
                chnn.val[1] < 143.82){
            //red
            Log.v("fuck", "red");
            return 2;
        }else if (chnn.val[2] > 110 && chnn.val[1] > 110 &&
                chnn.val[0] > 110){
            // White
            Log.v("fuck", "white");
            return 0;
        }else {
            // others color
            Log.v("fuck", "other");
            return 4;
        }
    }

    private Mat contours(Mat img) {

        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        double low = 50;
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();
        //Converting the image to gray scale
        Imgproc.cvtColor(img, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdges, low, low * 3);
        //finding contours
        Imgproc.findContours(cannyEdges, contourList,
                hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE);

//        Log.v("fuck", "contour size is: " + contourList.size());
        if (contourList.size() > 300) {
            low += 50;
            cannyEdges.release();
            contourList.clear();

            Imgproc.Canny(grayMat, cannyEdges, low, low * 3);
            //finding contours
            Imgproc.findContours(cannyEdges,contourList
                    ,hierarchy,Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE);
        }

        //Drawing contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                , cannyEdges.cols(), CvType.CV_8UC1);
//        Random r = new Random();

        for(int i = 0; i < contourList.size(); i++) {
            Imgproc.drawContours(contours,
                    contourList, i ,
                    new Scalar(255, 255,255), 1);
        }
        afterProcess(img, contours);
        return contours;
    }
    /*
     * The thresholding method
     *
     * Use the adaptive thresholding
     */
    private void thresholding(){
        if(processedImg == null){
            processedImg = new Mat();
        }

        //  Convert RGB image to gray image
        if(!isGray){
            Imgproc.cvtColor(img, processedImg, Imgproc.COLOR_BGR2GRAY);
            isGray = true;
        }

//        Imgproc.threshold(processedImg, processedImg,150,255,Imgproc.THRESH_BINARY);

        // applying the threhold method with gaussian adaption
        Imgproc.adaptiveThreshold(processedImg, processedImg, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 3, 0);

        afterProcess(img, processedImg);
    }
//
    // Canny Edge Detection.
    public void canny()
    {
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();

        //  Convert RGB image to gray image
//        if(!isBW){
            Imgproc.cvtColor(img, grayMat, Imgproc.COLOR_BGR2GRAY);
//            isBW = true;
//        }
        Imgproc.Canny(grayMat, cannyEdges, 10, 100);

        afterProcess(img, cannyEdges);
    }

    private void register(Mat image){

//        Flower flower = new Flower(image, "daisy");
//
//        flower.setBitmap(this.thisBitmap);
//
//        // Get the String of keypoints
//        String str = flower.keyPointToJSON();
//
//        flower.JSONToKeyPoint(str);
//
//        singleton.registerFlower(flower);
    }

    private double[] matchTwoFlowers(Flower flower, Flower refFlower) {

        // Store the result to be a return value
        double[] result = {0, 0, 0, 0};

        // Get two mat images of two flowers
        Mat image = flower.getImg();
        Mat refImage = refFlower.getImg();

        // Get two mat images of two flowers
//        Mat image = contours(flower.getImg());
//        Mat refImage = contours(refFlower.getImg());

//        double d = Imgproc.matchShapes(image, refImage, Imgproc.CV_CONTOURS_MATCH_I2, 1);
//        Log.v("fuck", "match shapes is: " + d);

        if(!isGray) {
            Imgproc.cvtColor(image, image,
                Imgproc.COLOR_BGRA2GRAY);

            Imgproc.cvtColor(refImage, refImage,
                Imgproc.COLOR_BGRA2GRAY);
            isGray = true;
        }

        // Get two keypoints of two flowers
        // that after detected
        MatOfKeyPoint keyPoint = flower.getKeyPoint();
        MatOfKeyPoint refKeyPoint = refFlower.getKeyPoint();

        // Get a descriptorExtractor of two flowers.
        DescriptorExtractor descriptorExtractor =
                DescriptorExtractor.
                        create(DescriptorExtractor.ORB);

        // declare two descriptors to contain the features
        Mat descriptor = new Mat();
        Mat refDescriptor = new Mat();

        // Use descriptor extractor to get the descriptors
        descriptorExtractor.compute(image, keyPoint, descriptor);
        descriptorExtractor.compute(refImage, refKeyPoint, refDescriptor);

        // get the matcher for BF matching
        DescriptorMatcher descriptorMatcher =
                DescriptorMatcher.create(
                        DescriptorMatcher.BRUTEFORCE_HAMMING);

        // normal match:

        // Declare a mat to contain matches
        MatOfDMatch matches = new MatOfDMatch();

        long t1 = System.currentTimeMillis();

        // match with normal
        descriptorMatcher.match(descriptor, refDescriptor, matches);

        double sumOfMatch = 0;

        // to include the matches
        List<DMatch> matchList = matches.toList();

        // to embrace the good matches
        List<DMatch> goodMatchList = new ArrayList<DMatch>();

        // Calculate the max and min
        double max = 0.0;
        double min = 100.0;
        for (int i = 0; i < matchList.size(); i++) {
            Double dist = (double) matchList.get(i).distance;
            if (dist < min && dist != 0) {
                min = dist;
            }
            if (dist > max) {
                max = dist;
            }
        }

        // select out the good matches
        for (DMatch match : matches.toList()) {
            if (match.distance < min * 3) {
                goodMatchList.add(match);
                sumOfMatch += match.distance;
            }
        }

//        Log.v("fuck", "time per match is: " +
//                Long.toString(t));

        result[0] = min;
        result[1] = goodMatchList.size();
        result[2] = sumOfMatch / goodMatchList.size();

        long t = System.currentTimeMillis() - t1;
        result[3] = t;

//         Draw matches :

//        MatOfDMatch gm = new MatOfDMatch();
//        gm.fromList(goodMatchList);
////        Log.v("fuck", "count: " + gm.size());
//        drawMatches(image, keyPoint,
//                refImage, refKeyPoint, gm);

        return result;
    }


    private void drawMatches(Mat image, MatOfKeyPoint keyPoint,
                             Mat refImage, MatOfKeyPoint refKeyPoint,
                             MatOfDMatch gm){
        // declare a outputImage
        Mat outputImage = new Mat();

        Features2d.drawMatches(image, keyPoint,
                refImage, refKeyPoint,
                gm, outputImage);

        // Display keyPoints
        Bitmap bmp = Bitmap.createBitmap(outputImage.cols(),
                outputImage.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(outputImage, bmp);
        imageView.setImageBitmap(bmp);
    }

    private double matchAllRef(Flower flower, String name) {
        Flower refFlower;

        double mean = 0;
        long t = 0;
        for(int i = 0; i < 5; i++){
            refFlower = singleton.getFlower(name, i + 1);

            double[] result =
                    matchTwoFlowers(flower, refFlower);

            t += result[3];

//            Log.v("fuck", "with " + name + " " + (i + 1) +
//                    ", mean :" + result[2] +
//                    ", time :" + result[3]);
            // get the mean
            mean += result[2];
        }
        mean /= 5;
        Log.v("fuck", "|*mean of good match is : " + mean + " *|");
//        Log.v("fuck", "|*totally time is : " + t + " *|");
//        Log.v("fuck", "|*mean of shape is : " + mean + " *|");

        return mean;
    }

    /*
      * Show a confirmation dialog. On confirmation, the photo is
      * deleted and the activity finishes.
      */
    private void deletePhoto() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(
                ProcessActivity.this);
        alert.setTitle(R.string.photo_delete_prompt_title);
        alert.setMessage(R.string.photo_delete_prompt_message);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog,
                                final int which) {
                    getContentResolver().delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.MediaColumns.DATA + "=?",
                        new String[] { dataPath });
                    finish(); }
                });
        alert.setNegativeButton(android.R.string.cancel, null);
        alert.show();
    }

    /*
      * Show a chooser so that the user may pick an app for editing
      * the photo.
      */
    private void editPhoto() {
        final Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(uri, PHOTO_MIME_TYPE);
        startActivity(Intent.createChooser(intent,
                getString(R.string.photo_edit_chooser_title)));
    }

    /*
      * Show a chooser so that the user may pick an app for sending
      * the photo.
      */
    private void sharePhoto() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(PHOTO_MIME_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.photo_send_extra_subject));
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.photo_send_extra_text));
        startActivity(Intent.createChooser(intent,
                getString(R.string.photo_send_chooser_title)));
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
