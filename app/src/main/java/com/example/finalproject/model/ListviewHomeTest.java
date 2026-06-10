package com.example.finalproject.model;

import java.io.Serializable;

public class ListviewHomeTest implements Serializable {
    private final String habitId;
    private String nameHabit;
    private String section;
    private String timeHabit;
    private String doneProgress;
    private int done;
    private double donViTang;
    private double target;
    private String status;
    private Double doing;

    public ListviewHomeTest(String habitID, String nameHabit, String section, String timeHabit, String doneProgress, int done, double donVi, double target, String status, Double doing) {
        this.habitId = habitID;
        this.nameHabit = nameHabit;
        this.section = section;
        this.timeHabit = timeHabit;
        this.doneProgress = doneProgress;
        this.done = done;
        this.donViTang = donVi;
        this.target = target;
        this.status = status;
        this.doing = doing;
    }

    public String getHabitId() { return habitId; }
    public String getNameHabit() { return nameHabit; }
    public String getSection() { return section; }
    public String getTimeHabit() { return timeHabit; }
    public String getDoneProgress() { return doneProgress; }
    public int getDone() { return done; }
    public double getDonViTang() { return donViTang; }
    public double getTarget() { return target; }
    public String getStatus() { return status; }
    public Double getDoing() { return doing; }

    public void setStatus(String status) { this.status = status; }
    public void setDoing(Double doing) { this.doing = doing; }
    public void setSection(String section) { this.section = section; }
    public void setDonViTang(double donViTang) { this.donViTang = donViTang; }
    public void setTarget(double target) { this.target = target; }
    public void setTimeHabit(String timeHabit) { this.timeHabit = timeHabit; }
    public void setDoneProgress(String doneProgress) { this.doneProgress = doneProgress; }
    public void setDone(int done) { this.done = done; }
    public void setNameHabit(String nameHabit) { this.nameHabit = nameHabit; }
}
