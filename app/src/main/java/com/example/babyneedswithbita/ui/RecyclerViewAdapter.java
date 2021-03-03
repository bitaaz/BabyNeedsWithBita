package com.example.babyneedswithbita.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babyneedswithbita.BabyNeedsList;
import com.example.babyneedswithbita.BabyNeedsStartPage;
import com.example.babyneedswithbita.CreateAccountActivity;
import com.example.babyneedswithbita.R;
import com.example.babyneedswithbita.model.BabyNeedItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final int GALLERY_CODE = 1;

    private Context context;
    private List<BabyNeedItem> babyNeedItems;

    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Needs");

    public RecyclerViewAdapter(Context context, List<BabyNeedItem> babyNeedItems) {
        this.context = context;
        this.babyNeedItems = babyNeedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_row, parent, false);


        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String imageUrl;

        BabyNeedItem babyNeedItem = babyNeedItems.get(position);

        imageUrl = babyNeedItem.getImageUrl();

        holder.listName.setText(babyNeedItem.getItemName());
        holder.listQuantity.setText(String.valueOf(babyNeedItem.getItemQuantity()));
        holder.listColor.setText(babyNeedItem.getItemColor());
        holder.listSize.setText(String.valueOf(babyNeedItem.getItemSize()));

        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(babyNeedItem.getDateCreated().getSeconds()*1000);
        holder.dateCreated.setText(timeAgo);

        Picasso.get().load(imageUrl).into(holder.listImage);


    }

    @Override
    public int getItemCount() {
        return babyNeedItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView listImage;
        private TextView listName;
        private TextView listQuantity;
        private TextView listSize;
        private TextView listColor;
        private TextView dateCreated;
        private Button editButton;
        private Button deleteButton;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            listImage = itemView.findViewById(R.id.item_image_list);
            listName = itemView.findViewById(R.id.item_name_list);
            listQuantity = itemView.findViewById(R.id.item_quantity_list);
            listSize = itemView.findViewById(R.id.item_size_list);
            listColor = itemView.findViewById(R.id.item_color_list);
            dateCreated = itemView.findViewById(R.id.date_created_list);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);


            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {

            int position;
            position = getAdapterPosition();

            BabyNeedItem babyNeedItem = babyNeedItems.get(position);


            switch (v.getId()){

                case R.id.edit_button:

                    editItem(babyNeedItem);
                    break;

                case R.id.delete_button:

                    deleteItem(babyNeedItem);
                    break;

            }

        }
    }

    private void deleteItem(BabyNeedItem babyNeedItem) {

        builder = new AlertDialog.Builder(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.confirmation_popup, null);

        Button yesButton, noButton;
        ProgressBar progressBar;

        yesButton = view.findViewById(R.id.yes_button);
        noButton = view.findViewById(R.id.no_button);
        progressBar = view.findViewById(R.id.confirmation_popup_progressbar);

        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItemFromDB(babyNeedItem, progressBar);
            }
        });


    }

    private void deleteItemFromDB(BabyNeedItem babyNeedItem, ProgressBar progressBar) {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, babyNeedItems);
        RecyclerView recyclerView ;

        View view = LayoutInflater.from(context).inflate(R.layout.activity_baby_needs_list, null);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        final String[] userId = new String[1];

//        progressBar.setVisibility(View.VISIBLE);


        collectionReference.whereEqualTo("dateCreated", babyNeedItem.getDateCreated())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){

                            for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots){
                                userId[0] = snapshot.getString("userId");
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        collectionReference.whereEqualTo("dateCreated", babyNeedItem.getDateCreated())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()){

                            for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots){
                                babyNeedItems.remove(babyNeedItem);
                                collectionReference.document(snapshot.getId()).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Intent intent = new Intent(context, BabyNeedsList.class);
                                                intent.putExtra("userId", userId[0]);
                                                context.startActivity(intent);
                                                recyclerView.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void editItem(BabyNeedItem babyNeedItem) {

        builder = new AlertDialog.Builder(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.popup, null);


        ImageView itemImage, cameraImage;
        EditText itemName, itemQuantity, itemColor, itemSize;
        Button updateButton;
        ProgressBar progressBar;
        String imageUrl;

        itemImage = view.findViewById(R.id.item_image);
        cameraImage = view.findViewById(R.id.camera_image);
        itemName = view.findViewById(R.id.item_name);
        itemQuantity = view.findViewById(R.id.item_quantity);
        itemColor = view.findViewById(R.id.item_Color);
        itemSize = view.findViewById(R.id.item_size);
        progressBar = view.findViewById(R.id.popup_progressbar);
        updateButton = view.findViewById(R.id.save_button);

        itemName.setText(babyNeedItem.getItemName());
        itemQuantity.setText(String.valueOf(babyNeedItem.getItemQuantity()));
        itemColor.setText(babyNeedItem.getItemColor());
        itemSize.setText(String.valueOf(babyNeedItem.getItemSize()));

        imageUrl = babyNeedItem.getImageUrl();

        Picasso.get().load(imageUrl).into(itemImage);


        builder.setView(view);
        dialog = builder.create();
        dialog.show();


        updateButton.setText("Update");

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateItem(itemName, itemQuantity, itemColor, itemSize, progressBar, babyNeedItem);

            }
        });




    }

    private void updateItem(EditText itemName, EditText itemQuantity, EditText itemColor, EditText itemSize,
                        ProgressBar progressBar, BabyNeedItem babyNeedItem) {

        String name = itemName.getText().toString().trim();
        int quantity = Integer.parseInt(itemQuantity.getText().toString().trim());
        String color = itemColor.getText().toString().trim();
        int size = Integer.parseInt(itemSize.getText().toString().trim());

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, babyNeedItems);
        RecyclerView recyclerView ;

        View view = LayoutInflater.from(context).inflate(R.layout.activity_baby_needs_list, null);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(itemQuantity.getText().toString().trim())
                && !TextUtils.isEmpty(color) && !TextUtils.isEmpty(itemSize.getText().toString().trim())){

            collectionReference.whereEqualTo("dateCreated", babyNeedItem.getDateCreated())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {



                            if (!queryDocumentSnapshots.isEmpty()){
                                for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots){

                                    BabyNeedItem babyNeedItem = snapshot.toObject(BabyNeedItem.class);
                                    babyNeedItem.setItemName(name);
                                    babyNeedItem.setItemQuantity(quantity);
                                    babyNeedItem.setItemColor(color);
                                    babyNeedItem.setItemSize(size);
                                    babyNeedItems.add(babyNeedItem);

                                    collectionReference.document(snapshot.getId()).set(babyNeedItem)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    collectionReference.whereEqualTo("dateCreated", babyNeedItem.getDateCreated())
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    if (!queryDocumentSnapshots.isEmpty()){
                                                                        for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots){

                                                                            String userId = snapshot.getString("userId");
                                                                            Intent intent = new Intent(context, BabyNeedsList.class);
                                                                            intent.putExtra("userId", userId);
                                                                            context.startActivity(intent);



                                                                        }


                                                                        recyclerView.setAdapter(adapter);
                                                                        adapter.notifyDataSetChanged();


                                                                    }


                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {


                                                        }
                                                    });


                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });


                                }






                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });







        }else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(context, "Empty Fields Does Not Allowed!", Toast.LENGTH_SHORT).show();
        }


    }


}
