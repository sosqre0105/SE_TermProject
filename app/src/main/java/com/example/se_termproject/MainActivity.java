package com.example.se_termproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ListViewBtnAdapter adapter;
    private ListViewBtnItem item;
    private ArrayList<ListViewBtnItem> items;
    private ListView listview;
    private String subject;
    private Context context;
    private TextView countText;
    private TextView achievementText;


    private float totalTask;
    private float completedTask;
    private float uncompletedTask;
    int percent;

    private Button btnLogout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(getApplicationContext(), MainHistory.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        context = getApplicationContext();
        listview = (ListView) findViewById(R.id.listView);
        countText = (TextView) findViewById(R.id.countText);
        achievementText = (TextView)findViewById(R.id.achievementText);
        btnLogout = (Button) findViewById(R.id.btn_logout);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                finishAffinity();
            }
        });


        if (items == null) {
            items = new ArrayList<>();
        }

        Date date = new Date();

        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();




        //?????? task ?????? ??????
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalTask = snapshot.getChildrenCount();

                percent = (int) cal(totalTask, completedTask);
                achievementText.setText("Achievement percentage: " + percent + "%");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myRef.orderByChild("flag").equalTo("Y").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedTask = snapshot.getChildrenCount();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // N??? ?????? ????????? ?????? Task?????? ?????????
        myRef.orderByChild("flag").equalTo("N").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();

                //??????????????? task ??? ??????
                uncompletedTask = dataSnapshot.getChildrenCount();

                // ????????? Task??? ????????? ?????? Subject??? ???????????? ?????? ??????
                if (dataSnapshot.getChildrenCount() == 0) {
                    item = new ListViewBtnItem();
                    item.setSubject("");
                    items.add(item);

                    adapter = new ListViewBtnAdapter(context, R.layout.before_layout, items);

                    listview.setAdapter(adapter);
                } else {
                    //item??? ????????? Task??? ???????????? items??? ?????? item?????? ??????
                    item = new ListViewBtnItem();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        item = new ListViewBtnItem();

                        //????????????????????? ????????? Subject ????????????
                        item.setSubject(dataSnapshot.child(child.getKey()).child("subject")
                                .getValue(String.class));
                        //????????????????????? ????????? memo ????????????
                        item.setMemo(dataSnapshot.child(child.getKey()).child("memo")
                                .getValue(String.class));
                        //????????????????????? ????????? data ??? ??????(??????, ???, ???) ????????????
                        item.setDate(dataSnapshot.child(child.getKey()).child("date")
                                .getValue(String.class));
                        //????????????????????? ????????? time ??? ??????(???, ???) ????????????
                        item.setTime(dataSnapshot.child(child.getKey()).child("time")
                                .getValue(String.class));
                        //????????????????????? ????????? flag ??? N?????? Y??? ????????????
                        item.setFlag(dataSnapshot.child(child.getKey()).child("flag")
                                .getValue(String.class));
                        item.setPosition(child.getKey());

                        items.add(item);
                    }

                    adapter = new ListViewBtnAdapter(context, R.layout.before_layout, items);

                    listview.setAdapter(adapter);


                    //item?????? ????????? ?????? ??? ?????? ??? Task??? ????????? ??????
                    countText.setText((int)uncompletedTask + " Tasks to Do");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), MainDetail.class);
                intent.putExtra("position", items.get(position).getPosition());
                intent.putExtra("checkFlag", "Main");

                startActivity(intent);
            }
        });



    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public float cal(float total, float complete){
        if(total == 0){
            return 0;
        }
        else if(complete == 0){
            return 0;
        }
        else{
            return (complete/total) * 100;
        }
    }

}

