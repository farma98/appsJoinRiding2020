package com.example.joinriding.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.ChatUsersActivity;
import com.example.joinriding.R;
import com.example.joinriding.models.ModelMyListUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatsList extends RecyclerView.Adapter<AdapterChatsList.MyChatsListHolder>{
    // Declare
    private Context context;
    private List<ModelMyListUsers> modelMyListUsers;
    private HashMap<String, String> lastMessageMap;

    // Constructor
    public AdapterChatsList(Context context, List<ModelMyListUsers> modelMyListUsers) {
        this.context = context;
        this.modelMyListUsers = modelMyListUsers;
        this.lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyChatsListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chat_list_user, parent, false);
        return new MyChatsListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyChatsListHolder holder, final int position) {
        // Get Data
        final String hisUid = modelMyListUsers.get(position).getUid();
        String userImage = modelMyListUsers.get(position).getPhotoUser();
        String userName = modelMyListUsers.get(position).getNameUser();
        String lastMessage = lastMessageMap.get(hisUid);

        // set data
        holder.nameUserList.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageQ.setVisibility(View.GONE);
        }else{
            holder.lastMessageQ.setVisibility(View.VISIBLE);
            holder.lastMessageQ.setText(lastMessage);
        }

        try{
            Picasso.get().load(userImage).placeholder(R.drawable.icon_user_grey).into(holder.profileUserList);
        }catch (Exception e){
            Picasso.get().load(R.drawable.icon_user_grey).into(holder.profileUserList);
        }

        // set online offline status
        if (modelMyListUsers.get(position).getOnlineStatus().equals("online")){
            holder.onlineStatus.setImageResource(R.drawable.circle_online);
        }else{
            holder.onlineStatus.setImageResource(R.drawable.circle_offline);
        }

        // Handle Long Press
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
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

                return false;
            }
        });

        // Handle Recycler View Click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start chat activity with that user
                Intent myIntent = new Intent(context, ChatUsersActivity.class);
                myIntent.putExtra("hisUid", hisUid);
                context.startActivity(myIntent);
            }
        });
    }

    private void deleteMessage(int position) {

    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return modelMyListUsers.size();
    }

    // Class Holder View
    class MyChatsListHolder extends RecyclerView.ViewHolder{
        // view row_chatlist.xml
        ImageView profileUserList, onlineStatus;
        TextView nameUserList, lastMessageQ;

        public MyChatsListHolder(@NonNull View itemView) {
            super(itemView);

            // init view
            profileUserList = itemView.findViewById(R.id.photoUserChatList);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            nameUserList = itemView.findViewById(R.id.nameUserChatList);
            lastMessageQ = itemView.findViewById(R.id.lastMessageChatList);
        }
    }
}
