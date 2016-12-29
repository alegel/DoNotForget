package com.donotforget.user.donotforget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.donotforget.user.donotforget.objects.ContactSchedule;
import com.donotforget.user.donotforget.objects.MyUsefulFuncs;

import java.util.ArrayList;

public class ShowFailedSchedulesActivity extends AppCompatActivity {
    private ListView lvFailedContacts;
    private ArrayAdapter<String> adapter;
    private ArrayList<ContactSchedule> contactSchedules = new ArrayList<>();
    private ArrayList<String> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_failed_schedules);

        lvFailedContacts = (ListView) findViewById(R.id.lvFailedContacts);
        Intent intent = getIntent();
        if(intent.hasExtra(MyUsefulFuncs.CONTACT_SCHEDULES)){
            contactSchedules = (ArrayList<ContactSchedule>) intent.getSerializableExtra(MyUsefulFuncs.CONTACT_SCHEDULES);
            if(contactSchedules != null && contactSchedules.size() > 0){
                for (int i = 0; i < contactSchedules.size(); i++) {
                    StringBuilder myContact = new StringBuilder();
                    myContact.append(contactSchedules.get(i).getContactName());
                    myContact.append("\n");
                    myContact.append(contactSchedules.get(i).getPhone());

                    contacts.add(myContact.toString());
                }

                lvFailedContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,contacts);
                lvFailedContacts.setAdapter(adapter);
                LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(this,R.anim.list_layout_controller);
                lvFailedContacts.setLayoutAnimation(controller);
            }
        }
    }
}
