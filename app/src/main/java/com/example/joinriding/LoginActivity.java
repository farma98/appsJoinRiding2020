package com.example.joinriding;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {
    // Declare View
    private EditText edtEmail, edtPassword;
    private TextView noHaveAccount, recoveryPassword;
    private Button buttonLogin;

    // Declare Firebase
    private FirebaseAuth mAuth;

    // Declare Progress Dialog
    private SweetAlertDialog sweetAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Action bar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Init View
        edtEmail = findViewById(R.id.edtEmailInput);
        edtPassword = findViewById(R.id.edtPasswordInput);
        noHaveAccount = findViewById(R.id.txtIntentRegister);
        recoveryPassword = findViewById(R.id.txtForgetPassword);
        buttonLogin = findViewById(R.id.btnLoginUser);

        // Init Progress Dialog
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));

        // Init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Handle Button Login Click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input Email & Password
                String Email = edtEmail.getText().toString();
                String Password = edtPassword.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                    // Set Error & Focus To Input Email
                    edtEmail.setError("Invalid Email");
                    edtPassword.setFocusable(true);
                }else if(Password.length()<6){
                    // Set Error & Focus To Input Password
                    edtPassword.setError("Invalid Passoword");
                    edtPassword.setFocusable(true);
                }else{
                    // invalid email pattern
                    loginUser(Email, Password);
                }
            }
        });

        // Handle Text View No Have Account
        noHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        // Recover Forget Password
        recoveryPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

    }

    private void showRecoverPasswordDialog() {
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password...");

        // Set Layout Linear
        LinearLayout linearLayout = new LinearLayout(this);

        // View To Set Dialog
        final EditText edtEmail = new EditText(this);
        edtEmail.setHint("Email");
        edtEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Sets The Min Width
        edtEmail.setMinEms(16);

        linearLayout.addView(edtEmail);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        // Handle Text View Recover Click
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Input Email Recover
                String Email = edtEmail.getText().toString().trim();
                beginRecovery(Email);
            }
        });

        // Handle Text View Cancel Click
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // dismiss dialog
                sweetAlertDialog.dismiss();
            }
        });

        // show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        // Show Progress Dialog
        sweetAlertDialog.setTitleText("Sending Email...");
        sweetAlertDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sweetAlertDialog.dismiss();
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Email Send", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "Failed Send..", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        // Show Progress Dialog
        sweetAlertDialog.setTitleText("Login...");
        sweetAlertDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign In Success, update UI with the signed-in user's information
                            sweetAlertDialog.dismiss();

                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If Sign In Failed, display a message to the user.
                            sweetAlertDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go Previous Activity
        return super.onSupportNavigateUp();
    }
}
