package com.example.finalproject.model;

import java.io.Serializable;

public class Habit implements Serializable {
    private String habitId;
    private String userId;
    private String donVi;

    private double donViTang;

    private String khoangThoiGian;

    private String loiNhacNho;

    private String moTa;

    private double mucTieu;

    private String ten;

    private String thoiDiem;

    private String thoiGianBatDau;

    private String thoiGianKetThuc;

    private String thoiGianNhacNho;

    private String trangThai;

    // Constructor
    public Habit() {
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public double getDonViTang() {
        return donViTang;
    }

    public void setDonViTang(double donViTang) {
        this.donViTang = donViTang;
    }

    public String getKhoangThoiGian() {
        return khoangThoiGian;
    }

    public void setKhoangThoiGian(String khoangThoiGian) {
        this.khoangThoiGian = khoangThoiGian;
    }

    public String getLoiNhacNho() {
        return loiNhacNho;
    }

    public void setLoiNhacNho(String loiNhacNho) {
        this.loiNhacNho = loiNhacNho;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public double getMucTieu() {
        return mucTieu;
    }

    public void setMucTieu(double mucTieu) {
        this.mucTieu = mucTieu;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getThoiDiem() {
        return thoiDiem;
    }

    public void setThoiDiem(String thoiDiem) {
        this.thoiDiem = thoiDiem;
    }

    public String getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(String thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public String getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(String thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public String getThoiGianNhacNho() {
        return thoiGianNhacNho;
    }

    public void setThoiGianNhacNho(String thoiGianNhacNho) {
        this.thoiGianNhacNho = thoiGianNhacNho;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

