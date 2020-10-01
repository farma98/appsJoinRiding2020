package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.joinriding.fragments.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddPostActivity extends AppCompatActivity {
    // Declare Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    // Declare View
    private EditText titlePosting, descriptionPosting;
    private ImageView imagePosting;
    private Button buttonUploadPost;

    // View Camera Or Gallery
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // Arrays Of Permissions Request
    private String cameraPermissions[];
    private String storagePermissions[];

    // Uri Image
    Uri imageUri = null;

    // User Info
    private String name, email, uid, photo, like, comment;
    // Info Post Edit
    private String editTitlePost, editDescriptionPost, editImagePost;

    // Progress Dialog
    private SweetAlertDialog sweetAlertDialog;

    private static final String IMAGE_DIRECTORY = "/JoinRidingIMG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // Action bar and ist title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Tambah Status / Posting");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // Init Views
        titlePosting = findViewById(R.id.txtTitlePost);
        descriptionPosting = findViewById(R.id.txtDescriptionPost);
        imagePosting = findViewById(R.id.imagePost);
        buttonUploadPost = findViewById(R.id.btnUploadPost);

        // Get Data From Previous Activity
        Intent myIntent = getIntent();

        // Get Data To Intent
        String action = myIntent.getAction();
        String type = myIntent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null){
            if ("text/plain".equals(type)){
                handleSendText(myIntent);
            }else if(type.startsWith("image")){
                handleSendImage(myIntent);
            }
        }

        final String isUpdateKey = ""+myIntent.getStringExtra("key");
        final String editPostId = ""+myIntent.getStringExtra("editPostId");

        // Validate If We Came Here To Update Post Came From Adapter Post
        if (isUpdateKey.equals("editPost")){
            // update
            actionBar.setTitle("Update Post");
            buttonUploadPost.setText("Update");
            loadPostData(editPostId);
        }else{
            // add
            actionBar.setTitle("Add New Post");
            buttonUploadPost.setText("Upload");
        }

        // Init Arrays Fot Permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // get Some Info Current User
        reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        Query query = reference.orderByChild("emailUser").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checked until required data get
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    name = ""+ snapshot.child("nameUser").getValue();
                    email = ""+ snapshot.child("emailUser").getValue();
                    photo = ""+ snapshot.child("photoUser").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // get image from camera or gallery on click
        imagePosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show image pick dialog
                showImagePickDialog();
            }
        });

        // action upload button click
        buttonUploadPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get data
                String title = titlePosting.getText().toString().trim();
                String description = descriptionPosting.getText().toString().trim();

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter Title....", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Description Title....", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")){
                    beginUpdatePost(title, description, editPostId);
                }else{
                    uploadData(title, description);
                }

                //Intent intent = new Intent(getBaseContext(), HomeFragment.class);
                //startActivity(intent);
            }
        });
    }

    private void handleSendImage(Intent intent) {
        Uri imageUriQ = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUriQ != null){
            imageUri = imageUriQ;
            // set to imageview
            imagePosting.setImageURI(imageUri);
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null){
            descriptionPosting.setText(sharedText);
        }
    }

    private void beginUpdatePost(String title, String description, String editPostId) {
        sweetAlertDialog.setTitleText("Updating Post...");
        sweetAlertDialog.show();

        if (!editImagePost.equals("noImage")){
            // with image
            updateWithWasImage(title, description, editPostId);
        }else if (imagePosting.getDrawable() != null){
            //with image
            updateWithNowImage(title, description, editPostId);
        }else{
            // without image
            updateWithOutImage(title, description, editPostId);
        }
    }

    private void updateWithOutImage(String title, String description, String editPostId) {

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uPhoto", photo);
        hashMap.put("namePosting", title);
        hashMap.put("descriptionPosting", description);
        hashMap.put("photoPosting", "noImage");
//        hashMap.put("likePosting", like);
//        hashMap.put("commentPosting", comment);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sweetAlertDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {
        // image deleted, upload new image
        // for post-image, post-id, publish-item
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "IMG_POST/"+ "imgPost_"+timeStamp;

        // get image
        Bitmap bitmap = ((BitmapDrawable)imagePosting.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[]data = baos.toByteArray();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathName);
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // image is uploaded
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uPhoto", photo);
                            // hashMap.put("pId",timeStamp);
                            hashMap.put("namePosting", title);
                            hashMap.put("descriptionPosting", description);
                            hashMap.put("photoPosting", downloadUri);
                            hashMap.put("timePosting",timeStamp);
//                            hashMap.put("likePosting", like);
//                            hashMap.put("commentPosting", comment);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sweetAlertDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sweetAlertDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // image not uploaded get is url
                sweetAlertDialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithWasImage(final String title, final String description, final String editPostId) {
        // update with image, delete previous image first
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(editImagePost);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // image deleted, upload new image
                        // for post-image, post-id, publish-item
                        final String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathName = "Posts/"+ "post_"+timeStamp;

                        // get image
                        Bitmap bitmap = ((BitmapDrawable)imagePosting.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        // image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[]data = baos.toByteArray();

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathName);
                        storageReference.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // image is uploaded
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());
                                        String downloadUri = uriTask.getResult().toString();

                                        if (uriTask.isSuccessful()) {

                                            HashMap<String, Object> hashMap = new HashMap<>();

                                            hashMap.put("uid", uid);
                                            hashMap.put("uName", name);
                                            hashMap.put("uEmail", email);
                                            hashMap.put("uPhoto", photo);
                                            hashMap.put("namePosting", title);
                                            hashMap.put("descriptionPosting", description);
                                            hashMap.put("photoPosting", downloadUri);
//                                            hashMap.put("likePosting", like);
//                                            hashMap.put("commentPosting", comment);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sweetAlertDialog.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    sweetAlertDialog.dismiss();
                                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // image not uploaded get is url
                                sweetAlertDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
        // get detail post using id of post
        Query query = reference.orderByChild("idPosting").equalTo(editPostId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    editTitlePost = ""+snapshot.child("namePosting").getValue();
                    editDescriptionPost = ""+snapshot.child("descriptionPosting").getValue();
                    editImagePost = ""+snapshot.child("photoPosting").getValue();

                    // set data to view
                    titlePosting.setText(editTitlePost);
                    descriptionPosting.setText(editDescriptionPost);

                    // set image
                    if (!editImagePost.equals("noImage")){
                        try {
                            Picasso.get().load(editImagePost).into(imagePosting);
                        }catch (Exception e){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(final String title, final String description) {
        sweetAlertDialog.setTitleText("Publishing Post...");
        sweetAlertDialog.show();

        // for post image time
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "IMG_POST/"+ "imgPost_"+timeStamp;

        if (imagePosting.getDrawable() != null){
            // get image
            Bitmap bitmap = ((BitmapDrawable)imagePosting.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[]data = baos.toByteArray();

            // post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // image is uploaded
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){

                                HashMap<String, Object> hashMap = new HashMap<>();

                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uPhoto",photo);
                                hashMap.put("idPosting",timeStamp);
                                hashMap.put("namePosting",title);
                                hashMap.put("descriptionPosting",description);
                                hashMap.put("photoPosting",downloadUri);
                                hashMap.put("timePosting",timeStamp);
                                hashMap.put("likePosting", "0");
                                hashMap.put("commentPosting", "0");

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
                                // put data in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                sweetAlertDialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();

                                                titlePosting.setText("");
                                                descriptionPosting.setText("");
                                                imagePosting.setImageURI(null);
                                                imageUri = null;

                                                // send notif
                                                preparedNotification(
                                                        ""+timeStamp,
                                                        ""+name+"add new post",
                                                        ""+title+"\n"+description,
                                                        "PostNotification",
                                                        "POST");

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        sweetAlertDialog.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // failed upload image
                    sweetAlertDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            HashMap<Object, String> hashMap = new HashMap<>();

            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail", email);
            hashMap.put("uPhoto",photo);
            hashMap.put("idPosting",timeStamp);
            hashMap.put("namePosting",title);
            hashMap.put("descriptionPosting",description);
            hashMap.put("photoPosting","noImage");
            hashMap.put("timePosting",timeStamp);
            hashMap.put("likePosting", "0");
            hashMap.put("commentPosting", "0");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_POSTS");
            // put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            sweetAlertDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();

                            titlePosting.setText("");
                            descriptionPosting.setText("");
                            imagePosting.setImageURI(null);
                            imageUri = null;

                            // send notif
                            preparedNotification(
                                    ""+timeStamp,
                                    ""+name+"add new post",
                                    ""+title+"\n"+description,
                                    "PostNotification",
                                    "POST");

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void preparedNotification(String pId, String title, String description, String notificationType, String notificationTopic){
        // prepare data for notification

        String NOTIFICATION_TOPIC = "/topics/"+ notificationTopic;
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = description;
        String NOTIFICATION_TYPE = notificationType;

        // prepare json to send and where to send
        JSONObject notifJs = new JSONObject();
        JSONObject notifBodyJs = new JSONObject();

        try {
            // what to send
            notifBodyJs.put("notificationType", NOTIFICATION_TYPE);
            notifBodyJs.put("sender", uid); // uid current user
            notifBodyJs.put("pId", pId); // post id
            notifBodyJs.put("pTitle", NOTIFICATION_TITLE);
            notifBodyJs.put("pDescription", NOTIFICATION_MESSAGE);

            notifJs.put("to", NOTIFICATION_TOPIC); // where to send
            notifJs.put("data", notifBodyJs); // combined data
        } catch (JSONException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotif(notifJs);
    }

    private void sendPostNotif(JSONObject notifJs) {
        // send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notifJs,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Toast.makeText(AddPostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // put required header
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-type", "application/json");
                headers.put("Authorization", "key=AAAAqQk6qcQ:APA91bE6Ix0Gi39HgAGfrpq6_22FG3NTotTqcX-nOIv-u9UWFeORw8cUSem0pngY-PIwoYmrH6P-UDxz8D2VKTfHwK6hsdWtW1r-hty9GH35fUmnxnks2BSHuF-nXpMSFsh01qrp_rC1");
                return headers;
            }
        };

        // add this request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void showImagePickDialog() {
        // options to show in dialog
        String options[] = {"Camera", "Gallery"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set title
        builder.setTitle("Pick Image From");
        //set item to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // handle dialog item click
                if (i == 0){
                    // camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickCamera();
                    }

                }else if (i == 1){
                    // gallery picked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickGallery();
                    }
                }
            }
        });

        // create and show dialog
        builder.create().show();
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        // request runtime storage permissions
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        // request runtime camera permissions
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void pickCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickGallery() {
        // pic from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // this been called after picking image from camera or gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                imagePosting.setImageBitmap(thumbnail);
                saveImage(thumbnail);
            }

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();

                imagePosting.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            // user is signed in stay here
            // set email of loggedin user
            email = user.getEmail();
            uid = user.getUid();

        }else{
            //user not signed,go to main
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                // picking from camera
                if (grantResults.length > 0) {
                    boolean cameraAccapted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccapted && writeStorageAccepted){
                        // permission enabled
                        pickCamera();
                    }else{
                        // permission denied
                        Toast.makeText(this, "Please Enable Camera & Storge Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                // picking from gallery
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted){
                        // permission enabled
                        pickGallery();
                    }else{
                        // permission denied
                        Toast.makeText(this, "Please Enable Storge Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
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
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_settings){
            startActivity(new Intent(AddPostActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
