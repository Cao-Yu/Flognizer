package com.cy.flognizer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cy.flognizer.domain.Flower;
import com.cy.flognizer.model.Singleton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class RegistrationActivity extends Activity {

    private List<Flower> list = new LinkedList<Flower>();
    private ArrayAdapter adapter;
    private List<String> flowerNames = new ArrayList<String>();
    private ListView listView;
    private Button btnClean;
    private Singleton singleton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        singleton =
                Singleton.getSingleton(RegistrationActivity.this);

        btnClean = (Button) findViewById(R.id.clean_all);
        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number = singleton.clean();
                Toast.makeText(RegistrationActivity.this,
                        "deleted "+ number + " rows",
                        Toast.LENGTH_SHORT).show();
            }
        });

        listView = (ListView)findViewById(R.id.listview);

//        this.list = singleton.queryAll();

        Iterator<Flower> iterator = list.iterator();
        while(iterator.hasNext()){
            flowerNames.add(iterator.next().getName());
        }

        String[] flowerName = new String[list.size()];
        flowerNames.toArray(flowerName);

//        for(Flower f : list){
//            Log.v("fuck", f.toString());
//        }
//
//        if(flowerName == null){
//            Log.v("fuck", "flower Name is null");
//        }
//
//        if(this.list == null){
//            Log.v("fuck", "list is null");
//        }

        this.adapter = new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.registration_cell,
                R.id.text_id,
                flowerName);
        listView.setAdapter(adapter);

    }



}
