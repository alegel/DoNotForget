package com.donotforget.user.donotforget;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.donotforget.user.donotforget.interfaces.IFragmentsEventListener;
import com.donotforget.user.donotforget.objects.ContactsListAdapter;
import com.donotforget.user.donotforget.objects.MyContacts;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment{
    public IFragmentsEventListener iFragmentsEventListener;
    public ListView lvContacts;
    public ArrayAdapter<MyContacts> adapter;
    public ArrayList<MyContacts> contacts = new ArrayList<>();

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(contacts.size() <= 0){
            Toast.makeText(context,getResources().getString(R.string.contacts_list_empty_err),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, null);

        lvContacts = (ListView) view.findViewById(R.id.lvContacts);
        lvContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ContactsListAdapter adapter = new ContactsListAdapter(contacts,getActivity());

        lvContacts.setAdapter(adapter);

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(),R.anim.list_layout_controller);
        lvContacts.setLayoutAnimation(controller);

        return view;
    }

}
