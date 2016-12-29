package com.donotforget.user.donotforget.interfaces;

/**
 * Created by user on 06.07.2016.
 */
import java.util.ArrayList;

public interface IFragmentsEventListener {
    public void ContactsEvent(ArrayList<String> contacts);
    public void GroupsEvent(ArrayList<String> groups);
}
