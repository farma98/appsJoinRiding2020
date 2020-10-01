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

public class CycleUserActivity extends AppCompatActivity {
    // Declare View
    private EditText edtName, edtPlat, edtYear, edtBrand, edtType;
    private TextView txtChangePhoto;
    private ImageView imgPhoto;
    private Button buttonUpdateCycle;

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
    private String editName, editPlat, editYear, editBrand, editType, editPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycle_user);

        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile Motor");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init View
        edtName = findViewById(R.id.edtNameCycle);
        edtPlat = findViewById(R.id.edtPlatCycle);
        edtYear = findViewById(R.id.edtYearCycle);
        edtBrand = findViewById(R.id.edtBrandCycle);
        edtType = findViewById(R.id.edtTypeCycle);
        imgPhoto = findViewById(R.id.photoCycle);
        txtChangePhoto = findViewById(R.id.txtChangePhotoCycle);
        buttonUpdateCycle = findViewById(R.id.btnUpdateCycle);

        // Get Data From Previous Activity
        Intent myIntent = getIntent();
        final String isUpdateKey = ""+myIntent.getStringExtra("key");
        final String editCycleId = ""+myIntent.getStringExtra("editCycleId");

        // Validate If We Came Here To Update Post Came From Adapter Post
        if (isUpdateKey.equals("editCycle")){
            // update
            loadCycleData(editCycleId);
        }

        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Update Cycle...");

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

        // Handle Button Register Click
        buttonUpdateCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Init Value Register
                String name = edtName.getText().toString().trim();
                String plat = edtPlat.getText().toString().trim();
                String year = edtYear.getText().toString().trim();
                String brand = edtBrand.getText().toString().trim();
                String type = edtType.getText().toString().trim();

                // Validation Input
                if(name.length()<0){
                    // Set Error & Focus To Name Input
                    edtName.setError("Name Must Be Insert");
                    edtName.setFocusable(true);
                }else if(plat.length()<0){
                    // Set Error & Focus To Phone Input
                    edtPlat.setError("Plat Must Be Insert");
                    edtPlat.setFocusable(true);
                }else if(year.length()<0){
                    // Set Error & Focus To Gender Input
                    edtYear.setError("Year Must Be Insert");
                    edtYear.setFocusable(true);
                }else if(brand.length()<0){
                    // Set Error & Focus To Gender Input
                    edtBrand.setError("Brand Must Be Insert");
                    edtBrand.setFocusable(true);
                }else if(type.length()<0){
                    // Set Error & Focus To Gender Input
                    edtType.setError("Type Must Be Insert");
                    edtType.setFocusable(true);
                }

                if (isUpdateKey.equals("editCycle")){
                    beginUpdateCycle(name, plat, year, brand, type, editCycleId);
                }
            }
        });
    }

    private void beginUpdateCycle(String name, String plat, String year, String brand, String type, String editCycleId) {
        sweetAlertDialog.setTitleText("Updating Cycle...");
        sweetAlertDialog.show();

        if (!editPhoto.equals("noImage")){
            // with image
            updateWithWasImage(name, plat, year, brand, type, editCycleId);
        }else if (imgPhoto.getDrawable() != null){
            //with image
            updateWithNowImage(name, plat, year, brand, type, editCycleId);
        }else{
            // without image
            updateWithOutImage(name, plat, year, brand, type, editCycleId);
        }
    }

    private void updateWithOutImage(String name, String plat, String year, String brand, String type, String editCycleId) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("nameCycle", name);
        hashMap.put("platCycle", plat);
        hashMap.put("yearCycle", year);
        hashMap.put("brandCycle", brand);
        hashMap.put("typeCycle", type);
        hashMap.put("photoCycle", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_CYCLE");
        ref.child(editCycleId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sweetAlertDialog.dismiss();
                        Toast.makeText(CycleUserActivity.this, "Update WithOut Image", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(CycleUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(final String name, final String plat, final String year, final String brand, final String type, final String editCycleId) {
        // image deleted, upload new image
        // for post-image, post-id, publish-item
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "IMG_CYCLE/"+ "imgCycle_"+timeStamp;

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

                            hashMap.put("nameCycle", name);
                            hashMap.put("platCycle", plat);
                            hashMap.put("yearCycle", year);
                            hashMap.put("brandCycle", brand);
                            hashMap.put("typeCycle", type);
                            hashMap.put("photoCycle", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_CYCLE");
                            ref.child(editCycleId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sweetAlertDialog.dismiss();
                                            Toast.makeText(CycleUserActivity.this, "Update With Now Image", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sweetAlertDialog.dismiss();
                                    Toast.makeText(CycleUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // image not uploaded get is url
                sweetAlertDialog.dismiss();
                Toast.makeText(CycleUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithWasImage(final String name, final String plat, final String year, final String brand, final String type, final String editCycleId) {
        // update with image, delete previous image first
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(editPhoto);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // for post-image, post-id, publish-item
                        final String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathName = "IMG_CYCLE/"+ "imgCycle_"+timeStamp;

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

                                            hashMap.put("nameCycle", name);
                                            hashMap.put("platCycle", plat);
                                            hashMap.put("yearCycle", year);
                                            hashMap.put("brandCycle", brand);
                                            hashMap.put("typeCycle", type);
                                            hashMap.put("photoCycle", downloadUri);

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TBL_CYCLE");
                                            ref.child(editCycleId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            sweetAlertDialog.dismiss();
                                                            Toast.makeText(CycleUserActivity.this, "Update With Was Image...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    sweetAlertDialog.dismiss();
                                                    Toast.makeText(CycleUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // image not uploaded get is url
                                sweetAlertDialog.dismiss();
                                Toast.makeText(CycleUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(CycleUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCycleData(String editCycleId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_CYCLE");
        // get detail post using id of post
        Query query= reference.orderByChild("uid").equalTo(editCycleId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // get data
                    editName = ""+snapshot.child("nameCycle").getValue();
                    editPlat = ""+snapshot.child("platCycle").getValue();
                    editYear = ""+snapshot.child("yearCycle").getValue();
                    editBrand = ""+snapshot.child("brandCycle").getValue();
                    editType = ""+snapshot.child("typeCycle").getValue();
                    editPhoto = ""+snapshot.child("photoCycle").getValue();

                    // set data to view
                    edtName.setText(editName);
                    edtPlat.setText(editPlat);
                    edtYear.setText(editYear);
                    edtBrand.setText(editBrand);
                    edtType.setText(editType);

                    // set image
                    if (!editPhoto.equals("noImage")){
                        try {
                            Picasso.get().load(editPhoto).into(imgPhoto);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.icon_user_grey).resize(50, 50).centerCrop().into(imgPhoto);
                        }
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
            startActivity(new Intent(CycleUserActivity.this, MainActivity.class));
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
            startActivity(new Intent(CycleUserActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
