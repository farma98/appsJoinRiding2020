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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CreateGroupActivity extends AppCompatActivity {
    // Declare
    private FirebaseAuth mAuth;
    private String myUid;
    private SweetAlertDialog sweetAlertDialog;

    // Declare View
    private ImageView groupIcon;
    private EditText groupTitle, groupDescription;
    private FloatingActionButton fbCreateGroup;

    // View Camera Or Gallery
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // arrays of permission to be request
    String cameraPermissions[];
    String storagePermissions[];

    // Uri Image
    Uri imageUri = null;

    private static final String IMAGE_DIRECTORY = "/JoinRidingIMG";

    // info user edit
    private String editName, editDescription, editPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mAuth = FirebaseAuth.getInstance();

        // Action bar and ist title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Tambah Grup");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init view
        groupIcon = findViewById(R.id.iconGroup);
        groupTitle = findViewById(R.id.titleGroup);
        groupDescription = findViewById(R.id.descriptionGroup);
        fbCreateGroup = findViewById(R.id.createGrupDone);

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));

        // Get Data From Previous Activity
        Intent myIntent = getIntent();
        final String isUpdateKey = ""+myIntent.getStringExtra("key");
        final String editGroupId = ""+myIntent.getStringExtra("editGroupId");

        // Validate If We Came Here To Update Post Came From Adapter Post
        if (isUpdateKey.equals("editGroup")){
            // update
            loadGroupData(editGroupId);
        }

        // init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        checkUserStatus();

        // handel click icon group
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        // handel create group
        fbCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Init Value Register
                String name = groupTitle.getText().toString().trim();
                String description = groupDescription.getText().toString().trim();

                if (isUpdateKey.equals("editGroup")){
                    beginUpdateGroup(name, description, editGroupId);
                }else{
                    creatingGroup();
                }
            }
        });
    }

    private void beginUpdateGroup(String name, String description, String editGroupId) {
        sweetAlertDialog.setTitleText("Updating Group...");
        sweetAlertDialog.show();

        // if image equal no image
        if (editPhoto.equals("noImage")){
            groupIcon.setVisibility(View.GONE);
        }else{
            // show image view
            groupIcon.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(editPhoto).into(groupIcon);
            }
            catch (Exception e){}
        }

        if (!editPhoto.equals("noImage")){
            // with image
            updateWithWasImage(name, description, editGroupId);
        }else if (groupIcon.getDrawable() != null){
            //with image
            updateWithNowImage(name, description, editGroupId);
        }else{
            // without image
            updateWithOutImage(name, description, editGroupId);
        }
    }

    private void updateWithOutImage(String name, String description, String editGroupId) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("nameGroup", name);
        hashMap.put("descriptionGroup", description);
        hashMap.put("photoGroup", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        ref.child(editGroupId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sweetAlertDialog.dismiss();
                        Toast.makeText(CreateGroupActivity.this, "Update WithOut Image", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(final String name, final String description, final String editGroupId) {
        // image deleted, upload new image
        // for post-image, post-id, publish-item
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "GROUP_ICON/"+ "groupIcon_"+timeStamp;

        // get image
        Bitmap bitmap = ((BitmapDrawable)groupIcon.getDrawable()).getBitmap();
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

                            hashMap.put("nameGroup", name);
                            hashMap.put("descriptionGroup", description);
                            hashMap.put("photoGroup", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
                            ref.child(editGroupId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sweetAlertDialog.dismiss();
                                            Toast.makeText(CreateGroupActivity.this, "Update With Now Image", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sweetAlertDialog.dismiss();
                                    Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // image not uploaded get is url
                sweetAlertDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithWasImage(final String name, final String description, final String editGroupId) {
        // update with image, delete previous image first
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(editPhoto);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // for post-image, post-id, publish-item
                        final String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathName = "GROUP_ICON/"+ "groupIcon_"+timeStamp;

                        // get image
                        Bitmap bitmap = ((BitmapDrawable)groupIcon.getDrawable()).getBitmap();
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

                                            hashMap.put("nameGroup", name);
                                            hashMap.put("descriptionGroup", description);
                                            hashMap.put("photoGroup", downloadUri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
                                            ref.child(editGroupId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sweetAlertDialog.dismiss();
                                                            Toast.makeText(CreateGroupActivity.this, "Update With Was Image...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    sweetAlertDialog.dismiss();
                                                    Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // image not uploaded get is url
                                sweetAlertDialog.dismiss();
                                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupData(String editGroupId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        // get detail post using id of post
        Query query= reference.orderByChild("idGroup").equalTo(editGroupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    editName = ""+snapshot.child("nameGroup").getValue();
                    editDescription = ""+snapshot.child("descriptionGroup").getValue();
                    editPhoto = ""+snapshot.child("photoGroup").getValue();

                    // set data to view
                    groupTitle.setText(editName);
                    groupDescription.setText(editDescription);

                    // set image
                    if (!editPhoto.equals("noImage")){
                        try {
                            Picasso.get().load(editPhoto).into(groupIcon);
                        }catch (Exception e){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void creatingGroup() {
        sweetAlertDialog.setTitleText("Creating Group");

        // input title, description
        final String groupTitles = groupTitle.getText().toString().trim();
        final String groupDescriptions = groupDescription.getText().toString().trim();
        // validation
        if (TextUtils.isEmpty(groupTitles)){
            Toast.makeText(this, "Enter Your Group Titles...", Toast.LENGTH_SHORT).show();
            return;
        }

        sweetAlertDialog.show();
        final String gTimesstamp = ""+System.currentTimeMillis();
        // timestamp : grup icon, grupId, timeCreated;
        if (imageUri == null){
            createGroupWithOutIcon(
                    ""+gTimesstamp,
                    ""+groupTitles,
                    ""+groupDescriptions,
                    "");
        }else{
            // image patch icon group
            String FileNamePatch = "GROUP_ICON/" + "groupIcon" + gTimesstamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(FileNamePatch);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // image uploda get url
                            Task<Uri> pUriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!pUriTask.isSuccessful());
                            Uri pDownloadUri = pUriTask.getResult();
                            if (pUriTask.isSuccessful()){
                                createGroupWithOutIcon(
                                        ""+gTimesstamp,
                                        ""+groupTitles,
                                        ""+groupDescriptions,
                                        ""+pDownloadUri);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sweetAlertDialog.dismiss();
                    Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createGroupWithOutIcon(final String gTimestamp, String groupTitles, String groupDescriptions, String groupIcons) {
        // setup info of group
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("idGroup", "" +gTimestamp);
        hashMap.put("nameGroup", "" +groupTitles);
        hashMap.put("descriptionGroup", "" +groupDescriptions);
        hashMap.put("photoGroup", "" +groupIcons);
        hashMap.put("timeGroup", "" +gTimestamp);
        hashMap.put("createdGroup", "" +mAuth.getUid());

        // create group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
        reference.child(gTimestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // success create new group
                        // setup member info group
                        HashMap<String, String> hashMap1 = new HashMap<>();
                        hashMap1.put("uid", mAuth.getUid());
                        hashMap1.put("role", "creator");
                        hashMap1.put("timestamp", gTimestamp);

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("TBL_GROUPS");
                        reference1.child(gTimestamp).child("Participants").child(mAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sweetAlertDialog.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, "Success Create New Group", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sweetAlertDialog.dismiss();
                                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        // intent to start camera
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

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // this been called after picking image from camera or gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                groupIcon.setImageBitmap(thumbnail);
                saveImage(thumbnail);
            }

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();

                groupIcon.setImageURI(imageUri);
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
