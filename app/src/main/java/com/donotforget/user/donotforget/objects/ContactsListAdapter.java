package com.donotforget.user.donotforget.objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.donotforget.user.donotforget.R;

import java.util.ArrayList;

/**
 * Created by user on 14.08.2016.
 */
public class ContactsListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {
    public ArrayList<MyContacts> contacts = new ArrayList<>();
    private Context context;
    private LayoutInflater layoutInflater;

    public ContactsListAdapter(ArrayList<MyContacts> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public MyContacts getItem(int pos) {
        return contacts.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        MyContacts contact = getItem(position);
        View row;
        Holder holder;

        if(convertView == null){
            row = layoutInflater.inflate(R.layout.contacts_list,viewGroup,false);
            holder = new Holder();

            holder.tvContactName = ((TextView)row.findViewById(R.id.tvContactName));
            holder.tvPhone = ((TextView) row.findViewById(R.id.tvPhone));
            holder.cbSelected = ((CheckBox) row.findViewById(R.id.cbSelected));
            holder.cbSelected.setOnCheckedChangeListener(this);

            row.setTag(holder);
        }
        else{
            row = convertView;
            holder = (Holder) row.getTag();
        }

        holder.tvContactName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());

        holder.cbSelected.setTag(contact);
        if(contact.isSelected() == 1)
            holder.cbSelected.setChecked(true);
        else
            holder.cbSelected.setChecked(false);

        holder.cbSelected.setTag(contact);


        return row;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        MyContacts myContact = (MyContacts) buttonView.getTag();
        if(isChecked == true)
            myContact.setSelected(1);
        else
            myContact.setSelected(0);
    }

    static  class Holder{
        TextView tvContactName;
        TextView tvPhone;
        CheckBox cbSelected;

    }
}
