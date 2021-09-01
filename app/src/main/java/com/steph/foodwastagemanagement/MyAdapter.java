package com.steph.foodwastagemanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.steph.foodwastagemanagement.SendNotificationPack.APIService;
import com.steph.foodwastagemanagement.SendNotificationPack.Client;
import com.steph.foodwastagemanagement.SendNotificationPack.Data;
import com.steph.foodwastagemanagement.SendNotificationPack.MyResponse;
import com.steph.foodwastagemanagement.SendNotificationPack.NotificationSender;
import com.steph.foodwastagemanagement.SendNotificationPack.Token;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private static APIService apiService;
    static ArrayList<Event> mList;
    private static DatabaseReference databaseRef;
    private String managerID;
    private static String eventID;
    private static DatabaseReference mDatabaseUsers;
    private static FirebaseAuth fAuth;
    private static FirebaseUser user;


    static Context context;
    private ProgressBar spinner;

    public MyAdapter(Context context, ArrayList<Event> mList) {
        this.mList = mList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        return new MyViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Event event = mList.get(position);
        holder.Location.setText(event.getLocation());


        holder.Plates.setText(event.getPlates());

        holder.Date.setText(String.valueOf(event.getDate()));
        holder.Events.setText(event.getEvent());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Location, Date, Plates, Events;
        Button Book;
        private ProgressBar spinner;
        private String Message, Title;
        private static String managerID;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Location = itemView.findViewById(R.id.location);
            Date = itemView.findViewById(R.id.date);
            Events = itemView.findViewById(R.id.eventtype);
            Plates = itemView.findViewById(R.id.plates);
            Book = itemView.findViewById(R.id.book);
            spinner = (ProgressBar) itemView.findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            Book.setOnClickListener(new View.OnClickListener() {
                //this will be automatically updated to the name of the current user in the code below
                String name = "Full Name";
                String number = "Phone Number";

                @Override
                public void onClick(View v) {
                    spinner.setVisibility(View.VISIBLE);
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference root = db.getReference().child("Events");
                    FirebaseAuth mAuth = null;
                    FirebaseUser user;
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    int position = getAdapterPosition();
                    DatabaseReference uref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                    //id for event manager
                    managerID = mList.get(position).getuid();
                    eventID = mList.get(position).getEventID();


                    user = FirebaseAuth.getInstance().getCurrentUser();
                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(managerID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String usertoken = dataSnapshot.getValue(String.class);
                            uref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ValueEventListener valueEventListener = new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            //add implementation for event booking here
                                            BookEvent(eventID,managerID);
                                            name = dataSnapshot.child("fullName").getValue().toString();
                                            number = dataSnapshot.child("PhoneNumber").getValue().toString();
                                            Title = name;
                                            Message = number + "" + name + " has booked for the " + mList.get(position).getEvent() + " event in" + mList.get(position).getLocation();
                                            sendNotifications(usertoken, Title, Message);
                                            Toast toast = Toast.makeText(context.getApplicationContext(), "Sending", Toast.LENGTH_LONG);
                                            toast.show();
                                            spinner.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.d("Error", databaseError.getMessage());
                                            spinner.setVisibility(View.GONE);
                                        }
                                    };
                                    uref.addListenerForSingleValueEvent(valueEventListener);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    spinner.setVisibility(View.GONE);
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast toast = Toast.makeText(context.getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                            spinner.setVisibility(View.GONE);
                        }
                    });
                    UpdateToken();
                }
            });

        }

        private static void UpdateToken() {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String refreshToken = FirebaseInstanceId.getInstance().getToken();
            Token token = new Token(refreshToken);
            FirebaseDatabase.getInstance().getReference("Tokens").child(managerID).setValue(token);
        }

        public static void sendNotifications(String usertoken, String title, String message) {
            Data data = new Data(title, message);
            NotificationSender sender = new NotificationSender(data, usertoken);
            apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                    if (response.code() == 200) {
                        if (response.body().success != 1) {
                            Toast.makeText(context.getApplicationContext(), "Failed ", Toast.LENGTH_LONG);
                        } else {
                            Toast toast = Toast.makeText(context.getApplicationContext(), "Sent: " + response.message(), Toast.LENGTH_LONG);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(context.getApplicationContext(), "Error: " + response.code(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<MyResponse> call, Throwable t) {
                    Toast toast = Toast.makeText(context.getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

        }

        public void BookEvent(String eventID, String managerID) {


            databaseRef = FirebaseDatabase.getInstance().getReference().child("Bookings");
            fAuth = FirebaseAuth.getInstance();
            user = fAuth.getCurrentUser();
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

            final DatabaseReference newBooking = databaseRef.push();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {


                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    String date = dtf.format(now);

                    newBooking.child("Manager").setValue(managerID);
                    newBooking.child("eventsID").setValue(eventID);
                    newBooking.child("Date").setValue(date);
                    newBooking.child("Status").setValue("Booked");
                    newBooking.child("userID").setValue(user.getUid());
                    newBooking.child("eventID").setValue(snapshot.child("eventID").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context.getApplicationContext(), "Event booked", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }


            });


        }


    }
}
