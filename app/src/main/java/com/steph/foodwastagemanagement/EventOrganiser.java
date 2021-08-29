package com.steph.foodwastagemanagement;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class EventOrganiser extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    private DatabaseReference databaseRef;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth fAuth;
    private FirebaseUser user;
    private EditText date, location, plates, placeID;
    private TextView surplus, greetingTextView;
    private int mYear;
    private int mMonth;
    private int mDay;
    RadioButton yes, no;
    Button logout, AddEvent;
    RadioGroup radioGroup;
    Spinner event_spinner;
    private String mSpinnerLabel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_organiser);
        logout = (Button) findViewById(R.id.signOut);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(EventOrganiser.this, MainActivity.class));
            }
        });


//storing data in the database
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Events");
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());


        yes = (RadioButton) findViewById(R.id.yes);
        no = (RadioButton) findViewById(R.id.no);

        date = findViewById(R.id.date);
        date.setOnClickListener(this);
        location = findViewById(R.id.location);
        plates = findViewById(R.id.plates);
        radioGroup = findViewById(R.id.radioGroup);
        placeID = findViewById(R.id.place);
        //Place API
        Places.initialize(getApplicationContext(), "AIzaSyAW9LNf5vfBWmGsfn8f7lfI867RNsgkh1A");
        placeID.setFocusable(false);
        placeID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(EventOrganiser.this);
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            placeID.setText(place.getAddress());
            location.setText(String.format("Locality Name : %s", place.getName()));

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }


    // event_category = findViewById(R.id.event_category);
        Spinner event_spinner = findViewById(R.id.event_spinner);
        if (event_spinner != null) {
            event_spinner.setOnItemSelectedListener(this);


        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_label, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (event_spinner != null) {
           event_spinner.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == date) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date.setText(dayOfMonth + "-" + (month + 1 + "_" + year));

                }
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }



    public void AddEvent(View view) {
        String Availability = "";
        //final String EventType = event_category.getText().toString().trim();
        final String Location = location.getText().toString().trim();
      // final String EventType= (String) event_spinner.getSelectedItem();
        final String Plates = plates.getText().toString().trim();
        final long Date = date.getDrawingTime();


       /* if (EventType.isEmpty()) {
            event_category.setError("Event type is required");
            event_category.requestFocus();
            return;
        }

        if (Location.isEmpty()) {
            location.setError("Location is required");
            location.requestFocus();
        }
        if (Date.isEmpty()) {
            date.setError("Date is required");
            date.requestFocus();
            return;
        }

         */
        if (yes.isChecked()) {
            Availability = "Yes";


        } else {
            Availability = "No";
        }
        String finalAvailability = Availability;

        final DatabaseReference newEvent = databaseRef.push();
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                newEvent.child("Event ").setValue(mSpinnerLabel);
                newEvent.child("Location").setValue(Location);
                newEvent.child("Date").setValue(Date);
                newEvent.child("uid").setValue(user.getUid());
                newEvent.child(" Plates").setValue(Plates);
                newEvent.child(" Availability").setValue(finalAvailability);
                newEvent.child("PhoneNumber").setValue(snapshot.child("PhoneNumber").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(EventOrganiser.this, "Event Added Successsfuly", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
        mSpinnerLabel=adapterView.getItemAtPosition(i).toString();
        Toast myToast=Toast.makeText(this,"Selected event as:"+mSpinnerLabel,Toast.LENGTH_SHORT);
        myToast.show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast toast=Toast.makeText(this,"nothing selected",Toast.LENGTH_SHORT);
        toast.show();

    }
}
















