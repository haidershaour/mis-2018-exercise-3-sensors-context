package com.example.shaour.assignment3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener,GPSCallback {

    public GoogleApiClient mApiClient;
    private static final String TAG = "";
    private GPSManager gpsManager = null;
    private double speed = 0.0;
    String activity;
    Boolean isGPSEnabled=false;
    double currentSpeed,kmphSpeed;
    public SensorManager senSensorManager;
    public Sensor senAccelerometer;
    public TextView textView1;
    public TextView xaxis1;
    LocationManager locationManager;
    public TextView yaxis1;
    public TextView zaxis1;
    public TextView speed1;
    public TextView Activity1;
    public TextView shake1;
    public float acelVal; //current accelaration value and gravity
    public float acelLast; // last aceelaration value and gravity
    public float shake; //accelaration value differ from gravity
    MediaPlayer mp;
    private LineChart lineChart;// 声明图表控件
    private LineChart fftlineChart;// 声明图表控件

    public List<Entry> poitListx;
    public Entry addfft;
    private double[] freqCounts;
    public LineDataSet lDSfft;

    public int EntryIndex = 0;
    private int sampleRate;
    public LineData linedata;
    public LineData fftlinedata;
    //MediaPlayer mp;

    public int fttidx = 0;


    public LineDataSet dataSetx;
    public LineDataSet dataSety;
    public LineDataSet dataSetz;
    public LineDataSet dataSetm;

    public List<Entry> entrylistx;
    public List<Entry> entrylisty;
    public List<Entry> entrylistz;
    public List<Entry> entrylistm;
    public List<Entry> entrylistfft;
    public int i=0;
    public int count=0;
    public SeekBar sb1;
    public SeekBar sb2;

    private AsyncTask aTask;
    private double[] mgroup;
    public int winSize=64;
    //example variables
    private double[] rndAccExamplevalues;
    //private double[] freqCounts;

    public int k;
    public int d;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        getCurrentSpeed();

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        sb1 = (SeekBar)findViewById(R.id.sb1);
        sb2 = (SeekBar)findViewById(R.id.sb2);

        sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                //System.out.println(progress);

                d = progress+1;

                //winSize = (int)Math.pow(2,progress);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if(d/8 == 0){
                    d=8;
                }
                winSize = (int)Math.pow(2,d/8);
                //System.out.println("kkkkkk"+winSize);

                d = 1;


            }
        });









        lineChart = (LineChart) findViewById(R.id.linechart);
        lineChart.setBackgroundColor(Color.GRAY);


        fftlineChart = (LineChart) findViewById(R.id.linechart1);

        fftlineChart.setBackgroundColor(Color.WHITE);

        lineChart.getDescription().setText("X,Y,Z,M");
        fftlineChart.getDescription().setText("FFT");



        linedata = new LineData();
        lineChart.setData(linedata);

        fftlinedata = new LineData();
        fftlineChart.setData(fftlinedata);

        XAxis x1 = lineChart.getXAxis();
        YAxis y1 = lineChart.getAxisLeft();
        YAxis y2 = lineChart.getAxisRight();

        XAxis fftx1 = fftlineChart.getXAxis();
        YAxis ffty1 = fftlineChart.getAxisLeft();
        YAxis ffty2 = fftlineChart.getAxisRight();
//
        entrylistx = new ArrayList<Entry>();
        entrylisty = new ArrayList<Entry>();
        entrylistz = new ArrayList<Entry>();
        entrylistm = new ArrayList<Entry>();

        entrylistfft = new ArrayList<Entry>();







       // mgroup = new double[winSize];



        //aTask = new FFTAsynctask(winSize).execute(mgroup);

        //initiate and fill example array with random values
//        rndAccExamplevalues = new double[64];
//        randomFill(rndAccExamplevalues);
//        new FFTAsynctask(64).execute(rndAccExamplevalues);


        mp = MediaPlayer.create( MainActivity.this, R.raw.mysong );
        acelVal=SensorManager.GRAVITY_EARTH;
        acelLast=SensorManager.GRAVITY_EARTH;
        shake=0.09f;

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);
        //senSensorManager.registerListener(this, senAccelerometer , 1000000);
        textView1=(TextView)findViewById(R.id.textView);
        speed1=(TextView)findViewById(R.id.speed1);
        shake1=(TextView)findViewById( R.id.shake1 );
        Activity1=(TextView)findViewById(R.id.Activity1);

        //sampleRate = SensorManager.SENSOR_DELAY_GAME;


        //System.out.println(sampleRate);

        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //System.out.println(progress);
                sampleRate = progress;
                System.out.println(sampleRate);



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                senSensorManager.registerListener(MainActivity.this, senAccelerometer, sampleRate*1000000);


            }
        });






    }

    private Entry addEntry(float f, int EntryIndex) {


        Entry e1 = new Entry(EntryIndex,f);

        return e1;
    }

    private void getCurrentSpeed() {
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(this);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled) {
            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }
    }

    @Override
    public void onGPSUpdate(Location location) {
        speed = location.getSpeed();
        currentSpeed = round(speed,3,BigDecimal.ROUND_HALF_UP);
        kmphSpeed = round((currentSpeed*3.6),3,BigDecimal.ROUND_HALF_UP);
        if (speed>0){
            Log.e(TAG, "onGPSUpdate: " + kmphSpeed+"km/h" );
            speed1.setText(kmphSpeed+"km/h");
        }

    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
        mp.release();
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }








    List <Double> arrayList = new ArrayList<>();

    @Override
    public void onSensorChanged(SensorEvent event) {






        float x= event.values[0];
        float y= event.values[1];
        float z= event.values[2];
        float m = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));


        if(k<winSize){



            arrayList.add((double)m);

            //System.out.println("jj"+winSize);

            k++;
        }else {

            Double[] mgrou = new Double[arrayList.size()];
            double[] mgroup = new double[arrayList.size()];

            mgrou = arrayList.toArray(mgrou);


            arrayList.clear();


            for(int h=0;h<mgrou.length;h++){
                double d = mgrou[h].doubleValue();
                mgroup[h] = d;
            }
            aTask = new FFTAsynctask(winSize).execute(mgroup,new double[winSize]);
            k = 0;





//
        }

        EntryIndex = EntryIndex+1;






        Log.d( TAG, "onSensorChanged: X: " + x + "Y: " + y + "Z: " + z );

        acelLast=acelVal;
        acelVal=(float) Math.sqrt( (double)(x*x+y*y+z*z) );

        float delta= acelVal-acelLast;
        shake=shake * 0.9f + delta;
        shake1.setText( ""+shake );

        //System.out.println("oooooooooooooooooo");
        //System.out.println(shake);
        //kmphSpeed = 8;

        if (shake>= 3 && kmphSpeed>=1) {
            textView1.setText( "MOOOOOOOVVVVVVIIIIIIIINNNNNNNGGGGGGGG" );

            System.out.println(kmphSpeed);
            changechart(event,EntryIndex);
            mp.start();
        }
        else{
            //mp.pause();

            textView1.setText( "" );
            changechart(event,EntryIndex);
        }
    }

    public void changechart(SensorEvent event,int EnteryIndex) {

        Entry addx = addEntry(event.values[0], EntryIndex);
        entrylistx.add(addx);

        Entry addy = addEntry(event.values[1], EntryIndex);
        entrylisty.add(addy);

        Entry addz = addEntry(event.values[2], EntryIndex);
        entrylistz.add(addz);

        float m = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        Entry addm = addEntry(m, EntryIndex);
        entrylistm.add(addm);






        if (i == 0) {


            LineDataSet lDSx = new LineDataSet(entrylistx, "x");
            LineDataSet lDSy = new LineDataSet(entrylisty, "y");
            LineDataSet lDSz = new LineDataSet(entrylistz, "z");
            LineDataSet lDSm = new LineDataSet(entrylistm, "m");



            lDSx.setColor(Color.RED);
            lDSy.setColor(Color.YELLOW);
            lDSz.setColor(Color.BLUE);
            lDSm.setColor(Color.GREEN);


            lDSx.setCircleRadius(1f);
            lDSy.setCircleRadius(1f);
            lDSz.setCircleRadius(1f);
            lDSm.setCircleRadius(1f);


            //        linedata.addEntry(addm,0);

            linedata.addDataSet(lDSx);
            linedata.addDataSet(lDSy);
            linedata.addDataSet(lDSz);
            linedata.addDataSet(lDSm);


            lineChart.setData(linedata);


            linedata.notifyDataChanged();


            lineChart.notifyDataSetChanged();

            lineChart.invalidate();

//
            i =  1;

        } else {

            linedata.addEntry(addx, 0);
            linedata.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            linedata.addEntry(addy, 1);
            linedata.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            linedata.addEntry(addz, 2);
            linedata.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            linedata.addEntry(addm, 3);
            linedata.notifyDataChanged();
            lineChart.notifyDataSetChanged();

            lineChart.setVisibleXRangeMaximum(winSize);
            //移到某个位置
            lineChart.moveViewToX(linedata.getEntryCount() - 5);
            lineChart.invalidate();

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(Location location) {
        float currentspeed = location.getSpeed();
        Log.d( TAG, "SpeedChange: " + location.getSpeed());
        if (location==null){
            speed1.setText( "_._ m/s" );
        }
        else{
        if (currentspeed>1.0)
        speed1.setText(""+ currentspeed);
            mp.start();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

        private int wsize; //window size must be power of 2

        // constructor to set window size
        FFTAsynctask(int wsize) {
            this.wsize = wsize;
        }

        @Override
        protected double[] doInBackground(double[]... values) {


            double[] realPart = values[0].clone(); // actual acceleration values
            double[] imagPart = new double[wsize]; // init empty
            for (int r =0;r<values.length;r++){

//                System.out.println(realPart[r]);


            }

            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */
            FFT fft = new FFT(wsize);
            fft.fft(realPart, imagPart);

            //init new double array for magnitude (e.g. frequency count)
            double[] magnitude = new double[wsize];


            //fill array with magnitude values of the distribution
            for (int i = 0; wsize > i ; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
//                System.out.println(realPart[i]);
            }




            return magnitude;

        }



        @Override
        protected void onPostExecute(double[] values) {

            //hand over values to global variable after background task is finished
            freqCounts = values;


            if(fttidx == 0){
                addfft = addEntry(0, EntryIndex);
                entrylistfft.add(addfft);

                fttidx++;
//fft
                LineDataSet lDSfft = new LineDataSet(entrylistfft, "fft");
                //fft
                lDSfft.setColor(Color.BLACK);
                //fft
                lDSfft.setCircleRadius(5f);
                //fft
                fftlinedata.addDataSet(lDSfft);
                //fft
                fftlineChart.setData(fftlinedata);
                fftlinedata.notifyDataChanged();
                fftlineChart.notifyDataSetChanged();

                fftlineChart.invalidate();
                fttidx++;
            }



            if(lDSfft==null){}
            else {
                lDSfft.clear();
            }
            for(int e = 0; e<freqCounts.length;e++){

                addfft = addEntry((float) freqCounts[e], fttidx);
                count++;
//                fttidx++;
                //System.out.println(count);
                fftlinedata.addEntry(addfft, 0);
//

//                fftlinedata.notifyDataChanged();
                fftlineChart.notifyDataSetChanged();
////
                fftlineChart.setVisibleXRangeMaximum(winSize);
////                    //移到某个位置
                fftlineChart.moveViewToX(fftlinedata.getEntryCount() - 5);
                fftlineChart.invalidate();

                fttidx++;
            }
            //System.out.println("=========");




        }
    }




    /**
     * little helper function to fill example with random double values
     */
    public void randomFill(double[] array){
        Random rand = new Random();
        for(int i = 0; array.length > i; i++){
            array[i] = rand.nextDouble();
        }
    }


}
