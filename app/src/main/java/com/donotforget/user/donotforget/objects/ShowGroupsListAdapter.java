package com.donotforget.user.donotforget.objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.donotforget.user.donotforget.R;

import java.util.ArrayList;

/**
 * Created by user on 13.07.2016.
 */
public class ShowGroupsListAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {
    private Context context;
    public ArrayList<Group> dataSet;
    private LayoutInflater inflater;

    public ShowGroupsListAdapter(Context context, ArrayList<Group> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        View v = convertView;
        String groupName = getGroup(groupPosition).getGroup_name().toString();

        if(groupName != null){
            v = inflater.inflate(R.layout.show_group_view, null);
            TextView tvGroup = (TextView)v.findViewById(R.id.txtShowGroup);
            tvGroup.setText(groupName);
            tvGroup.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return false;
                }
            });
        }
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup viewGroup) {
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
}
