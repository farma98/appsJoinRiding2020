package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinriding.adapters.AdapterComments;
import com.example.joinriding.models.ModelComments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PostDetailActivity extends AppCompatActivity {
    // To Get Detail User And Post
    private String hisUid, myUid, myEmail, myName, myPhoto, postId, pLikes, hisDp, hisName, pImage;

    private ProgressDialog progressDialog;

    boolean processComment = false;
    boolean processLike = false;

    // Declare View Post
    private ImageView photoUser, imagePost;
    private TextView nameUser, timePost, descriptionPost, likePost, titlePost, commentPost;
    private ImageButton moreButton;
    private Button likeButton, shareButton;
    private LinearLayout profileLayout;
    private RecyclerView recyclerViewComment;

    private List<ModelComments> modelCommentsList;
    private AdapterComments adapterComments;

    // Declare Comment View
    private EditText commentText, timeComment;
    private ImageButton sendComment;
    private ImageView avatarComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Action bar and ist title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Detail Post Comment");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Get Id Post From Adapter Post
        Intent myIntent = getIntent();
        postId = myIntent.getStringExtra("postId");

        // Init Views
        photoUser = findViewById(R.id.photoUserPosting);
        imagePost = findViewById(R.id.imageUserPosting);
        nameUser = findViewById(R.id.nameUserPosting);
        timePost = findViewById(R.id.timeUserPosting);
        titlePost = findViewById(R.id.titleUserPosting);
        likePost = findViewById(R.id.likeUserPosting);
        commentPost = findViewById(R.id.commentUserPosting);
        descriptionPost = findViewById(R.id.descriptionUserPosting);
        moreButton = findViewById(R.id.buttonMorePosting);
        likeButton = findViewById(R.id.buttonLikePosting);
        shareButton = findViewById(R.id.buttonSharePosting);
        profileLayout = findViewById(R.id.userLayoutPosting);
        recyclerViewComment = findViewById(R.id.recyclerViewComment);
        profileLayout = findViewById(R.id.userLayoutPosting);

        commentText = findViewById(R.id.commentText);
        sendComment = findViewById(R.id.btnSendComment);
        avatarComment = findViewById(R.id.iconUserComment);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLike();

        loadComments();

        // handle layout clikc
        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent profileIntent = new Intent(getApplicationContext(), OwnProfileActivity.class);
                profileIntent.putExtra("uid", hisUid);
                startActivity(profileIntent);
            }
        });

        // Handle Button Comment Send Click
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });
        // Handle Button Like Click
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });
        // Handle Button More Click
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOption();
            }
        });
        // Handle Button Share Click
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pTitle = titlePost.getText().toString().trim();
                String pDescription = descriptionPost.getText().toString().trim();

                // get image from imageview
                BitmapDrawable bitmapDrawable = (BitmapDrawable)imagePost.getDrawable();
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

        // Handle Click Like By
        likePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(PostDetailActivity.this, PostLikeByActivity.class);
                myIntent.putExtra("postId", postId);
                startActivity(myIntent);
            }
        });
    }

    private void addHisNotif(String hisUid, String pId, String notification){
        // timestamp
        String timestamp = ""+System.currentTimeMillis();

        // data to put in notif in firebase
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
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
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
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;

        try{
            imageFolder.mkdirs(); // create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.example.joinridingapps.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return  uri;
    }

    private void loadComments() {
        // layout linear for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // set layout to recyclerview
        recyclerViewComment.setLayoutManager(layoutManager);

        // init comments list
        modelCommentsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS").child(postId).child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelCommentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelComments modelComments = snapshot.getValue(ModelComments.class);

                    modelCommentsList.add(modelComments);
                    // pass myUid and postId as parameter Comment Adapter
                    // setup adapter
                    adapterComments = new AdapterComments(getApplicationContext(), modelCommentsList, myUid, postId);
                    // set adapter
                    recyclerViewComment.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOption() {
        // create pop up menu delete
        PopupMenu popupMenu = new PopupMenu(this, moreButton, Gravity.END);

        // show delete options in onlyoist of currently signe in user
        if (hisUid.equals(myUid)){
            // add item in menu
            popupMenu.getMenu().add(Menu.NONE,0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE,1, 0, "Edit");
        }

        // item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0){
                    // delete click
                    beginDelete();
                }else if (id == 1){
                    // edit click start activity add post with key
                    Intent myIntent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    myIntent.putExtra("key", "editPost");
                    myIntent.putExtra("editPostId", postId);
                    startActivity(myIntent);
                }

                return false;
            }
        });

        // show menu
        popupMenu.show();
    }

    private void beginDelete() {
        // post can delete image or no image
        if (pImage.equals("noImage")){
            deleteWithOutImage();
        }else{
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        // progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting Post...");

        // steps delete post
        // 1).delete image using url
        // 2).delete from database using post id

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // delete from database storage
                        Query query = FirebaseDatabase.getInstance().getReference("TBL_POSTS").orderByChild("idPosting").equalTo(postId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    snapshot.getRef().removeValue(); // remove values from database with pId matches
                                }
                                // dialog delete
                                Toast.makeText(PostDetailActivity.this, "Delete Successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

    private void deleteWithOutImage() {
        // progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting Post...");

        // delete from database storage
        Query query = FirebaseDatabase.getInstance().getReference("TBL_POSTS").orderByChild("idPosting").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    snapshot.getRef().removeValue(); // remove values from database with pId matches
                }
                // dialog delete
                Toast.makeText(PostDetailActivity.this, "Delete Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLike() {
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("TBL_LIKES");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)){
                    likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_liked_pink, 0,0,0);
                    likeButton.setText("Liked");
                }else{
                    likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_grey, 0,0,0);
                    likeButton.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        processLike = true;
        // get id the post clicked
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("TBL_LIKES");
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("TBL_POSTS");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (processLike){
                    if (dataSnapshot.child(postId).hasChild(myUid)){
                        // already like, sp remove like
                        postRef.child(postId).child("likePosting").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        processLike = false;
                    }else{
                        // add like post or not like
                        postRef.child(postId).child("likePosting").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");
                        processLike = false;

                        addHisNotif(""+hisUid, ""+postId, "Like Your Posting...");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Comment");

        // get data from comment edit text
        String comment = commentText.getText().toString().trim();
        // validate
        if (TextUtils.isEmpty(comment)){
            // no value is entered
            Toast.makeText(this, "Comment is Empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();

        //put info in hashMap
        hashMap.put("commentId", timeStamp);
        hashMap.put("commentMessage", comment);
        hashMap.put("commentTime", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uPhoto", myPhoto);
        hashMap.put("uName", myName);


        // put this data to db
        reference.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                        commentText.setText("");
                        updateCommentCount();

                        addHisNotif(""+hisUid, ""+postId, "Comment On Your Posting...");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentCount() {
        processComment = true;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (processComment){
                    String comments = ""+dataSnapshot.child("commentPosting").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    reference.child("commentPosting").setValue(""+newCommentVal);
                    processComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query query = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        query.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    myName = ""+snapshot.child("nameUser").getValue();
                    myPhoto = ""+snapshot.child("photoUser").getValue();

                    // set data image
                    try{
                        Picasso.get().load(myPhoto).placeholder(R.drawable.icon_user_grey).into(avatarComment);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.icon_user_grey).into(avatarComment);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        // get detail post using id of post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        Query query = reference.orderByChild("idPosting").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    String pTitle = ""+snapshot.child("namePosting").getValue();
                    String pDescr = ""+snapshot.child("descriptionPosting").getValue();
                    pLikes = ""+snapshot.child("likePosting").getValue();
                    pImage = ""+snapshot.child("photoPosting").getValue();
                    String pTimeStamp = ""+snapshot.child("timePosting").getValue();
                    hisDp = ""+snapshot.child("uPhoto").getValue();
                    hisUid = ""+snapshot.child("uid").getValue();
                    hisName = ""+snapshot.child("uName").getValue();
                    String commentCount = ""+snapshot.child("commentPosting").getValue();

                    // convert time stamp to dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();

                    // set data
                    titlePost.setText(pTitle);
                    descriptionPost.setText(pDescr);
                    likePost.setText(pLikes + " Likes ");
                    timePost.setText(dateTime);
                    nameUser.setText(hisName);
                    commentPost.setText(commentCount + " Comments ");

                    // set post image
                    // if image equal no image
                    if (pImage.equals("noImage")){
                        imagePost.setVisibility(View.GONE);
                    }else{
                        // show image view
                        imagePost.setVisibility(View.VISIBLE);
                        try{
                            Picasso.get().load(pImage).into(imagePost);
                        }
                        catch (Exception e){}
                    }

                    // set user image comment
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.icon_user_grey).into(photoUser);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.icon_user_grey).resize(50, 50).centerCrop().into(photoUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            // user is signed in stay here
            // set email of loggedin user
            myUid = user.getUid();

        }else{
            //user not signed,go to main
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_notification).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
