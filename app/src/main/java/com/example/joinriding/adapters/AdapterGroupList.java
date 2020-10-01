package com.example.joinriding.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.ChatGroupActivity;
import com.example.joinriding.R;
import com.example.joinriding.models.ModelGroupList;
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

public class AdapterGroupList extends RecyclerView.Adapter<AdapterGroupList.MyGroupListHolder>{
    private Context context;
    private ArrayList<ModelGroupList> GroupLists;

    // Constructor
    public AdapterGroupList(Context context, ArrayList<ModelGroupList> GroupLists) {
        this.context = context;
        this.GroupLists = GroupLists;
    }

    @NonNull
    @Override
    public MyGroupListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_list, parent, false);

        return new MyGroupListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupListHolder holder, int position) {
        // get data
        ModelGroupList modelGroupList = GroupLists.get(position);
        final String groupId = modelGroupList.getIdGroup();
        String groupIcon = modelGroupList.getPhotoGroup();
        String groupName = modelGroupList.getNameGroup();
        String groupTime = modelGroupList.getTimeGroup();

        holder.groupNameQ.setText("");
        holder.groupTimeQ.setText("");
        holder.groupMessageQ.setText("");

        // load last message and message-time
        loadLastMessage(modelGroupList, holder);

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(groupTime));
        String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

        // set data
        holder.groupNameQ.setText(groupName);
        holder.groupTimeQ.setText(dateTime);
        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.icon_group_grey).into(holder.groupIconQ);
        }catch (Exception e){
            holder.groupIconQ.setImageResource(R.drawable.icon_group_grey);
        }

        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.icon_group_grey).into(holder.groupIconQ);
        }catch (Exception e){
            holder.groupIconQ.setImageResource(R.drawable.icon_group_grey);
        }

        // handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open group chat
                Intent myIntent = new Intent(context, ChatGroupActivity.class);
                myIntent.putExtra("idGroup", groupId);
                context.startActivity(myIntent);
            }
        });
    }

    private void loadLastMessage(ModelGroupList modelGroupList, final MyGroupListHolder holder) {
        // get laslast message from group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(modelGroupList.getIdGroup()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            // get data
                            String message = ""+snapshot.child("message").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();
                            String sender = ""+snapshot.child("sender").getValue();
                            String messageType = ""+snapshot.child("type").getValue();

                            // convert time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

                            if (messageType.equals("image")){
                                holder.groupMessageQ.setText("Send Photo");
                            }else{
                                holder.groupMessageQ.setText(message);
                            }
                            holder.groupMessageQ.setText(message);
                            holder.groupTimeQ.setText(dateTime);

                            // get info of sender of last message
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("TBL_USERS");
                            reference1.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                                String name = ""+snapshot1.child("nameUser").getValue();
                                                holder.groupNameUserQ.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return GroupLists.size();
    }

    // class view holder
    class MyGroupListHolder extends RecyclerView.ViewHolder{
        // views
        private ImageView groupIconQ;
        private TextView groupNameQ, groupNameUserQ, groupMessageQ, groupTimeQ;

        public MyGroupListHolder(@NonNull View itemView) {
            super(itemView);

            groupIconQ = itemView.findViewById(R.id.groupIconTv);
            groupNameUserQ = itemView.findViewById(R.id.groupNameUserTv);
            groupNameQ = itemView.findViewById(R.id.groupNameTv);
            groupMessageQ = itemView.findViewById(R.id.groupMessageTv);
            groupTimeQ = itemView.findViewById(R.id.groupTimeTv);
        }
    }
}
