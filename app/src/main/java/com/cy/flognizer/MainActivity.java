package com.cy.flognizer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.cy.flognizer.domain.Flower;
import com.cy.flognizer.model.Database;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

public class MainActivity extends ActionBarActivity {

    //  define widget
    private Button btnStart;
    private Button btnImport;
    private Button btnLibrary;
    private TextView tvStatus;
    private static String openCVStatus = "";

    //  init the OpenCV and log the result
    static{
        if (!OpenCVLoader.initDebug()){
            openCVStatus = "OpenCV is not OK";
        } else {
            openCVStatus = "OpenCV is OK !";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  The start button definition
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        CameraActivity.class);
                startActivity(intent);
            }
        });

        // The import button definition
        btnImport = (Button) findViewById(R.id.btn_import);
        btnImport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =
                        new Intent(MainActivity.this,
                                ProcessActivity.class);
                startActivity(intent);
            }
        });

        // The Library button
        btnLibrary = (Button) findViewById(R.id.library);
        btnLibrary.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,
                        RegistrationActivity.class);
                startActivity(intent);
            }
        });

        //  the status text definition
        tvStatus = (TextView) findViewById(R.id.status);
        tvStatus.setText(openCVStatus);

        dbInit();
    }

    private void dbInit(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
