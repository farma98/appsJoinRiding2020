package com.example.joinriding.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.R;
import com.example.joinriding.models.ModelChatUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdapterChatsUsers extends RecyclerView.Adapter<AdapterChatsUsers.MyChatUsersHolder>{
    // Declare
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<ModelChatUsers> modelChatUsersList;
    private String imageUrlChat;
    // Declare Firebase
    private FirebaseUser fuser;

    // Constructor
    public AdapterChatsUsers(Context context, List<ModelChatUsers> modelChatUsersList, String imageUrlChat) {
        this.context = context;
        this.modelChatUsersList = modelChatUsersList;
        this.imageUrlChat = imageUrlChat;
    }

    @NonNull
    @Override
    public MyChatUsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row chat xml
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyChatUsersHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyChatUsersHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyChatUsersHolder holder, final int position) {
        // get data
        String message = modelChatUsersList.get(position).getMessage();
        String timeStamp = modelChatUsersList.get(position).getTimestamp();
        String type = modelChatUsersList.get(position).getType();

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

        if (type.equals("text")){
            // text message
            holder.messageChat.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageChat.setText(message);
        }else{
            // image message
            holder.messageChat.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.icon_user_grey).into(holder.messageImage);
        }

        // set data
        holder.messageChat.setText(message);
        holder.timeChat.setText(dateTime);
        try{
            Picasso.get().load(imageUrlChat).into(holder.profileChat);
        }
        catch (Exception e){}

        // Handle Show Delete Dialog Click
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Message");
                builder.setMessage("Are You Sure Delete Message ? ");
                // delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
                    }
                });
                // cancel button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                // create and show dialog
                builder.create().show();
            }
        });

        // Set Seen Or Not  Message Chat
        if (position == modelChatUsersList.size()-1){
            if (modelChatUsersList.get(position).isSeen()){
                holder.isSeenChat.setImageResource(R.drawable.icon_check_seen_pink);
            }else{
                holder.isSeenChat.setImageResource(R.drawable.icon_check_send_grey);
            }
        }else{
        holder.isSeenChat.setVisibility(View.GONE);
    }
}

    private void deleteMessage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp = modelChatUsersList.get(position).getTimestamp();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_CHATS");
        Query query = reference.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("sender").getValue().equals(myUID)){
                        // 1). Remoce message from chat
                         snapshot.getRef().removeValue();

                        // 2). Set value message "this message was deleted"
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("message", "This Message Was Deleted...");
//                        snapshot.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "You Can Only Delete Yout Messages...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (modelChatUsersList.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return modelChatUsersList.size();
    }

    // View Holder Class
    class MyChatUsersHolder extends RecyclerView.ViewHolder{
        // Declare View
        ImageView profileChat, messageImage, isSeenChat;
        TextView messageChat, timeChat;
        LinearLayout messageLayout;

        public MyChatUsersHolder(View itemView){
            super(itemView);

            // init view
            profileChat = itemView.findViewById(R.id.photoUserChat);
            messageChat = itemView.findViewById(R.id.messageUserChat);
            messageImage = itemView.findViewById(R.id.messageImage);
            timeChat = itemView.findViewById(R.id.timeUserChat);
            isSeenChat = itemView.findViewById(R.id.seenUserChat);
            messageLayout = itemView.findViewById(R.id.massageLayout);
        }
    }
}
