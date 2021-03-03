package com.example.babyneedswithbita;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {


    private static final String TAG = "CreateAccountActivity";
    private ProgressBar progressBar;
    private EditText username;
    private EditText email;
    private EditText password;
    private Button createAccount;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressbar_create_account);
        username = findViewById(R.id.username_create_account);
        email = findViewById(R.id.email_create_account);
        password = findViewById(R.id.password_create_account);
        createAccount = findViewById(R.id.create_button_create_account);


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){

                }
                else {

                }

            }
        };

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (!TextUtils.isEmpty(username.getText()) && !TextUtils.isEmpty(email.getText()) &&
                        !TextUtils.isEmpty(password.getText())){

                    String username_text = username.getText().toString().trim();
                    String email_text = email.getText().toString().trim();
                    String password_text = password.getText().toString().trim();

                    createAccountWithEmailAndPassword(username_text, email_text, password_text);
                }
                else {
                    Toast.makeText(CreateAccountActivity.this, "Empty Fields Not Allowed!", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void createAccountWithEmailAndPassword(String username_text, String email_text, String password_text) {

        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email_text, password_text)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            firebaseUser = firebaseAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            Map<String, Object> userObj = new HashMap<>();
                            userObj.put("userId", userId);
                            userObj.put("username", username_text);

                            collectionReference.add(userObj)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.getResult().exists()){
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        String name = task.getResult().getString("username");

                                                        Intent intent = new Intent(CreateAccountActivity.this, BabyNeedsStartPage.class);
                                                        intent.putExtra("userId", userId);
                                                        intent.putExtra("username", name);
                                                        startActivity(intent);


                                                    }
                                                    else {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }

                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onFailure: " + e.toString());

            }
        });


    }
}