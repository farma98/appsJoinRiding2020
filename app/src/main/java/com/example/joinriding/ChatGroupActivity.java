package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinriding.adapters.AdapterGroupChats;
import com.example.joinriding.models.ModelGroupChats;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatGroupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private Toolbar toolbar;

    private String groupId;
    private String myGroupRole = "";
    private ImageView groupIconQ, messageImage;
    private ImageButton btnAttachImage, btnSendChat;
    private TextView groupTitleQ;
    private EditText messageChat;

    private RecyclerView recyclerViewChatGroup;
    private ArrayList<ModelGroupChats> modelGroupChatsList;
    private AdapterGroupChats adapterGroupChats;

    // view camera or gallery
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // arrays of permission to be request
    String cameraPermissions[];
    String storagePermissions[];

    // Uri Image
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);

        mAuth = FirebaseAuth.getInstance();

        // inti view
        toolbar = findViewById(R.id.toolbarChatGroup);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        groupIconQ = findViewById(R.id.groupIconTv);
        groupTitleQ = findViewById(R.id.groupTitleTv);
        messageChat = findViewById(R.id.messageChat);
        messageImage = findViewById(R.id.messageImageGroup);
        btnSendChat = findViewById(R.id.btnSendChat);
        btnAttachImage = findViewById(R.id.attachButton);
        recyclerViewChatGroup = findViewById(R.id.recyclerViewGroupChatsQ);

        // get id the group
        Intent myIntent = getIntent();
        groupId = myIntent.getStringExtra("idGroup");

        // init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();

        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = messageChat.getText().toString().trim();

                // check if text is empty or not
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(ChatGroupActivity.this, "Cant send empty message", Toast.LENGTH_SHORT).show();
                }else{
                    // message ready
                    sendMessage(message);
                }
                messageChat.setText("");
            }
        });

        btnAttachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show image pick dialog
                showImagePickDialog();
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            myGroupRole = ""+snapshot.child("role").getValue();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.orderByChild("idGroup").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String groupTitle = ""+snapshot.child("nameGroup").getValue();
                            String groupDescription = ""+snapshot.child("descriptionGroup").getValue();
                            String groupIcon = ""+snapshot.child("photoGroup").getValue();
                            String timestamp = ""+snapshot.child("timeGroup").getValue();
                            String createdBy = ""+snapshot.child("createdGroup").getValue();

                            groupTitleQ.setText(groupTitle);
                            try{
                                Picasso.get().load(groupIcon).resize(50, 50).centerCrop().placeholder(R.drawable.icon_group_grey).into(groupIconQ);
                            }catch (Exception e){
                                groupIconQ.setImageResource(R.drawable.icon_group_grey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadGroupMessages() {
        modelGroupChatsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        modelGroupChatsList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            ModelGroupChats modelGroupChats = snapshot.getValue(ModelGroupChats.class);
                            modelGroupChatsList.add(modelGroupChats);
                        }

                        // adapter
                        adapterGroupChats = new AdapterGroupChats(ChatGroupActivity.this, modelGroupChatsList);
                        // set adapter
                        recyclerViewChatGroup.setAdapter(adapterGroupChats);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage(final String message) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", mAuth.getUid());
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("type", "text");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(groupId).child("Messages").child(timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // clear text messages
                        messageChat.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendImageMessage(Uri imageUri) throws IOException {

        // progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Image...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        final String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "CHAT_IMAGE_GROUP/"+ "imgChatGroup_"+timeStamp;

        // get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,  100, baos);
        byte[] data = baos.toByteArray(); // convert image to bytes

        StorageReference reference = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        reference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // image upload
                        progressDialog.dismiss();
                        // get url of uploaded image
                        Task<Uri> uriTask =  taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            // add image url and other info to database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            // setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", mAuth.getUid());
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timeStamp);
                            hashMap.put("type", "image");

                            // add to database
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
                            reference1.child(groupId).child("Messages").child(timeStamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messageChat.setText("");
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChatGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
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
        // intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Group Image Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Description");
        // put image url
        imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickGallery() {
        // pic from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // this been called after picking image from camera or gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                imageUri = data.getData();
                // use this image uri to upload to firebase
                try {
                    sendImageMessage(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();
                // use this image uri to upload to firebase
                try {
                    sendImageMessage(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);

        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_notification).setVisible(false);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            menu.findItem(R.id.action_add_user).setVisible(true);
        }else{
            menu.findItem(R.id.action_add_user).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_user){
            Intent myIntent = new Intent(this, GroupChatAddUserActivity.class);
            myIntent.putExtra("idGroup", groupId);
            startActivity(myIntent);
        } else if (id == R.id.action_group_info){
            Intent myIntent = new Intent(this, GroupInfoActivity.class);
            myIntent.putExtra("idGroup", groupId);
            startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}
