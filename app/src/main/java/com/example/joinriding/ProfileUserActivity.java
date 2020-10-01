package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileUserActivity extends AppCompatActivity {
    // Declare View
    private EditText edtName, edtPhone,edtAddress;
    private TextView txtChangePhoto;
    private ImageView imgPhoto;
    private Button buttonUpdateProfile;
    private RadioGroup radioGender;
    private RadioButton radioGenderOption, radioPria, radioWanita;

    // Declare Firebase
    private FirebaseAuth mAuth;

    // Declare Progress Dialog
    private SweetAlertDialog sweetAlertDialog;

    // view camera or gallery
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // arrays of permission to be request
    private String cameraPermissions[];
    private String storagePermissions[];

    // Uri Image
    private Uri imageUri = null;

    private static final String IMAGE_DIRECTORY = "/JoinRidingIMG";

    // info user edit
    private String editName, editPhone, editAddress, editGender, editPhoto;

    // info
    private String name, phone, gender, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile User");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init View
        edtName = findViewById(R.id.edtNameUser);
        edtPhone = findViewById(R.id.edtPhoneUser);
        edtAddress = findViewById(R.id.edtAddressUser);
        imgPhoto = findViewById(R.id.photoProfile);
        radioPria = findViewById(R.id.rbPria);
        radioWanita = findViewById(R.id.rbWanita);
        txtChangePhoto = findViewById(R.id.txtChangePhotoProfile);
        buttonUpdateProfile = findViewById(R.id.btnUpdateProfile);

        // Get Data From Previous Activity
        Intent myIntent = getIntent();
        final String isUpdateKey = ""+myIntent.getStringExtra("key");
        final String editUserId = ""+myIntent.getStringExtra("editUserId");

        // Validate If We Came Here To Update Post Came From Adapter Post
        if (isUpdateKey.equals("editUser")){
            // update
            loadUserData(editUserId);
        }

        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));

        // init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // get image photo profile from camera or gallery on click
        txtChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show image pick dialog
                showImagePickDialog();
            }
        });

        radioGender = findViewById(R.id.rbGender);

        radioGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radioGenderOption = radioGender.findViewById(i);

                if (i == R.id.rbPria){
                    gender = radioGenderOption.getText().toString();
                }else{
                    gender = radioGenderOption.getText().toString();
                }

            }
        });

        // Handle Button Register Click
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Init Value Register
                name = edtName.getText().toString().trim();
                phone = edtPhone.getText().toString().trim();
                address = edtAddress.getText().toString().trim();

                // Validation Input
                if(name.length()<0){
                    // Set Error & Focus To Name Input
                    edtName.setError("Name Must Be Insert");
                    edtName.setFocusable(true);
                }else if(phone.length()<0){
                    // Set Error & Focus To Phone Input
                    edtPhone.setError("Phone Must Be Insert");
                    edtPhone.setFocusable(true);
                }else if(address.length()<0){
                    // Set Error & Focus To Address Input
                    edtAddress.setError("Address Must Be Insert");
                    edtAddress.setFocusable(true);
                }

                if (isUpdateKey.equals("editUser")){
                    beginUpdateUser(name, phone, gender, address, editUserId);
                }
            }
        });
    }

    private void beginUpdateUser(String name, String phone, String gender, String address, String editUserId) {
        sweetAlertDialog.setTitleText("Updating Profile...");
        sweetAlertDialog.show();

        if (!editPhoto.equals("noImage")){
            // with image
            updateWithWasImage(name, phone, gender, address, editUserId);
        }else if (imgPhoto.getDrawable() != null){
            //with image
            updateWithNowImage(name, phone, gender, address, editUserId);
        }else{
            // without image
            updateWithOutImage(name, phone, gender, address, editUserId);
        }
    }

    private void updateWithOutImage(String name, String phone, String gender, String address, String editUserId) {

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("nameUser", name);
        hashMap.put("phoneUser", phone);
        hashMap.put("genderUser", gender);
        hashMap.put("addressUser", address);
        hashMap.put("photoUser", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        ref.child(editUserId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sweetAlertDialog.dismiss();
                        Toast.makeText(ProfileUserActivity.this, "Update WithOut Image", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(final String name, final String phone, final String gender, final String address, final String editUserId) {
        // image deleted, upload new image
        // for post-image, post-id, publish-item
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "IMG_USER/"+ "imgUser_"+timeStamp;

        // get image
        Bitmap bitmap = ((BitmapDrawable)imgPhoto.getDrawable()).getBitmap();
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

                            hashMap.put("nameUser", name);
                            hashMap.put("phoneUser", phone);
                            hashMap.put("genderUser", gender);
                            hashMap.put("addressUser", address);
                            hashMap.put("photoUser", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_USERS");
                            ref.child(editUserId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sweetAlertDialog.dismiss();
                                            Toast.makeText(ProfileUserActivity.this, "Update With Now Image", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sweetAlertDialog.dismiss();
                                    Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // image not uploaded get is url
                sweetAlertDialog.dismiss();
                Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithWasImage(final String name, final String phone, final String gender, final String address, final String editUserId) {
        // update with image, delete previous image first
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(editPhoto);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // for post-image, post-id, publish-item
                        final String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathName = "IMG_USER/"+ "imgUser_"+timeStamp;

                        // get image
                        Bitmap bitmap = ((BitmapDrawable)imgPhoto.getDrawable()).getBitmap();
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

                                            hashMap.put("nameUser", name);
                                            hashMap.put("phoneUser", phone);
                                            hashMap.put("genderUser", gender);
                                            hashMap.put("addressUser", address);
                                            hashMap.put("photoUser", downloadUri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_USERS");
                                            ref.child(editUserId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sweetAlertDialog.dismiss();
                                                            Toast.makeText(ProfileUserActivity.this, "Update With Was Image...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    sweetAlertDialog.dismiss();
                                                    Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // image not uploaded get is url
                                sweetAlertDialog.dismiss();
                                Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(ProfileUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData(String editUserId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS");
        // get detail post using id of post
        Query query= reference.orderByChild("uid").equalTo(editUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    editName = ""+snapshot.child("nameUser").getValue();
                    editPhone = ""+snapshot.child("phoneUser").getValue();
                    editAddress = ""+snapshot.child("addressUser").getValue();
                    editGender = ""+snapshot.child("genderUser").getValue();
                    editPhoto = ""+snapshot.child("photoUser").getValue();

                    // set data to view
                    edtName.setText(editName);
                    edtPhone.setText(editPhone);
                    edtAddress.setText(editAddress);

                    // set gender
                    if(editGender.equalsIgnoreCase("Pria")){
                        radioPria.setChecked(true);
                    }else if(editGender.equalsIgnoreCase("Wanita")){
                        radioWanita.setChecked(true);
                    }

                    // set image
                    if (!editPhoto.equals("noImage")){
                        try {
                            Picasso.get().load(editPhoto).into(imgPhoto);
                        }catch (Exception e){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // this been called after picking image from camera or gallery
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                imgPhoto.setImageBitmap(thumbnail);
                saveImage(thumbnail);
            }

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();

                imgPhoto.setImageURI(imageUri);
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

    // inflate optionmenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflated menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_nav_menu, menu);

        // hide addpost icon
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_user).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){

        }else{
            startActivity(new Intent(ProfileUserActivity.this, MainActivity.class));
            finish();
        }
    }

    // handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }else if(id == R.id.action_settings){
            startActivity(new Intent(ProfileUserActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
