package com.flyingantsstudios.esmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.virgilsecurity.android.ethree.kotlin.interaction.EThree;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat extends AppCompatActivity {

    private EditText messageEditText;
    private ListView messagesListView;
    private DatabaseReference dref;
    private String messageText;
    private String user2Name;
    private String senderUid,receiverUid;
    EThree ethree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        user2Name = intent.getStringExtra("displayname");
        setTitle(user2Name);
        messageEditText = findViewById(R.id.editTextMessage);
        messagesListView = findViewById(R.id.messagesListView);
        getReceiverUid();

        dref = FirebaseDatabase.getInstance().getReference("Messages");

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void getReceiverUid()
    {
        class getUid extends AsyncTask<Void,Void, List<ContactList>>
        {
            @Override
            protected List<ContactList> doInBackground(Void... voids) {
                List<ContactList> contactLists = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().contactListDao()
                        .contact1(FirebaseAuth.getInstance().getCurrentUser().getUid());
                return contactLists;
            }
            @Override
            protected void onPostExecute(List<ContactList> contactLists) {
                super.onPostExecute(contactLists);
                receiverUid=contactLists.get(0).uid2;
            }
        }
        getUid contacts = new getUid();
        contacts.execute();
    }

    public void onSendClick()
    {
        //messageText = messageEditText.getText().toString();
        //ethree.findUsers(user2Name).addCallback(findUsersListener);
    }
}