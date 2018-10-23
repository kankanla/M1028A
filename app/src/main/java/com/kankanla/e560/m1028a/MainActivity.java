package com.kankanla.e560.m1028a;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private Sensor sensor;
    private Handler handler;
    private TextView textView;
    private boolean fund_light_sensor;
    private start_Dialog start_dialog;
    private int lu_now;
    private long lu_max;
    private long lu_min;
    private long lu_agr;
    private Timer timer;
    private int SHOW_period;

    protected TimerTask timerTask;

    {
        lu_now = 0;
        lu_max = 0;
        lu_agr = 0;
        lu_min = 0;
        SHOW_period = 600;
        fund_light_sensor = false;

    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.title);

        //SupportActionBar 背景色の変更
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        int color = android.R.color.holo_blue_dark;
        Drawable backgroundDrawable = getApplicationContext().getResources().getDrawable(color);
        actionBar.setBackgroundDrawable(backgroundDrawable);
//        actionBar.hide();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        textView = findViewById(R.id.luText);
        handler = new Handler();
        timer = new Timer();

        SET_SCREEN_LAND();  //スクリン横設定
        Fund_LIGHT_Sensor();    //センサが存在するか?
        SET_SCREEN_SLEEP_ON();  //スリープOFF
        setScreenBrightness();      //スクリン明るさ
    }

    @Override
    protected void onStart() {
        super.onStart();
        start_dialog = new start_Dialog(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start_dialog.show();
            }
        }, 1);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start_dialog.hide();
            }
        }, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        admo();
        if (fund_light_sensor) {
            //ルーメン値の表示 600s間隔
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            int x = 0;
                            for (int i = 0; i < 1000; i++) {
                                x = x + lu_now;
                            }

                            textView.setText(String.valueOf(x / 1000));
                            if (lu_now == 0) {
                                start_dialog.show();
                            } else if (lu_now > 5) {
                                start_dialog.hide();
                            }
                        }
                    });
                }
            };
            timer.schedule(timerTask, 5, SHOW_period);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "no light sensor", Toast.LENGTH_SHORT).show();
        }
//        sen_listener();     // //センサー起動
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lu_now = (int) event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void sen_listener() {
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    lu_now = (int) event.values[0];
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void Fund_LIGHT_Sensor() {
        //LIGHTセンサー存在するか?
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            fund_light_sensor = true;
        } else {
            fund_light_sensor = false;
        }
    }

    protected void SET_SCREEN_POR() {
        //横画面
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void SET_SCREEN_LAND() {
        //横画面
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    protected void SET_SCREEN_SLEEP_ON() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void SET_SCREEN_SLEEP_OFF() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void reset_MIX() {
        lu_max = 0;
        lu_agr = 0;
        lu_min = 0;
    }

    protected void setScreenBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1;
        getWindow().setAttributes(lp);
    }

    protected void admo() {
        MobileAds.initialize(this, getString(R.string.admob_APP_ID));
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.admin_layout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        AdView adView = new AdView(this);
//        adView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        adView.setLayoutParams(layoutParams);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setAdSize(AdSize.FULL_BANNER);
        adView.setAdUnitId(getString(R.string.admob_1));

        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addTestDevice(getString(R.string.addTestDeviceH));
        builder.addTestDevice(getString(R.string.addTestDeviceASUS));
        AdRequest adRequest = builder.build();

        adView.loadAd(adRequest);
        viewGroup.addView(adView);
    }

    class start_Dialog extends AlertDialog {
        protected start_Dialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.start_dialog);
        }
    }

}
