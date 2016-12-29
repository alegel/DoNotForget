package com.donotforget.user.donotforget.objects;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.donotforget.user.donotforget.R;

import java.util.ArrayList;

/**
 * Created by user on 06.07.2016.
 */
public class GroupListAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {
    private Context context;
    public ArrayList<Group> dataSet;
    private LayoutInflater inflater;

    public GroupListAdapter(Context context, ArrayList<Group> groups) {
        this.context = context;
        this.dataSet = groups;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getGroupCount() {
        return dataSet.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return dataSet.get(groupPosition).children.size();
    }

    @Override
    public Group getGroup(int groupPosition) {
        return dataSet.get(groupPosition);
    }

    @Override
    public MyContacts getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).children.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        View v = convertView;
        String groupName = getGroup(groupPosition).getGroup_name().toString();

        if(groupName != null){
            v = inflater.inflate(R.layout.group_view, null);
            TextView tvGroup = (TextView)v.findViewById(R.id.txtGroup);
            tvGroup.setText(groupName);
            CheckBox cbGroup = (CheckBox)v.findViewById(R.id.cbGroup);
            cbGroup.setFocusable(false);

            cbGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    getGroup(groupPosition).isChecked = isChecked;
                }
            });
            cbGroup.setChecked(getGroup(groupPosition).isChecked);
        }
        return v;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View view = convertView;
        MyContacts contact = getChild(groupPosition, childPosition);
        String child = contact.getName();
        if(child != null){
            view = inflater.inflate(R.layout.child_group,null);
            TextView tvChild = (TextView)view.findViewById(R.id.txtGroup);
            tvChild.setText(child.toString());
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int i) {

    }

    @Override
    public void onGroupCollapsed(int i) {

    }

    @Override
    public long getCombinedChildId(long l, long l1) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long l) {
        return 0;
    }

}
