package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.joinriding.adapters.AdapterChatsUsers;
import com.example.joinriding.models.ModelChatUsers;
import com.example.joinriding.models.ModelMyListUsers;
import com.example.joinriding.notifications.Data;
import com.example.joinriding.notifications.Sender;
import com.example.joinriding.notifications.Token;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatUsersActivity extends AppCompatActivity {
    // Declare View From chat_users_activity.xml
    private RecyclerView recyclerViewChatUsers;
    private ImageView photoUserChat;
    private TextView nameUserChat, statusUserChat;
    private EditText messageChatUser;
    private ImageButton btnSendChat, btnAttachImage;
    // for checking if use has seen message or not
    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;
    // Declare
    private List<ModelChatUsers> modelChatUsersList;
    private AdapterChatsUsers adapterChatsUsers;
    // Declare Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    //Declare Info
    private String hisUid, myUid, hisImage;
    // Volly Request
    private RequestQueue requestQueue;
    // Declare Notify boolean
    boolean notify = false;
    // View Camera Or Gallery
    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    // arrays of permission to be request
    private String cameraPermissions[];
    private String storagePermissions[];
    // Uri Image
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        // Init View
        recyclerViewChatUsers = findViewById(R.id.recyclerChatUser);
        photoUserChat = findViewById(R.id.photoUserChat);
        nameUserChat = findViewById(R.id.nameChatUser);
        messageChatUser = findViewById(R.id.messageChat);
        btnSendChat = findViewById(R.id.buttonSendChat);
        statusUserChat = findViewById(R.id.statusChatUser);
        btnAttachImage = findViewById(R.id.attachImage);

        // init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Layout Linear Manager to RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        // RecyclerView Properties
        recyclerViewChatUsers.setHasFixedSize(true);
        recyclerViewChatUsers.setLayoutManager(linearLayoutManager);
        // Init Intent
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("TBL_USERS");

        // Search User To Get Info
        Query userQuery = mReference.orderByChild("uid").equalTo(hisUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String name = ""+snapshot.child("nameUser").getValue();
                    hisImage = ""+snapshot.child("photoUser").getValue();
                    String typingStatus = ""+snapshot.child("typingTo").getValue();

                    //check typing status
                    if (typingStatus.equals(myUid)){
                        statusUserChat.setText("typing...");
                    }else{
                        // get value online status
                        String onlineStatus = "" + snapshot.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            statusUserChat.setText(onlineStatus);
                        }else{
                            // convert time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"), Locale.getDefault());
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyy H:mm", calendar).toString();
                            statusUserChat.setText("Last Seen At : "+dateTime);

                        }
                    }

                    // set data
                    nameUserChat.setText(name);

                    // if image equal no image
                    if (!hisImage.equals("noImage")){
                        try {
                            // if image success set
                            Picasso.get().load(hisImage).resize(50, 50).centerCrop().into(photoUserChat);
                        }
                        catch (Exception e){
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // click button to import image
        btnAttachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show image pick dialog
                showImagePickDialog();
            }
        });

        //click to send button
        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;

                String message = messageChatUser.getText().toString().trim();

                // check if text is empty or not
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(ChatUsersActivity.this, "Cant send empty message", Toast.LENGTH_SHORT).show();
                }else{
                    // message ready
                    sendMessage(message);
                }
                messageChatUser.setText("");
            }
        });

        // check edit text change listener
        messageChatUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        readMessages();

        seenMessage();
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
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
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

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("TBL_CHATS");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelChatUsers chats = snapshot.getValue(ModelChatUsers.class);
                    if (chats.getReceiver().equals(myUid) && chats.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSeenMap = new HashMap<>();
                        hasSeenMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hasSeenMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        modelChatUsersList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_CHATS");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelChatUsersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelChatUsers chats = snapshot.getValue(ModelChatUsers.class);
                    if (chats.getReceiver().equals(myUid) && chats.getSender().equals(hisUid) ||
                            chats.getReceiver().equals(hisUid) && chats.getSender().equals(myUid)){
                        modelChatUsersList.add(chats);
                    }

                    //adapter
                    adapterChatsUsers = new AdapterChatsUsers(ChatUsersActivity.this, modelChatUsersList, hisImage);
                    adapterChatsUsers.notifyDataSetChanged();
                    // set adapter to recylerview
                    recyclerViewChatUsers.setAdapter(adapterChatsUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");

        databaseReference.child("TBL_CHATS").push().setValue(hashMap);

        String msg = message;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS").child(myUid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelMyListUsers users = dataSnapshot.getValue(ModelMyListUsers.class);

                if (notify){
                    sendNotification(hisUid, users.getNameUser(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Create chatlist to database
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("TBL_CHATLIST")
                .child(myUid)
                .child(hisUid);

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("TBL_CHATLIST")
                .child(hisUid)
                .child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendImageMessage(Uri imageUri) throws IOException {
        notify = true;

        // progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending Image...");
        progressDialog.show();

        final String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post"+timeStamp;

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
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timeStamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);

                            // put this data to firebase
                            databaseReference.child("TBL_CHATS").push().setValue(hashMap);

                            DatabaseReference dbRef  = FirebaseDatabase.getInstance().getReference("TBL_USERS").child(myUid);
                            dbRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ModelMyListUsers mUser = dataSnapshot.getValue(ModelMyListUsers.class);

                                    if (notify){
                                        sendNotification(hisUid, mUser.getNameUser(), "Sent Photo You...");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            // Create chatlist to database
                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("TBL_CHATLIST")
                                    .child(myUid)
                                    .child(hisUid);

                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("TBL_CHATLIST")
                                    .child(hisUid)
                                    .child(myUid);

                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        chatRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void sendNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(
                            ""+myUid,
                            ""+name+":"+message,
                            "New Message",
                            ""+hisUid,
                            "ChatNotification",
                            R.drawable.icon_chat_user_grey);

                    Sender sender = new Sender(data, token.getToken());
                    // fcm json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // response of the request
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                // put params
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-type", "application/json");
                                headers.put("Authorization", "key=AAAAqQk6qcQ:APA91bE6Ix0Gi39HgAGfrpq6_22FG3NTotTqcX-nOIv-u9UWFeORw8cUSem0pngY-PIwoYmrH6P-UDxz8D2VKTfHwK6hsdWtW1r-hty9GH35fUmnxnks2BSHuF-nXpMSFsh01qrp_rC1");

                                return headers;
                            }
                        };
                        // add this request queue
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
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

    private void checkOnlineStatus(String status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        // update value online status
        reference.updateChildren(hashMap);
    }

    private void checkTypingStatus(String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_USERS").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", type);
        // update value online status
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        // set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // timestamp
        String timeStamp = String.valueOf(System.currentTimeMillis());
        // set offline with last time stamp
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        // set online
        checkOnlineStatus("online");
        super.onResume();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
