package fyi.meld.gaitlab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.orm.SugarContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensorManager;
    private Sensor mAccel;
    private Sensor mGyro;
    private Sensor mMagnetometer;
    private Sensor mStationary;
    private TriggerEventListener mStationaryEventListener;
    private ArrayList<SensorReading> mReadings;
    private int mCurrentExperiment;
    private NumberPicker mSessionDurationPicker;
    private Button sessionActionBtn;
    private Timer sessionTimer;
    private long startSessionTime;
    private SimpleDateFormat timestampFormat;
    private SharedPreferences sharedPref;

    private final String LOGGING_ID = "GaitLab";
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSessionDurationPicker = findViewById(R.id.sessionDurationPicker);
        mSessionDurationPicker.setMinValue(1);
        mSessionDurationPicker.setMaxValue(10);
        mSessionDurationPicker.setWrapSelectorWheel(true);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sessionActionBtn = findViewById(R.id.sessionActionBtn);
        sessionActionBtn.setOnClickListener(this);


        timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if(sharedPref.contains("experimentID"))
        {
            mCurrentExperiment = sharedPref.getInt("experimentID", -1);
        }
        else
        {
            mCurrentExperiment = 0;
        }


        initSensors();
        SugarContext.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SugarContext.terminate();
    }

    private void initSensors()
    {

        if(mAccel == null)
        {
            mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if(mGyro == null)
        {
            mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        if(mMagnetometer == null)
        {
            mMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        if(mStationaryEventListener == null)
        {
            mStationary = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        }

        Log.d(LOGGING_ID, sensorManager.getSensorList(Sensor.TYPE_ALL).toString());
    }

    private void beginSession()
    {
        isRecording = true;
        mReadings = new ArrayList<SensorReading>();
        int duration = mSessionDurationPicker.getValue();
        SharedPreferences.Editor editor = sharedPref.edit();
        mCurrentExperiment++;
        editor.putInt("experimentID", mCurrentExperiment);
        editor.commit();

        if(mStationaryEventListener == null)
        {
            mStationaryEventListener  = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent triggerEvent) {
                    Log.d(LOGGING_ID, "Significant Motion" + java.util.Arrays.toString(triggerEvent.values));
                }
            };
        }

        sessionTimer = new Timer();
        startSessionTime = System.currentTimeMillis();
        sessionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(isRecording)
                {
                    endSession();
                }
            }
        }, duration * 60 * 1000);

        sensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.requestTriggerSensor(mStationaryEventListener, mStationary);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sessionActionBtn.setText("End Recording");
            }
        });
    }

    private void endSession()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sessionActionBtn.setClickable(false);
            }
        });

        sessionActionBtn.setClickable(false);

        sensorManager.unregisterListener(this, mAccel);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mMagnetometer);
        sensorManager.cancelTriggerSensor(mStationaryEventListener, mStationary);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                for(SensorReading r : mReadings)
                {
                    r.save();
                }
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        getApplicationContext(),
                        "Successfully ended recording session.",
                        Toast.LENGTH_SHORT).show();
                sessionActionBtn.setText("Begin Recording");
                sessionActionBtn.setClickable(true);
            }
        });

        isRecording = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        String humanReadableTimestamp = timestampFormat.format(new Date(System.currentTimeMillis()));

        SensorReading reading = new SensorReading(  mCurrentExperiment,
                                                    sensorEvent.sensor.getStringType(),
                                                    humanReadableTimestamp,
                                                    sensorEvent.values[0],
                                                    sensorEvent.values[1],
                                                    sensorEvent.values[2]);
        mReadings.add(reading);

        Log.d(LOGGING_ID, reading.sensor + " event captured. " + humanReadableTimestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.sessionActionBtn)
        {
            if(!isRecording)
            {
                beginSession();
            }
            else
            {
                int duration = mSessionDurationPicker.getValue();
                long sessionEndTime = System.currentTimeMillis();
                if(((sessionEndTime - startSessionTime) / 1000f) < (duration * 60))
                {
                    sessionTimer.cancel();
                }

                endSession();
            }
        }
    }
}
