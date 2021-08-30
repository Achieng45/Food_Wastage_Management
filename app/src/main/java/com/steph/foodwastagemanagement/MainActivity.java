package com.steph.foodwastagemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "YOUR-TAG-NAME";

    private TextView register, forgotpassword;
    EditText txt_email, txt_password;
    Button signIn;
    RadioButton mevent, mrestaurant, mchildren;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);


        //signIn = (Button) findViewById(R.id.login_button);
        //signIn.setOnClickListener(this);

        txt_email = (EditText) findViewById(R.id.login_email);
        txt_password = (EditText) findViewById(R.id.login_password);

        forgotpassword = (TextView) findViewById(R.id.forgotpassword);
        forgotpassword.setOnClickListener(this);


        mrestaurant = (RadioButton) findViewById(R.id.restaurant);
        mevent = (RadioButton) findViewById(R.id.event);
        mchildren = (RadioButton) findViewById(R.id.children);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

    }
        //set onclick listeners
        @Override
        public void onClick (View v){
            int id = v.getId();
            if (id == R.id.register) {
                startActivity(new Intent(this, RegisterUser.class));
            } else if (id == R.id.forgotpassword) {
                startActivity(new Intent(this, ForgotPassword.class));
            } else if (id == R.id.login_button) {
                Toast.makeText(MainActivity.this, "PROCESSING....",
                        Toast.LENGTH_LONG).show();
                // get the email and password entered by the user

                String email = txt_email.getText().toString().trim();
                String password = txt_password.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    // use firebase authentication instance you create and call the method  signInWithEmailAndPassword method passing the email and password you got from the views   //Further call the addOnCompleteListener() method to handle the  Authentication result
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    checkUserExistence();

                                } else {
                                    //if the user does not exist in the database reference throw a toast
                                    Toast.makeText(MainActivity.this, "Please verify your email before login", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } else {
                    //if the fields for email and password were not completed show a toast
                    Toast.makeText(MainActivity.this, "Fields not complete or invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }


        }


        //check if the user exists and redirect to their role
        public void checkUserExistence () {
            //check the user existence of the user using the user id in users database reference
            final String user_id = mAuth.getCurrentUser().getUid();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference uidRef = rootRef.child("Users").child(user_id);
            //call the method addValueEventListener on the database reference of the user to determine  if the current userID supplied exists in our database reference
            uidRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //get a dataSnapshot of the users database reference to determine if current user exists

                    //if the users exists direct the user to the Main Activity
                    DatabaseReference uidRef = rootRef.child("Users").child(user_id);
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        final TextView greetingTextView = (TextView) findViewById(R.id.greeting);

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            String position = dataSnapshot.child("Position").getValue().toString();
                            if (position.equals("Event manager")) {
                                startActivity(new Intent(MainActivity.this, EventOrganiser.class));

                            } else if (position.equals("Hotel manager")) {
                                startActivity(new Intent(MainActivity.this, RestaurantManager.class));
                            } else if (position.equals("Children's home manager")) {
                                startActivity(new Intent(MainActivity.this, ChildrensHome.class));
                            } else {
                                Toast.makeText(MainActivity.this, position, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, databaseError.getMessage());
                        }
                    };
                    uidRef.addListenerForSingleValueEvent(valueEventListener);
                    Toast.makeText(MainActivity.this, user_id, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


    }













