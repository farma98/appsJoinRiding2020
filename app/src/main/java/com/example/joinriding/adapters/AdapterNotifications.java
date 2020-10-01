package com.example.joinriding.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.PostDetailActivity;
import com.example.joinriding.R;
import com.example.joinriding.models.ModelNotifications;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.MyNotifHolder>{
    private Context context;
    private ArrayList<ModelNotifications> notificationsList;
    private FirebaseAuth mAuth;

    public AdapterNotifications(Context context, ArrayList<ModelNotifications> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;

        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyNotifHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);

        return new MyNotifHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyNotifHolder holder, int position) {
        // Get Set Data
        final ModelNotifications modelNotifications = notificationsList.get(position);
        String name = modelNotifications.getsName();
        String notification = modelNotifications.getNotification();
        String image = modelNotifications.getsImage();
        final String timestamp = modelNotifications.getTimestamp();
        String senderUid = modelNotifications.getsUid();
        final String pId = modelNotifications.getpId();

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

        // get name, email, image user notif from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationsList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String name = ""+snapshot.child("nameUser").getValue();
                            String image = ""+snapshot.child("photoUser").getValue();
                            String email = ""+snapshot.child("emailUser").getValue();

                            // add to mode notif
                            modelNotifications.setsName(name);
                            modelNotifications.setsImage(image);
                            modelNotifications.setsEmail(email);

                            // set to view
                            holder.nameNotif.setText(name);

                            try{
                                Picasso.get().load(image).placeholder(R.drawable.icon_user_grey).into(holder.iconNotif);
                            }catch (Exception e){
                                holder.iconNotif.setImageResource(R.drawable.icon_user_grey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        holder.notificationNotif.setText(notification);
        holder.timeNotif.setText(dateTime);

        // click notif to open posting
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, PostDetailActivity.class);
                myIntent.putExtra("postId", pId);
                context.startActivity(myIntent);
            }
        });

        // long press to delete notif posting
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // show confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Notif");
                builder.setMessage("Do You Sure Delete Notification ?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("TBL_USERS");
                        dbRef.child(mAuth.getUid()).child("Notifications").child(timestamp)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Notification Delete..", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class MyNotifHolder extends RecyclerView.ViewHolder{
        //declare view
        ImageView iconNotif;
        TextView nameNotif, notificationNotif, timeNotif;

        public MyNotifHolder(@NonNull View itemView) {
            super(itemView);

            // init views
            iconNotif = itemView.findViewById(R.id.iconNotification);
            nameNotif = itemView.findViewById(R.id.nameNotification);
            notificationNotif = itemView.findViewById(R.id.notification);
            timeNotif = itemView.findViewById(R.id.timeNotification);
        }
    }
}
