package com.flyingantsstudios.esmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.ethree.kotlin.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.kotlin.callback.OnResultListener;
import com.virgilsecurity.android.ethree.kotlin.interaction.EThree;

import java.util.Map;
import java.util.UUID;


public class LoginActivity extends AppCompatActivity {
    String userName,email,pwd;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    private EThree eThreeUser;
    private String authToken;
    public  String jwToken;
    private Button okButton;
    DatabaseReference dref;
    Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressBar= (ProgressBar)findViewById(R.id.progressBar);
        okButton = findViewById(R.id.createLoginButton);
    }

    final OnGetTokenCallback tokenCallback = new OnGetTokenCallback() {
        @Override public String onGetToken() {
            Log.i("token", "JWT TOKEN " + jwToken);
            return jwToken;
        }

    };
    final com.virgilsecurity.android.ethree.kotlin.callback.OnCompleteListener registerListener =
            new com.virgilsecurity.android.ethree.kotlin.callback.OnCompleteListener(){
                @Override public void onSuccess() {
                    Log.i("msg","Registered successfully");
                    Intent intent = new Intent(getApplicationContext(),MainChatWindow.class);
                    startActivity(intent);
                }
                @Override public void onError(final Throwable throwable) {
                    Log.e("vrigil error","Error during registration",throwable);
                }
            };

    final OnResultListener<EThree> initializeListener = new OnResultListener<EThree>() {
        @Override public void onSuccess(EThree result) {
            // Init done!
            // Save the eThree instance
            eThreeUser=result;
            eThreeUser.register().addCallback(registerListener);

        }
        @Override public void onError(@NotNull Throwable throwable) {
            Log.e("vrigil error","Error during initialise",throwable);
        }
    };

    public void onOkClick(View view) {
        EditText usern = (EditText) findViewById(R.id.editTextUsername);
        userName = usern.getText().toString();
        if(userName.equals("")) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        email = userName+ts+"@flyingantsstudios.com";
        pwd = UUID.randomUUID().toString();
        progressBar.setVisibility(View.VISIBLE);
        okButton.setEnabled(false);

        authenticate(new OnResultListener<String>() {
            @Override public void onSuccess(String value) {
                authToken=value;
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userName).build();
                currentUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i("username",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                    user= new Users();
                                    dref = FirebaseDatabase.getInstance().getReference().child("Users");
                                    user.setDisplayName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                    user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    dref.push().setValue(user);
                                }
                            }
                        });
                getVirgilJwt();
            }
            @Override
            public void onError(Throwable throwable) {
                Log.e("vrigil error","Error on OK Click",throwable);
                okButton.setEnabled(true);
            }
        });
    }

    private void authenticate(final OnResultListener<String> onResultListener)
    {
        mAuth.createUserWithEmailAndPassword(email,pwd)
        .addOnCompleteListener(LoginActivity.this,new OnCompleteListener<AuthResult>(){
            @Override public void onComplete(@NonNull Task<AuthResult> task){
            if(task.isSuccessful()){
                FirebaseUser user=mAuth.getCurrentUser();

                user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,"Successfully signed in. Please Wait",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            onResultListener.onSuccess(task.getResult().getToken());
                        } else {
                            onResultListener.onError(task.getException());
                        }
                    }
                });
            }
            else{
                Log.w("warning","createUserWithEmail:failure",task.getException());
                Toast.makeText(LoginActivity.this,"Sign in failed",Toast.LENGTH_LONG).show();
            }
            }
        });
    }

   private void getVirgilJwt() {
       //Map<String, String> data; = new HashMap<>();
       FirebaseFunctions.getInstance()
               .getHttpsCallable("getVirgilJwt")
               .call()
               .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                   @Override
                   public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                       Map<String, String> data = (Map<String, String>) task.getResult().getData();
                       if (task.isSuccessful()) {
                           jwToken = data.get("token");

                           EThree.initialize(LoginActivity.this, tokenCallback).addCallback(initializeListener);
                       } else {
                           Log.e("virgil error", "Error getting token from function");
                       }
                   }
               });
   }
}