package fyi.meld.gaitlab;

import com.orm.SugarRecord;

public class SensorReading extends SugarRecord {
    int experiment;
    String sensor;
    String timestamp;
    float x;
    float y;
    float z;
    // Default constructor is necessary for SugarRecord
    public SensorReading() {

    }

    public SensorReading(int experiment, String sensor, String timestamp, float x, float y, float z) {
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.experiment = experiment;
    }
}