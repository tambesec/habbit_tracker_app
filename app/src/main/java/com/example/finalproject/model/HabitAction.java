package com.example.finalproject.model;

import java.io.Serializable;

public class HabitAction implements Serializable {
    private String actionTime;
    private double value;

    public HabitAction() {
    }

    public HabitAction(String actionTime, double value) {
        this.actionTime = actionTime;
        this.value = value;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

