package com.donotforget.user.donotforget.objects;

/**
 * Created by user on 06.07.2016.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
public class Group implements Serializable {
    private int group_id;
    private String group_name;
    public ArrayList<MyContacts> children = new ArrayList<>();
    public boolean isChecked;

    static Random random = new Random();

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public ArrayList<MyContacts> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<MyContacts> children) {
        this.children = children;
    }

    public Group(String group_name) {
        this.group_name = group_name;
    }
}
