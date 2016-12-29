package com.donotforget.user.donotforget;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ExpandableListView;

import com.donotforget.user.donotforget.interfaces.IFragmentsEventListener;
import com.donotforget.user.donotforget.objects.GroupListAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    public IFragmentsEventListener iFragmentsEventListener;
    public ExpandableListView lvGroups;
//    private ArrayAdapter<String> adapter;
    GroupListAdapter adapter;
    public String[] strGroups;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iFragmentsEventListener = (IFragmentsEventListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement IFragmentsEventListener");
        }
    }

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, null);
        lvGroups = (ExpandableListView) view.findViewById(R.id.lvGroups);

        lvGroups.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        //adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_single_choice,strGroups);
        lvGroups.setAdapter(adapter);

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(),R.anim.list_layout_controller);
        lvGroups.setLayoutAnimation(controller);

        return view;
    }

}
