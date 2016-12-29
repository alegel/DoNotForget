package com.donotforget.user.donotforget.objects;

import java.io.Serializable;

/**
 * Created by user on 20.07.2016.
 */
public class ContactSchedule implements Serializable {
    private long    id;
    private String  contactName;
    private String  phone;
    private long    schedule_id;
    private int     schedule_owner;
    private int     isSent;

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int isSent) {
        this.isSent = isSent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return contactName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }



    public long getSchedule_id() {
        return schedule_id;
    }

    public void setSchedule_id(long schedule_id) {
        this.schedule_id = schedule_id;
    }

    public int getSchedule_owner() {
        return schedule_owner;
    }

    public void setSchedule_owner(int schedule_owner) {
        this.schedule_owner = schedule_owner;
    }

    public void reset(){
        id = 0;
        contactName = "";
        phone = "";
        schedule_id = 0;
        schedule_owner = 0;
        isSent = 1;
    }
    public void Copy(ContactSchedule obj){
        this.id = obj.id;
        this.contactName = obj.contactName;
        this.phone = obj.phone;
        this.schedule_id = obj.schedule_id;
        this.schedule_owner = obj.schedule_owner;
        this.isSent = obj.isSent;
    }
}
