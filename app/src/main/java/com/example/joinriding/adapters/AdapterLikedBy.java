package com.example.joinriding.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.ChatUsersActivity;
import com.example.joinriding.MyListUsersActivity;
import com.example.joinriding.OwnProfileActivity;
import com.example.joinriding.R;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterLikedBy extends RecyclerView.Adapter<AdapterLikedBy.likedByHolder> {
    // Declare
    private Context context;
    private List<ModelMyListUsers> usersList;
    private String myUid;

    // Constructor
    public AdapterLikedBy(Context context, List<ModelMyListUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getUid();
    }

    @NonNull
    @Override
    public likedByHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout row_my_list_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_liked_by, parent, false);

        return new likedByHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull likedByHolder holder, final int position) {
        // Get Data For Show On RecyclerView
        final String hisUID = usersList.get(position).getUid();
        String userPhoto = usersList.get(position).getPhotoUser();
        String userName = usersList.get(position).getNameUser();
        String userLocation = usersList.get(position).getAddressUser();

        // Set Data
        holder.mNameTv.setText(userName);
        try {
            Picasso.get().load(userPhoto)
                    .placeholder(R.drawable.icon_user_grey)
                    .into(holder.mImageIv);
        }
        catch (Exception e){

        }
    }

    private void blockOrNot(final String hisUID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if (snapshot.exists()){
                                Toast.makeText(context, "You Is Blocked...", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        // not blocked
                        Intent intentChat = new Intent(context, ChatUsersActivity.class);
                        intentChat.putExtra("hisUid", hisUID);
                        context.startActivity(intentChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    // View Holder Class
    class likedByHolder extends RecyclerView.ViewHolder{
        ImageView mImageIv;
        TextView mNameTv, mLocationTv;

        likedByHolder(View itemView){
            super(itemView);

            // Init View row_my_list_userist_user.xml
            mImageIv = itemView.findViewById(R.id.photoLikedByUser);
            mNameTv = itemView.findViewById(R.id.nameLikedByUser);
            mLocationTv = itemView.findViewById(R.id.locationLikedByUser);
        }
    }
}