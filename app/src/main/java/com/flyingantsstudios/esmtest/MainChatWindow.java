package com.flyingantsstudios.esmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainChatWindow extends AppCompatActivity {
    ListView contactsListView;
    Intent intent;
    DatabaseReference dref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Contacts");
        setContentView(R.layout.activity_main_chat_window);
        contactsListView = (ListView) findViewById(R.id.contactsListView);
        intent = getIntent();
        if(intent!=null && intent.getExtras()!=null) ShowAddedContactInList();
        getContacts1();

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                String value = item.get("First Line");
                String stat = item.get("Second Line");
                if(stat.contains("Click")) {
                    onContactAccepted(value);
                    updateContactsFirebase(value);
                }
                else if (stat.contains("Waiting"))
                {
                    //checkParseContacts(value);
                }
                else if(stat.contains("chat")){
                    Intent intent = new Intent(getApplicationContext(), Chat.class);
                    intent.putExtra("displayname",value);
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        /*Query query = FirebaseDatabase.getInstance().getReference("Contacts").orderByChild("uid2")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Query query2 = FirebaseDatabase.getInstance().getReference("Contacts").orderByChild("uid1")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());*/
        DatabaseReference dref1 = FirebaseDatabase.getInstance().getReference("Contacts");
        dref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        Contacts contacts = dataSnapshot.getValue(Contacts.class);
                        if((contacts.getStatus()==1) && (contacts.getUid2().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())))
                        {
                            String key = dataSnapshot.getKey();
                            Log.i("key", "The key value is "+ key );
                            dref = FirebaseDatabase.getInstance().getReference();
                            dref.child("Contacts").child(key).child("status").setValue(4);
                            CreateContactEntry(contacts);
                        }
                        if((contacts.getStatus()==2) && (contacts.getUid1().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())))
                        {
                            onContactAccepted(contacts.getDisplayName());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    /* Valuelistener to check for new contact requests */
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Contacts contacts = dataSnapshot.getValue(Contacts.class);
                    if(contacts.getStatus()==1)
                    {
                        String key = dataSnapshot.getKey();
                        Log.i("key", "The key value is "+ key );
                        dref = FirebaseDatabase.getInstance().getReference();
                        dref.child("Contacts").child(key).child("status").setValue(4);
                        CreateContactEntry(contacts);
                    }
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    ValueEventListener query2ValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };



    /* When Add contact button is clicked */
    public void onAddContactClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(),AddContact.class);
        startActivity(intent);
    }

    /* Show the recently added contact in the contact list view*/
    void ShowAddedContactInList()
    {
        String username = intent.getStringExtra("UserName");
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> datum = new HashMap<String, String>(2);
        datum.put("First Line", username);
        datum.put("Second Line","Waiting for contact to accept");
        data.add(datum);
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"First Line", "Second Line" },
                new int[] {android.R.id.text1, android.R.id.text2 });
        contactsListView.setAdapter(adapter);
    }

    /* Get all contacts from local database and show them in contact list view*/
    void getContacts1()
    {
        class GetContacts extends AsyncTask<Void,Void, List<ContactList>>
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

                String username;
                int status;
                List<Map<String, String>> data = new ArrayList<Map<String, String>>();
                for(int i=0;i<contactLists.size();i++)
                {
                    username=contactLists.get(i).displayname;
                    status=contactLists.get(i).status;
                    Map<String, String> datum = new HashMap<String, String>(2);
                    datum.put("First Line", username);
                    if(status==2)   datum.put("Second Line","Tap to chat");
                    else if(status==3)   datum.put("Second Line","Click to accept connection request");
                    else            datum.put("Second Line","Waiting for contact to accept.");
                    data.add(datum);
                }
                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), data,
                        android.R.layout.simple_list_item_2,
                        new String[] {"First Line", "Second Line" },
                        new int[] {android.R.id.text1, android.R.id.text2 });
                contactsListView.setAdapter(adapter);
            }
        }
        GetContacts contacts = new GetContacts();
        contacts.execute();
    }

    /* Create a local database entry when contact request is sent from other user */
    void CreateContactEntry(final Contacts contacts)
    {
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid")
                .equalTo(contacts.getUid1());
        query.addListenerForSingleValueEvent(userValueEventListener);

    }
    /*Listener to get result of the query to find display name from user table */
    ValueEventListener userValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    final Users users = dataSnapshot.getValue(Users.class);

                    class SaveLocalData extends AsyncTask<Void,Void,Void> {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            ContactList contactList = new ContactList();
                            contactList.uid1 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            contactList.uid2 = users.getUid();
                            contactList.status = 3;
                            contactList.displayname=users.getDisplayName();
                            DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().contactListDao().insert(contactList);
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void avoid) {
                            super.onPostExecute(avoid);
                            Log.i("msg", "Record saved locally with status 3");
                            getContacts1();
                        }
                    }
                    SaveLocalData saveData = new SaveLocalData();
                    saveData.execute();
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };


    /* Change status to 2 in local database When list item is clicked to accept contact request */
    void onContactAccepted(final String displayname)
    {
        class ChangeContactStatus extends AsyncTask<Void,Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().contactListDao()
                        .updateStatus(displayname);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getContacts1();
            }
        }
        ChangeContactStatus changeStatus = new ChangeContactStatus();
        changeStatus.execute();
    }

    void updateContactsFirebase(final String displayname)
    {
        class GetUserId extends AsyncTask<Void,Void, String> {
            @Override
            protected String doInBackground(Void... voids) {
                String userId=DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().contactListDao()
                        .getUserId(displayname);
                return userId;
            }
            @Override
            protected void onPostExecute(String userId) {
                super.onPostExecute(userId);
                Query query = FirebaseDatabase.getInstance().getReference("Contacts").orderByChild("uid1")
                        .equalTo(userId);
                query.addListenerForSingleValueEvent(valueEventListener2);
            }
        }
        GetUserId changeStatus = new GetUserId();
        changeStatus.execute();
    }

    ValueEventListener valueEventListener2 = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Contacts contacts = dataSnapshot.getValue(Contacts.class);
                    if(contacts.getUid2().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    {
                        String key = dataSnapshot.getKey();
                        Log.i("key", "The key value is "+ key );
                        dref = FirebaseDatabase.getInstance().getReference();
                        dref.child("Contacts").child(key).child("status").setValue(2);
                    }
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };


}