package com.example.babyneedswithbita;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.babyneedswithbita.model.BabyNeedItem;
import com.example.babyneedswithbita.ui.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BabyNeedsList extends AppCompatActivity {

    private static final String TAG = "BabyNeedsList";
    private static final int GALLERY_CODE = 1;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    private FloatingActionButton fab;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Needs");

    private List<BabyNeedItem> babyNeedItems;
    private String userId;

    private ImageView itemImage, cameraImage;
    private EditText itemName, itemQuantity, itemColor, itemSize;
    private Button saveButton;
    private ProgressBar progressBar;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private Uri imageUri;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_needs_list);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference();



        babyNeedItems = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.floatingActionButton);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            userId = bundle.getString("userId");

        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.sign_out:
                if (firebaseUser != null && firebaseAuth != null){
                    firebaseAuth.signOut();

                    startActivity(new Intent(BabyNeedsList.this, MainActivity.class));
                    finish();
                }
                break;

        }


        return super.onOptionsItemSelected(item);
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
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(BabyNeedsList.this, BabyNeedsList.class);
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
    protected void onStart() {
        super.onStart();

        collectionReference.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots){

                                BabyNeedItem babyNeedItem = snapshot.toObject(BabyNeedItem.class);
                                babyNeedItems.add(babyNeedItem);

                            }

                            adapter = new RecyclerViewAdapter(BabyNeedsList.this, babyNeedItems);

                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();


                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());

            }
        });
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


}