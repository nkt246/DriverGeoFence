package com.geofence.drivergeofence;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button routeBtn;
    EditText src, dest;
//    JSONArray retrObj;
    List<ParseGeoPoint> pathList = new ArrayList<ParseGeoPoint>();
    List<ParseGeoPoint> leftFenceList = new ArrayList<ParseGeoPoint>();
    List<ParseGeoPoint> rightFenceList = new ArrayList<ParseGeoPoint>();

    ArrayList<LatLng> leftFenceLatLng = new ArrayList<LatLng>();
    ArrayList<LatLng> rightFenceLatLng = new ArrayList<LatLng>();
    ArrayList<LatLng> pathLatLng = new ArrayList<LatLng>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "wuKOMiIyw9mo579ITKCuAR5lz5OoiIG1m5K9krEG", "BmQtKZdNaFr2Mn3Hi4cgFs1JOXLA3JYcB1KKEv8y");

        src = (EditText)findViewById(R.id.src);
        dest = (EditText)findViewById(R.id.dest);
        routeBtn = (Button) findViewById(R.id.routeBtn);

//        Log.d("Test", src.getText().toString());
//        Log.d("Test", dest.getText().toString());

        routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("PathFence");
                query1.whereEqualTo("Source", src.getText().toString()).whereEqualTo("Destination", dest.getText().toString());
                query1.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject objects, com.parse.ParseException e) {
                        pathList = objects.getList("ParsePoints");
                        leftFenceList = objects.getList("ParseLeftPoints");
                        rightFenceList = objects.getList("ParseRightPoints");
                        Log.d("PathSize", String.valueOf(pathList.size()));
                        Log.d("PathSize", String.valueOf(leftFenceList.size()));
                        Log.d("PathSize", String.valueOf(rightFenceList.size()));

                        ParseGeoPoint pg = pathList.get(0);
                        Log.d("GeoPoint", String.valueOf(pg.getLatitude()));
                        Log.d("GeoPoint", String.valueOf(pg.getLongitude()));

                        for (int i = 0; i < pathList.size(); i++) {
                            ParseGeoPoint pgp = pathList.get(i);
//                            Log.d("GeoPoint", pgp.toString());
                            pathLatLng.add(new LatLng(pgp.getLatitude(), pgp.getLongitude()));

                            pgp = leftFenceList.get(i);
                            leftFenceLatLng.add(new LatLng(pgp.getLatitude(), pgp.getLongitude()));

                            pgp = rightFenceList.get(i);
                            rightFenceLatLng.add(new LatLng(pgp.getLatitude(), pgp.getLongitude()));
                        }

                        //Log.d("PathLng", pathLatLng.toString());
//                        Log.d("PtList", pathLatLng.toString());
                        Intent intent = new Intent(MainActivity.this, DriverMapsActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("pathLatLng", pathLatLng);
                        bundle.putParcelableArrayList("leftFenceLatLng", leftFenceLatLng);
                        bundle.putParcelableArrayList("rightFenceLatLng", rightFenceLatLng);


                        intent.putExtra("pathBundle", bundle);
                        intent.putExtra("Source", src.getText().toString());
                        intent.putExtra("Destination", dest.getText().toString());
                        startActivity(intent);
                    }

                });

                //Log.d("PathLng2", pathLatLng.toString());

            }
        });


//        for (int i=0; i<pathLatLng.size(); i++) {
//            Log.d("Position",pathLatLng.get(i).toString());
//        }
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
