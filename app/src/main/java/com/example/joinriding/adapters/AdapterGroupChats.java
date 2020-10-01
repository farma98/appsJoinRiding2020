package com.example.joinriding.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.R;
import com.example.joinriding.models.ModelGroupChats;
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

public class AdapterGroupChats extends RecyclerView.Adapter<AdapterGroupChats.MyChatGroupHolder>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChats> modelGroupChatsQ;

    private FirebaseAuth fuser;

    // Constractor
    public AdapterGroupChats(Context context, ArrayList<ModelGroupChats> modelGroupChatsQ) {
        this.context = context;
        this.modelGroupChatsQ = modelGroupChatsQ;

        fuser = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyChatGroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row chat xml
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_right, parent, false);
            return new MyChatGroupHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_left, parent, false);
            return new MyChatGroupHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyChatGroupHolder holder, int position) {
        // get data
        ModelGroupChats modelGroupChats = modelGroupChatsQ.get(position);

        String message = modelGroupChats.getMessage();
        String timestamp = modelGroupChats.getTimestamp();
        String senderUid = modelGroupChats.getSender();
        String messageType = modelGroupChats.getType();

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

        // set data
        if (messageType.equals("text")){
            // message text
            holder.messageChat.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageChat.setText(message);
        }else{
            // message image
            holder.messageChat.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(message).placeholder(R.drawable.icon_image_message_grey).into(holder.messageImage);
            }catch (Exception e){
                holder.messageImage.setImageResource(R.drawable.icon_image_message_grey);
            }
        }
        holder.timeChat.setText(dateTime);

        setUserName(modelGroupChats, holder);
    }

    private void setUserName(ModelGroupChats model, final MyChatGroupHolder holder) {
        // get sender info from uid in modelGroupChats
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String name = ""+snapshot.getChildren();
                            holder.nameChat.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatsQ.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatsQ.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    class MyChatGroupHolder extends RecyclerView.ViewHolder{
        private TextView nameChat, messageChat, timeChat;
        private ImageView messageImage;

        public MyChatGroupHolder(@NonNull View itemView) {
            super(itemView);

            nameChat = itemView.findViewById(R.id.nameChatUser);
            messageChat = itemView.findViewById(R.id.messageUserChat);
            timeChat = itemView.findViewById(R.id.timeUserChat);
            messageImage = itemView.findViewById(R.id.messageImageGroup);
        }
    }
}
