package com.example.joinriding.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.R;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterUserAdd extends RecyclerView.Adapter<AdapterUserAdd.MyGroupAddUserHolder>{
    private Context context;
    private ArrayList<ModelMyListUsers> modelUsersList;

    private String groupId, myGroupRole;

    public AdapterUserAdd(Context context, ArrayList<ModelMyListUsers> modelUsersList, String groupId, String myGroupRole) {
        this.context = context;
        this.modelUsersList = modelUsersList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public MyGroupAddUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_user_add, parent, false);
        return new MyGroupAddUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupAddUserHolder holder, int position) {
        // get data
        final ModelMyListUsers modelUsers = modelUsersList.get(position);
        String name = modelUsers.getNameUser();
        String image = modelUsers.getPhotoUser();
        final String uid = modelUsers.getUid();

        // set data
        holder.nameUser.setText(name);
        try{
            Picasso.get().load(image).placeholder(R.drawable.icon_user_grey).into(holder.imageUser);
        }catch(Exception e){
            holder.imageUser.setImageResource(R.drawable.icon_user_grey);
        }

        checkReadyExist(modelUsers, holder);

        // handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
                reference.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    // user exist
                                    String hisPreviousRole = ""+dataSnapshot.child("role").getValue();

                                    // option to display dialog
                                    String [] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){
                                            // iam creator, he is admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // handle item click
                                                    if (i == 0){
                                                        // remove admin clicked
                                                        removeAdmin(modelUsers);
                                                    }else{
                                                        removeUser(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            // iam creator, he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // handle item click
                                                    if (i == 0){
                                                        // remove admin clicked
                                                        makeAdmin(modelUsers);
                                                    }else{
                                                        removeUser(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }else if(myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){
                                            Toast.makeText(context, "Creator Of Group...", Toast.LENGTH_SHORT).show();
                                        }
                                        else if(hisPreviousRole.equals("admin")){
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // handle item click
                                                    if (i == 0){
                                                        // remove admin clicked
                                                        makeAdmin(modelUsers);
                                                    }else{
                                                        removeUser(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // handle item click
                                                    if (i == 0){
                                                        // remove admin clicked
                                                        makeAdmin(modelUsers);
                                                    }else{
                                                        removeUser(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }

                                }else{
                                    // users dosn't exist participant
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participants")
                                            .setMessage("Add this user to the group")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    // add user to group
                                                    addUser(modelUsers);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void addUser(ModelMyListUsers modelUsers) {
        // set user data
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", modelUsers.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp",""+timestamp);
        // add that user in Groups>groupId>Participants
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added Success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(ModelMyListUsers modelUsers) {
        // set data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        // update role in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "This User Now Admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeUser(ModelMyListUsers modelUsers) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void removeAdmin(ModelMyListUsers modelUsers) {
        // set data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");
        // update role in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "This User Not Longer Admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkReadyExist(ModelMyListUsers modelUsers, final MyGroupAddUserHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants").child(modelUsers.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String hisRole = ""+dataSnapshot.child("role").getValue();
                            holder.statusUser.setText(hisRole);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelUsersList.size();
    }

    class MyGroupAddUserHolder extends RecyclerView.ViewHolder{

        private ImageView imageUser;
        private TextView nameUser, statusUser;

        public MyGroupAddUserHolder(@NonNull View itemView) {
            super(itemView);

            imageUser = itemView.findViewById(R.id.avatarUserList);
            nameUser = itemView.findViewById(R.id.txtName);
            statusUser = itemView.findViewById(R.id.txtStatus);
        }
    }
}
