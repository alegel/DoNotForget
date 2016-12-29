package com.donotforget.user.donotforget.objects;

import java.io.Serializable;

/**
 * Created by user on 10.07.2016.
 */
public class MyMessages implements Serializable {
    private long id;
    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MyMessages() {
        reset();
    }

    @Override
    public String toString() {
        return message;
    }

    public void reset(){
        id = 0;
        message = "";
    }
}
