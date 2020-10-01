package com.example.joinriding.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.R;
import com.example.joinriding.models.ModelComments;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyCommentHolder>{
    // Declare
    private Context context;
    private List<ModelComments> modelCommentsList;
    private String myUid, postId;

    // Constructor

    public AdapterComments(Context context, List<ModelComments> modelCommentsList, String myUid, String postId) {
        this.context = context;
        this.modelCommentsList = modelCommentsList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyCommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);
        return new MyCommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCommentHolder holder, int position) {
        // Get Data
        final String uid = modelCommentsList.get(position).getUid();
        String name = modelCommentsList.get(position).getuName();
        String photo = modelCommentsList.get(position).getuPhoto();
        final String cid = modelCommentsList.get(position).getCommentId();
        String comment = modelCommentsList.get(position).getCommentMessage();
        String timestamp = modelCommentsList.get(position).getCommentTime();

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

        // Set Data To row_comment
        holder.nameUser.setText(name);
        holder.commentUser.setText(comment);
        holder.timeUser.setText(dateTime);

        // Set User Photo
        try{
            Picasso.get().load(photo).placeholder(R.drawable.icon_user_grey).into(holder.photoUser);
        }catch (Exception e){}

        // Handle Button Commnet Click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if this commnet is by signed  in user or not
                if(myUid.equals(uid)){
                    // show deletr dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Delete Comments");
                    builder.setMessage("Are You Serious Delete This ?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteComments(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }else{
                    // not my comment
                    Toast.makeText(context, "Can't Delete Other User Post...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteComments(String cid) {
        final DatabaseReference referenceQ = FirebaseDatabase.getInstance().getReference("TBL_POSTS").child(postId);
        referenceQ.child("Comments").child(cid).removeValue(); // delete comments

        // update the comment count
        referenceQ.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = ""+dataSnapshot.child("commentPosting").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                referenceQ.child("commentPosting").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelCommentsList.size();
    }

    // Declare Class Holder
    class MyCommentHolder extends RecyclerView.ViewHolder{
        // declare view from row_comments.xml
        ImageView photoUser;
        TextView nameUser, commentUser, timeUser;

        public MyCommentHolder(View itemView){
            super(itemView);

            photoUser = itemView.findViewById(R.id.photoUserComment);
            nameUser = itemView.findViewById(R.id.nameUserComment);
            commentUser = itemView.findViewById(R.id.messageUserComment);
            timeUser = itemView.findViewById(R.id.timeUserComment);
        }
    }
}
