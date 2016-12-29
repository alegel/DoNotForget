package com.donotforget.user.donotforget.objects;

import java.io.Serializable;

/**
 * Created by user on 14.07.2016.
 */
public class MyContacts implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String group;
    private int selected;
    private int isSent;

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int isSent) {
        this.isSent = isSent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int isSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public MyContacts() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return name;
    }

    public MyContacts(String name) {

        this.name = name;
    }

    public void reset(){
        id = "";
        name = "";
        phone = "";
        group = "";
        selected = 0;
        isSent = 1;
    }
}
