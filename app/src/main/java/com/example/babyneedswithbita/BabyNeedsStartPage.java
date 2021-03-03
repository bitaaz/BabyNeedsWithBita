package com.example.babyneedswithbita;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.babyneedswithbita.model.BabyNeedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Date;
import java.util.Objects;

public class BabyNeedsStartPage extends AppCompatActivity {

    private static final String TAG ="BabyNeedsStartPage";
    private static final int GALLERY_CODE = 1;
    private ImageView itemImage, cameraImage;
    private EditText itemName, itemQuantity, itemColor, itemSize;
    private Button saveButton;
    private ProgressBar progressBar;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private String userId;
    private String userName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private Uri imageUri;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Needs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_needs_start_page);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            userId = bundle.getString("userId");
            userName = bundle.getString("username");

        }

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createPopupDialog();
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();

                if (firebaseUser != null){

                }else {

                }

            }
        };
    }

    private void createPopupDialog() {

        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup, null);

        itemImage = view.findViewById(R.id.item_image);
        cameraImage = view.findViewById(R.id.camera_image);
        itemName = view.findViewById(R.id.item_name);
        itemQuantity = view.findViewById(R.id.item_quantity);
        itemColor = view.findViewById(R.id.item_Color);
        itemSize = view.findViewById(R.id.item_size);
        progressBar = view.findViewById(R.id.popup_progressbar);
        saveButton = view.findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveItem();

            }
        });

        cameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

            }
        });

        builder.setView(view);
        dialog = builder.create();
        dialog.show();

    }

    private void saveItem() {

        String name = itemName.getText().toString().trim();
        int quantity = Integer.parseInt(itemQuantity.getText().toString().trim());
        String color = itemColor.getText().toString().trim();
        int size = Integer.parseInt(itemSize.getText().toString().trim());

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(itemQuantity.getText().toString().trim())
            && !TextUtils.isEmpty(color) && !TextUtils.isEmpty(itemSize.getText().toString().trim())
            && imageUri != null){


            StorageReference filePath = storageReference
                    .child("baby_needs_images")
                    .child("my_image" + Timestamp.now().getSeconds());

            filePath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressBar.setVisibility(View.INVISIBLE);

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    BabyNeedItem babyNeedItem = new BabyNeedItem();
                                    babyNeedItem.setItemName(name);
                                    babyNeedItem.setItemQuantity(quantity);
                                    babyNeedItem.setItemSize(size);
                                    babyNeedItem.setItemColor(color);
                                    babyNeedItem.setImageUrl(uri.toString());
                                    babyNeedItem.setUserId(userId);
                                    babyNeedItem.setDateCreated(new Timestamp(new Date()));

                                    collectionReference.add(babyNeedItem)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Intent intent = new Intent(BabyNeedsStartPage.this, BabyNeedsList.class);
                                                    intent.putExtra("userId", userId);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.getMessage());

                                        }
                                    });

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "onFailure: " + e.getMessage());

                }
            });


        }else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Empty Fields Does Not Allowed!", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            if (data != null){
                imageUri = data.getData();
                itemImage.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseAuth !=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}