package com.example.joinriding.adapters;

import android.content.Context;
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

public class AdapterMyListUsers extends RecyclerView.Adapter<AdapterMyListUsers.MyUserHolder> {
    // Declare
    private Context context;
    private List<ModelMyListUsers> usersList;
    private String myUid;

    // Constructor
    public AdapterMyListUsers(Context context, List<ModelMyListUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getUid();
    }

    @NonNull
    @Override
    public MyUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout row_my_list_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_my_list_user, parent,
                false);

        return new MyUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyUserHolder holder, final int position) {
        // Get Data For Show On RecyclerView
        final String hisUID = usersList.get(position).getUid();
        String userPhoto = usersList.get(position).getPhotoUser();
        String userName = usersList.get(position).getNameUser();
        String userAddress = usersList.get(position).getAddressUser();

        // Set Data
        holder.mNameTv.setText(userName);
        holder.mAddressTv.setText(userAddress);
        try {
            Picasso.get().load(userPhoto)
                    .placeholder(R.drawable.icon_user_grey)
                    .into(holder.mImageIv);
        }
        catch (Exception e){

        }

        // Handle Block User Click
        holder.mBlockUser.setImageResource(R.drawable.icon_unblock_user_grey);
        // check if user if is blocked or not
        checkIsBlocked(hisUID, holder, position);

        // Handle Button Profile Click
        holder.btnProfileUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(context, OwnProfileActivity.class);
                profileIntent.putExtra("uid", hisUID);
                context.startActivity(profileIntent);
            }
        });

        // Handle RecyclerView Click
        holder.btnChatUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockOrNot(hisUID);
            }
        });

        // click to block unblock user
        holder.mBlockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usersList.get(position).isBlocked()){
                    unBlockedUser(hisUID);
                }else{
                    blockedUser(hisUID);
                }
            }
        });
    }

    private void blockOrNot(final String hisUID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if (snapshot.exists()){
                                Toast.makeText(context, "You Is Blocked...",
                                        Toast.LENGTH_SHORT).show();
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

    private void checkIsBlocked(String hisUID, final MyUserHolder holder, final int position) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if (snapshot.exists()){
                                holder.mBlockUser.setImageResource(R.drawable.icon_block_user_red);
                                usersList.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockedUser(String hisUID) {
        // put value into hashmap
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // block success
                        Toast.makeText(context, "Blocked User Success...", Toast.LENGTH_SHORT)
                                .show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // block failed
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockedUser(String hisUID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if (snapshot.exists()){
                                snapshot.getRef().removeValue()// remove blocked user
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // unblocked success
                                                Toast.makeText(context, "UnBlocked User Success...",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // unblocked failed
                                        Toast.makeText(context, ""+e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
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
    class MyUserHolder extends RecyclerView.ViewHolder{
        ImageView mImageIv, mBlockUser;
        TextView mNameTv, mAddressTv;
        Button btnChatUsers, btnProfileUsers;

        MyUserHolder(View itemView){
            super(itemView);

            // Init View row_my_list_userist_user.xml
            mImageIv = itemView.findViewById(R.id.photoMyListUsers);
            mNameTv = itemView.findViewById(R.id.nameMyListUsers);
            mAddressTv = itemView.findViewById(R.id.locationMyListUsers);
            mBlockUser = itemView.findViewById(R.id.iconBlockUsers);
            btnChatUsers = itemView.findViewById(R.id.buttonChatMyListUsers);
            btnProfileUsers = itemView.findViewById(R.id.buttonProfileMyListUsers);
        }
    }
}
