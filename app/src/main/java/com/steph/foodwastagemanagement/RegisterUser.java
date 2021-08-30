package com.steph.foodwastagemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {
    private TextView register;
    EditText txt_fullName, txt_email, txt_password, phone_number;
    ProgressBar ProgressBar;
    FirebaseAuth mAuth;
    RadioButton mevent, mrestaurant, mchildren;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    String position = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        // banner = (TextView) findViewById(R.id.banner);
        // banner.setOnClickListener(this);

        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(this);

        txt_fullName = (EditText) findViewById(R.id.txt_fullName);
        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_password = (EditText) findViewById(R.id.txt_password);
        phone_number = (EditText) findViewById(R.id.phone_number);
        ProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        mrestaurant = (RadioButton) findViewById(R.id.restaurant);
        mevent = (RadioButton) findViewById(R.id.event);
        mchildren = (RadioButton) findViewById(R.id.children);
        databaseReference = FirebaseDatabase.getInstance().getReference("User");


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // case R.id.banner:
            //startActivity(new Intent(this, MainActivity.class));
            //break;
            case R.id.register:
                register();
                break;
        }

    }

    private void register() {


        String position = "";
        String email = txt_email.getText().toString().trim();
        String password = txt_password.getText().toString().trim();
        String fullName = txt_fullName.getText().toString().trim();
        String phonenumber = phone_number.getText().toString().trim();
        if (fullName.isEmpty()) {
            txt_fullName.setError("Full name is required");
            txt_fullName.requestFocus();
            return;
        }
        if (phonenumber.isEmpty()) {
            phone_number.setError("Phone number is required");
            phone_number.requestFocus();
        }
        if (email.isEmpty()) {
            txt_email.setError("Email is required");
            txt_email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txt_email.setError("please provide a valid email");
            txt_email.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            txt_password.setError("password is required");
            txt_password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            txt_password.setError("Min password length should be 6 characters!");
            txt_password.requestFocus();
            return;
        }

        if (mevent.isChecked()) {
            position = "Event manager";

        } else if (mrestaurant.isChecked()) {
            position = "Hotel manager";
        } else {
            position = "Children's home manager";
        }
        ProgressBar.setVisibility(View.VISIBLE);
        String finalPosition = position;


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Trial
                    FirebaseUser userm = mAuth.getCurrentUser();
                    userm.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {                            ProgressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterUser.this, "user has been registered successfully! Please check your email for verification", Toast.LENGTH_LONG).show();
                                txt_email.setText("");
                                txt_password.setText("");
                                //Adding user details
                                finish();
                                startActivity(new Intent(RegisterUser.this, MainActivity.class));
                                Users user = new Users(fullName, email, finalPosition, phonenumber);
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterUser.this, "", Toast.LENGTH_LONG).show();
                                            ProgressBar.setVisibility(View.GONE);

                                        } else {
                                            Toast.makeText(RegisterUser.this, "Failed to register!Try again", Toast.LENGTH_LONG).show();
                                            ProgressBar.setVisibility(View.GONE);
                                        }

                                    }
                                });

                            } else {
                                Toast.makeText(RegisterUser.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterUser.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}



