package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FullScreenImagePostActivity extends AppCompatActivity {
    private ImageView imgFullScreen;
    private String idPosting, fullScreenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_post);
        getSupportActionBar().hide();

        imgFullScreen = findViewById(R.id.imageViewFullScreen);

        // Get Data From Previous Activity
        Intent myIntent = getIntent();
        idPosting = ""+myIntent.getStringExtra("idPosting");

        loadImagePostFull(idPosting);

    }

    private void loadImagePostFull(String idPosting) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        // get detail post using id of post
        Query query= reference.orderByChild("idPosting").equalTo(idPosting);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String photoPost = ""+snapshot.child("photoPosting").getValue();

                    // set image
                    Picasso.get().load(photoPost).into(imgFullScreen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
