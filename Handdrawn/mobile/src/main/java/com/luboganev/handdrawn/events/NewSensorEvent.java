package com.luboganev.handdrawn.events;


import com.luboganev.handdrawn.data.Sensor;

public class NewSensorEvent {
    private Sensor sensor;

    public NewSensorEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
