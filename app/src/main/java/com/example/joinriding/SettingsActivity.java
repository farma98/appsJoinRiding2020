package com.example.joinriding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Switch postSwitch;
    private TextView changePassword;
    private SweetAlertDialog sweetAlertDialog;

    // use shared preferences to save of switch
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Constant for topic
    private static  final String TOPIC_POST_NOTIF = "POST"; // assigned any value but user same for this kind on notif

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        postSwitch  = findViewById(R.id.postSwitch);
        changePassword = findViewById(R.id.txtChangePassword);

        // init shared preferences
        sharedPreferences = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnable = sharedPreferences.getBoolean(""+TOPIC_POST_NOTIF, false);

        // Action bar and ist title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Setting Notification");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));

        // if enabled check switch, unhcheked
        if (isPostEnable){
            postSwitch.setChecked(true);
        }else{
            postSwitch.setChecked(false);
        }

        // implement switch changelistener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                // edit switch state
                editor = sharedPreferences.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIF, isChecked);
                editor.apply();

                if (isChecked){
                    subPostNotif();
                }else{
                    unsubPostNotif();
                }
            }
        });

        // handle txt change password click
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });
    }

    private void showChangePasswordDialog() {
        // inflate layout for dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_update_password, null);
        final EditText passwordOld = view.findViewById(R.id.passwordInput1);
        final EditText passwordNew = view.findViewById(R.id.passwordInput2);
        Button updatePasswordBtn = view.findViewById(R.id.buttonChangePassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate data
                String oldPassword = passwordOld.getText().toString().trim();
                String newPassword = passwordNew.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(SettingsActivity.this, "Enter Your Old Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(SettingsActivity.this, "Password Must Be 6 Character", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        sweetAlertDialog.show();

        final FirebaseUser fuser = mAuth.getCurrentUser();
        // before change password re-validation account user
        AuthCredential authCredential = EmailAuthProvider.getCredential(fuser.getEmail(), oldPassword);
        fuser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fuser.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sweetAlertDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Password Was Updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sweetAlertDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "Password Was Not Updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubPostNotif() {
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIF)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You Will Not Activate Post Notif";

                        if (!task.isSuccessful()){
                            msg = "UnSub Failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subPostNotif() {
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIF)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You Will Activate Post Notif";

                        if (!task.isSuccessful()){
                            msg = "Sub Failed";
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}