package fyi.meld.gaitlab;

import com.orm.SugarRecord;

public class SensorReading extends SugarRecord {
    String sensor;
    String timestamp;
    float x;
    float y;
    float z;

    Experiment experiment;

    // Default constructor is necessary for SugarRecord
    public SensorReading() {

    }

    public SensorReading(String sensor, String timestamp, float x, float y, float z, Experiment experiment) {
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.experiment = experiment;
    }
}