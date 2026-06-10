package com.example.finalproject.model;

import java.io.Serializable;
import java.util.Date;

public class Account implements Serializable {
    private String Avatar;
    private String Sex;
    private String Gmail;
    private String Name;
    private String Password;
    private String Born;
    private String Phone;
    private String Username;

    public Account() {
    }

    public Account(String avatar, String sex, String gmail, String name, String password, String born, String phone, String username) {
        Avatar = avatar;
        Sex = sex;
        Gmail = gmail;
        Name = name;
        Password = password;
        Born = born;
        Phone = phone;
        Username = username;
    }


    public Account(String password, String username) {
        Password = password;
        Username = username;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    public String getGmail() {
        return Gmail;
    }

    public void setGmail(String gmail) {
        Gmail = gmail;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getBorn() {
        return Born;
    }

    public void setBorn(String born) {
        Born = born;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    @Override
    public String toString() {
        return "Account{" +
                "Avatar='" + Avatar + '\'' +
                ", Sex='" + Sex + '\'' +
                ", Gmail='" + Gmail + '\'' +
                ", Name='" + Name + '\'' +
                ", Password='" + Password + '\'' +
                ", Born=" + Born +
                ", Phone='" + Phone + '\'' +
                ", Username='" + Username + '\'' +
                '}';
    }
}
