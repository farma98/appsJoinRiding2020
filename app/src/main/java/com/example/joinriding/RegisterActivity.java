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
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterActivity extends AppCompatActivity {
    // Declare View
    private EditText edtName, edtPhone, edtAddress, edtEmail, edtPassword;
    private TextView txtHaveAccount;
    private ImageView imgSim, imgPhoto;
    private Button buttonRegisterAccount;
    private RadioGroup radioGender;
    private RadioButton radioGenderOption;

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

    // info
    private String name, phone, gender, address, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Registrasi");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init View
        edtName = findViewById(R.id.edtNameUser);
        edtPhone = findViewById(R.id.edtPhoneUser);
        edtAddress = findViewById(R.id.edtAddressUser);
        imgSim = findViewById(R.id.imgSimUser);
        imgPhoto = findViewById(R.id.imgPhotoUser);
        edtEmail = findViewById(R.id.edtEmailInput);
        edtPassword = findViewById(R.id.edtPasswordInput);
        buttonRegisterAccount = findViewById(R.id.btnRegisterUser);
        txtHaveAccount = findViewById(R.id.txtIntentLogin);

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

        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Registering User...");

        // init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // get image sim from camera or gallery on click
        imgSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show image pick dialog
                showImagePickDialog();
            }
        });

        // Handle Button Register Click
        buttonRegisterAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Init Value Register
                name = edtName.getText().toString().trim();
                phone = edtPhone.getText().toString().trim();
                address = edtAddress.getText().toString().trim();
                email = edtEmail.getText().toString().trim();
                password = edtPassword.getText().toString().trim();

                // Validation Input
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    // Set Error & Focus To Email Input
                    edtEmail.setError("Invalid Email");
                    edtPassword.setFocusable(true);
                }else if(password.length()<6){
                    // Set Error & Focus To Password Input
                    edtPassword .setError("Password Length At Least 6 Characters");
                    edtPassword.setFocusable(true);
                }else if(name.length()<0){
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
                }else{
                    registerUser(email, password, name, phone, gender, address); // Call Function registerUser
                }
            }
        });

        // Handle Text View Already Click
        txtHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, final String password, final String name, final String phone, final String gender, final String address) {
        // If Success Validation
        sweetAlertDialog.setTitleText("Register User Loading...");
        sweetAlertDialog.show();

        // for user photo time
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        final String filePathSim = "IMG_SIM/"+"simUser_"+timeStamp;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            final FirebaseUser user = mAuth.getCurrentUser();

                            final String uid = user.getUid();
                            final String email = user.getEmail();

                            if (imgSim.getDrawable() != null){
                                // get image sim
                                Bitmap bitmap = ((BitmapDrawable)imgSim.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                // image sim compress
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                byte[]data = baos.toByteArray();

                                // register with image SIM
                                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathSim);
                                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // image sim is uploaded
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());
                                        String downloadUriSim = uriTask.getResult().toString();

                                        if (uriTask.isSuccessful()){

                                            HashMap<Object, String> hashMap = new HashMap<>();
                                            // Put Into Database
                                            hashMap.put("uid", uid);
                                            hashMap.put("nameUser", name);
                                            hashMap.put("phoneUser", phone);
                                            hashMap.put("genderUser", gender);
                                            hashMap.put("addressUser", address);
                                            hashMap.put("photoUser", "noImage");
                                            hashMap.put("simUser", downloadUriSim);
                                            hashMap.put("emailUser", email);
                                            hashMap.put("passwordUser", password);
                                            hashMap.put("onlineStatus", "online");
                                            hashMap.put("typingTo", "noOne");
                                            // Instance Database
                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            // Path To Database Name (TBL_USER)
                                            DatabaseReference reference = database.getReference("TBL_USERS");
                                            // Put Data Within Value Hashmap In Database
                                            reference.child(uid).setValue(hashMap);

                                            registerCycle();

                                            sweetAlertDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, IntroSliderActivity.class));
                                            finish();

                                        }
                                    }
                                });
                            }

                        } else {
                            // If Sign In Failed, Display A Message To The user
                            sweetAlertDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed...", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Message Error
                sweetAlertDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerCycle(){
        final FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();

        HashMap<Object, String> hashMap = new HashMap<>();
        // Put Into Database
        hashMap.put("uid", uid);
        hashMap.put("nameCycle", "");
        hashMap.put("platCycle", "");
        hashMap.put("yearCycle", "");
        hashMap.put("brandCycle", "");
        hashMap.put("typeCycle", "");
        hashMap.put("photoCycle", "noImage");

        // Instance Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Path To Database Name (TBL_USER)
        DatabaseReference reference = database.getReference("TBL_CYCLE");
        // Put Data Within Value Hashmap In Database
        reference.child(uid).setValue(hashMap);

        finish();
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
                imgSim.setImageBitmap(thumbnail);
                saveImage(thumbnail);
            }

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                imageUri = data.getData();

                imgSim.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go To Previous Activity
        return super.onSupportNavigateUp();
    }

}
