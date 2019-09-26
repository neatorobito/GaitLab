package fyi.meld.gaitlab;

import android.hardware.Sensor;

import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Experiment extends SugarRecord {
    String date;
    String start;
    String end;
    int duration;

    // Default constructor is necessary for SugarRecord
    public Experiment() {
        this.date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        this.start = new SimpleDateFormat("HH:mm:ss.sssZ", Locale.US).format(new Date());
    }

    public Experiment(String date, String start, String end, int duration) {
        this.date = date;
        this.start = start;
        this.end = end;
        this.duration = duration;
    }
}