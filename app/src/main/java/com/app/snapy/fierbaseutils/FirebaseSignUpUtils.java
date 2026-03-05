package com.app.snapy.fierbaseutils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.app.snapy.activities.BaseActivity;
import com.app.snapy.activities.LoginActivity;
import com.app.snapy.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseSignUpUtils {
    public interface OnSignUpRequestCompleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;
    private DatabaseReference mRef;
    private static FirebaseSignUpUtils sInstance;
    private OnSignUpRequestCompleteListener listener;

    public static FirebaseSignUpUtils getInstance() {
        if (sInstance != null) return sInstance;
        sInstance = new FirebaseSignUpUtils();
        return sInstance;
    }

    private FirebaseSignUpUtils() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mRef = mDb.getReference();
    }

    public void signUpAndUploadData(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User();
                    user.setEmail(mAuth.getCurrentUser().getEmail());
                    user.setUid(mAuth.getCurrentUser().getUid());
                    uploadUserData(user);
                } else {
                    listener.onFailure(task.getException());
                }
            }
        });
    }

    public void uploadUserData(User user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("users").child(mAuth.getCurrentUser().getUid());
        databaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (listener instanceof LoginActivity) {  // If listener is instance of Login activity, it means that user data from google signup was uploaded, so we don't need to send verification message
                        listener.onSuccess();
                    } else {
                        sendVerificationEmail((BaseActivity) listener, listener);
                    }
                } else {
                    listener.onFailure(task.getException());
                }
            }
        });
    }

    public FirebaseSignUpUtils setListener(OnSignUpRequestCompleteListener listener) {
        sInstance.listener = listener;
        return sInstance;
    }

    public static void sendVerificationEmail(BaseActivity activity, OnSignUpRequestCompleteListener listener) {
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String message;
                if (task.isSuccessful()) {

                    // If listener is not null, it means that verification email has been sent after signup
                    if (listener != null) {
                        message = "Your account has been created and a verification link has been sent to your email\nPlease verify your email and then login.";
                    } else {
                        message = "Your email is not verified, a verification link has been sent to your email.\nPlease verify your email and then try again.";
                    }
                } else {
                    message = "Your email is not verified, and we were unable to send a verification email, please try again.";
                }
                showAlertDialog(activity, message, listener);
            }
        });
    }

    public static void showAlertDialog(BaseActivity activity, String message, OnSignUpRequestCompleteListener listener) {
        new AlertDialog.Builder(activity).setCancelable(false)
                .setMessage(message)
                .setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (listener != null) { // This listener will not be null when the dialog will be shown in sign up activity
                            listener.onSuccess();
                        }
                        activity.closeProgressDialog();
                    }
                }).show();
    }
}
