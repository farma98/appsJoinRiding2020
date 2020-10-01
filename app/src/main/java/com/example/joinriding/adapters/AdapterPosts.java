package com.example.joinriding.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinriding.AddPostActivity;
import com.example.joinriding.FullScreenImagePostActivity;
import com.example.joinriding.OwnProfileActivity;
import com.example.joinriding.PostDetailActivity;
import com.example.joinriding.PostLikeByActivity;
import com.example.joinriding.R;
import com.example.joinriding.models.ModelPosts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyPostHolder>{
    // Declare
    private Context context;
    private List<ModelPosts> postsList;
    private String myUid;
    // Declare Like & Post Database Reference
    private DatabaseReference likeReference;
    private DatabaseReference postReference;
    // boolean process like
    private boolean processLike = false;

    // declare sweet alert dialog
    private SweetAlertDialog sweetAlertDialog;

    // Adapter Post
    public AdapterPosts(Context context, List<ModelPosts> postsList) {
        this.context = context;
        this.postsList = postsList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeReference = FirebaseDatabase.getInstance().getReference().child("TBL_LIKES");
        postReference = FirebaseDatabase.getInstance().getReference().child("TBL_POSTS");
    }

    @NonNull
    @Override
    public MyPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);

        return new MyPostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyPostHolder holder, final int position) {
        // Get Data From Model Post
        final String uid = postsList.get(position).getUid();
        String uEmail = postsList.get(position).getuEmail();
        String uName = postsList.get(position).getuName();
        String uPhoto = postsList.get(position).getuPhoto();
        final String pId = postsList.get(position).getIdPosting();
        final String pTitle = postsList.get(position).getNamePosting();
        final String pDescription = postsList.get(position).getDescriptionPosting();
        final String pImage = postsList.get(position).getPhotoPosting();
        String pTimeStamp = postsList.get(position).getTimePosting();
        String pLike = postsList.get(position).getLikePosting();
        String pComment = postsList.get(position).getCommentPosting();

        // init sweetalert
        sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

        // Set Data To row_post.xml
        holder.nameUserPost.setText(uName);
        holder.timeUserPost.setText(dateTime);
        holder.titleUserPost.setText(pTitle);
        holder.descriptionUserPost.setText(pDescription);
        holder.likeUserPost.setText(pLike + " Likes ");
        holder.commentUserPost.setText(pComment + " Comments ");
        // Set User Photo
        try{
            Picasso.get().load(uPhoto).placeholder(R.drawable.icon_user_grey).into(holder.photoUserPost);
        }
        catch (Exception e){}

        // Set Post Image
        // If Image Equal No image
        if (pImage.equals("noImage")){
            holder.imageUserPost.setVisibility(View.GONE);
        }else{
            // show image view
            holder.imageUserPost.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.imageUserPost);
            }
            catch (Exception e){}
        }

        holder.imageUserPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, FullScreenImagePostActivity.class);
                myIntent.putExtra("idPosting", pId);
                context.startActivity(myIntent);
            }
        });

        // Set Like For Each Post
        setLike(holder, pId);

        // Handle Button More Click
        holder.moreBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOption(holder.moreBtnPost, uid, myUid, pId, pImage);
            }
        });

        // Handle Button Like Click
        holder.likeBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int pLike = Integer.parseInt(postsList.get(position).getLikePosting());
                processLike = true;
                // get id the post clicked
                final String postIde = postsList.get(position).getIdPosting();
                likeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (processLike){
                            if (dataSnapshot.child(postIde).hasChild(myUid)){
                                // already like, sp remove like
                                postReference.child(postIde).child("likePosting").setValue(""+(pLike-1));
                                likeReference.child(postIde).child(myUid).removeValue();
                                processLike = false;
                            }else{
                                // add like post or not like
                                postReference.child(postIde).child("likePosting").setValue(""+(pLike+1));
                                likeReference.child(postIde).child(myUid).setValue("Liked");
                                processLike = false;

                                addHisNotif(""+uid, ""+pId, "Like Your Posting...");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        // Handle Button Comment Click
        holder.commentBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, PostDetailActivity.class);
                myIntent.putExtra("postId", pId);
                context.startActivity(myIntent);
            }
        });

        // Handle Button Share Click
        holder.shareBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get image from imageview
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.imageUserPost.getDrawable();
                if (bitmapDrawable == null){
                    // post without image
                    shareTextOnly(pTitle, pDescription);
                }else{
                    // post with image

                    // convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });

        // Handle User Layout Click
        holder.userLayoutPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Own Profile", Toast.LENGTH_SHORT).show();

                Intent profileIntent = new Intent(context, OwnProfileActivity.class);
                profileIntent.putExtra("uid", uid);
                context.startActivity(profileIntent);
            }
        });

        // Handle Text View Like Click
        holder.likeUserPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, PostLikeByActivity.class);
                myIntent.putExtra("postId", pId);
                context.startActivity(myIntent);
            }
        });

    }

    private void addHisNotif(String hisUid, String pId, String notification){
        // Inti TimeStamp
        String timestamp = ""+System.currentTimeMillis();

        // Data To Put Notif in Firebase
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("idPosting", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        reference.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // success
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed
            }
        });
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //concate title and description
        String shareBody = pTitle +"\n"+ pDescription;

        // share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        // concate title and description
        String shareBody = pTitle +"\n"+ pDescription;

        // first save this image in cache. get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        // share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        shareIntent.setType("images/png");
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try{
            imageFolder.mkdirs(); // create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.joinriding.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  uri;
    }

    private void setLike(final MyPostHolder holder, final String postKey) {
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)){
                    holder.likeBtnPost.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_liked_pink, 0,0,0);
                    holder.likeBtnPost.setText("Liked");
                }else{
                    holder.likeBtnPost.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_grey, 0,0,0);
                    holder.likeBtnPost.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void beginDelete(String pId, String pImage) {
        // post can delete image or no image
        if (pImage.equals("noImage")){
            deleteWithOutImage(pId);
        }else{
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        // steps delete post
        // 1).delete image using url
        // 2).delete from database using post id

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // delete from database storage
                        Query query = FirebaseDatabase.getInstance().getReference("TBL_POSTS").orderByChild("idPosting").equalTo(pId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    snapshot.getRef().removeValue(); // remove values from database with pId matches
                                }
                                // dialog delete
                                Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithOutImage(String pId) {
        // delete from database storage
        Query query = FirebaseDatabase.getInstance().getReference("TBL_POSTS").orderByChild("idPosting").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    snapshot.getRef().removeValue(); // remove values from database with pId matches
                }
                // dialog delete
                Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void showMoreOption(ImageButton moreBtnPost, String uId, String myUid, final String pId, final String pImage) {
        // Create Menu Pop Up Delete
        PopupMenu popupMenu = new PopupMenu(context, moreBtnPost, Gravity.END);

        // Show Delete Options
        if (uId.equals(myUid)){
            // add item in menu
            popupMenu.getMenu().add(Menu.NONE,0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE,1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE,2, 0, "View Detail");

        // item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    // delete click
                    beginDelete(pId, pImage);
                }else if (id == 1){
                    // edit click start activity add post with key
                    Intent myIntent = new Intent(context, AddPostActivity.class);
                    myIntent.putExtra("key", "editPost");
                    myIntent.putExtra("editPostId", pId);
                    context.startActivity(myIntent);
                }else if (id == 2){
                    Intent myIntent = new Intent(context, PostDetailActivity.class);
                    myIntent.putExtra("postId", pId);
                    context.startActivity(myIntent);
                }

                return false;
            }
        });

        // show menu
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    // View Holder Class
    class MyPostHolder extends RecyclerView.ViewHolder{
        // Declare Views From row_post.xml
        ImageView photoUserPost, imageUserPost;
        TextView nameUserPost, timeUserPost, descriptionUserPost, likeUserPost, titleUserPost, commentUserPost;
        ImageButton moreBtnPost;
        Button likeBtnPost, commentBtnPost, shareBtnPost;
        LinearLayout userLayoutPost;

        MyPostHolder(@NonNull View itemView) {
            super(itemView);

            // Init View
            photoUserPost = itemView.findViewById(R.id.photoUserPosting);
            imageUserPost= itemView.findViewById(R.id.imageUserPosting);
            nameUserPost = itemView.findViewById(R.id.nameUserPosting);
            titleUserPost = itemView.findViewById(R.id.titleUserPosting);
            timeUserPost = itemView.findViewById(R.id.timeUserPosting);
            descriptionUserPost = itemView.findViewById(R.id.descriptionUserPosting);
            likeUserPost = itemView.findViewById(R.id.likeUserPosting);
            commentUserPost = itemView.findViewById(R.id.commentUserPosting);
            moreBtnPost = itemView.findViewById(R.id.buttonMorePosting);
            likeBtnPost = itemView.findViewById(R.id.buttonLikePosting);
            commentBtnPost = itemView.findViewById(R.id.buttonCommentPosting);
            shareBtnPost = itemView.findViewById(R.id.buttonSharePosting);
            userLayoutPost = itemView.findViewById(R.id.userLayoutPosting);
        }
    }
}
