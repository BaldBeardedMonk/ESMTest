package com.flyingantsstudios.esmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.AsyncQueryHandler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AddContact extends AppCompatActivity {

    String myContactLink;
    EditText contactLink;
    TextView errorTextView;

    DatabaseReference dref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        setTitle("Add Contact");
        contactLink = (EditText) findViewById(R.id.contactLinkEditText);
        errorTextView = (TextView) findViewById(R.id.errorTextView);

        setTitle("Add Contact");

        myContactLink = "esm://" + FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView sendLink = findViewById(R.id.sendLinkTextview);
        sendLink.setText(myContactLink);
    }

    public void onCopyClick(View view)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("userLink", myContactLink);
        clipboard.setPrimaryClip(clip);
    }

    public void onShareClick(View view)
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = myContactLink;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ESM contact link");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void onPasteClick(View view)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";
        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        pasteData = item.getText().toString();
        contactLink.setText(pasteData);
    }

    public void onContinueClick(View view) {
        if (contactLink.getText().toString().equals(""))
            errorTextView.setText("Please enter a link");
        else if (contactLink.getText().toString().equals(myContactLink))
            errorTextView.setText("Enter your contact's link, not your own");
        else if (!contactLink.getText().toString().substring(0, 6).equals("esm://") || contactLink.getText().length() != 34)
            errorTextView.setText("Invalid link provided");
        else {
            errorTextView.setText("");
            String contactUserid = contactLink.getText().toString().substring(6, contactLink.getText().length());
            Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(contactUserid);
            query.addListenerForSingleValueEvent(valueEventListener);
        }
    }
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        Users users = dataSnapshot.getValue(Users.class);
                        CreateContactEntry(users);
                        Toast.makeText(AddContact.this, "Contact added", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainChatWindow.class);
                        intent.putExtra("UserName",users.getDisplayName() );
                        intent.putExtra("UserId",users.getUid());
                        startActivity(intent);
                    }
                }
                else Log.w("warning","No matching user found");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("error",error.toString());
            }
        };


    void CreateContactEntry(final Users users)
    {
        dref = FirebaseDatabase.getInstance().getReference().child("Contacts");
        Contacts contacts = new Contacts();
        contacts.setUid1(FirebaseAuth.getInstance().getCurrentUser().getUid());
        contacts.setUid2(users.getUid());
        contacts.setDisplayName(users.getDisplayName());
        contacts.setStatus(1);
        dref.push().setValue(contacts);

        class SavteLocalData extends AsyncTask<Void,Void,Void>
        {
            @Override
            protected Void doInBackground(Void... voids) {
                ContactList contactList = new ContactList();
                contactList.uid1 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                contactList.uid2 = users.getUid();
                contactList.status = 1;
                contactList.displayname = users.getDisplayName();
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().contactListDao().insert(contactList);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                Log.i("msg","Record saved locally");
            }
        }
        SavteLocalData saveData = new SavteLocalData();
        saveData.execute();
    }
}