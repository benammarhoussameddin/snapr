package com.app.snapy.fierbaseutils;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.snapy.activities.BaseActivity;
import com.app.snapy.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.security.PublicKey;

public class FirebaseUtils  {
    public interface ResponseListener {
        void onSuccess(DataSnapshot snapshot);
        void onFailure(String error);
    }
    public static String getUserEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }
    public static String getUserName(){
        String string = getUserEmail();
        String[] userName = string.split("@");
        return userName[0];

    }
    public static String getUserUuid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static void getUser(String uid, ResponseListener listener){
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference usersNode=db.getReference("users/"+uid);
        usersNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onSuccess(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.toString());
            }
        });

    }



   /* public static String getUserProfileLink(){
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference usersNode=db.getReference("users/"+getUserUuid()+"/profileUrl");
        usersNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CURRENT_USER_PROFILE_URL =  snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return CURRENT_USER_PROFILE_URL;
    }*/

    public static void updatePostStatus(String postID,String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(postID).child("status").setValue(status);
                Log.d("POST","Value is updated....");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("POST","Not updated");
            }
        });

    }
}
