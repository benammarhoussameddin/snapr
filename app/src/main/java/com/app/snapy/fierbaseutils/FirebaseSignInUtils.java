package com.app.snapy.fierbaseutils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.app.snapy.R;
import com.app.snapy.activities.BaseActivity;
import com.app.snapy.activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseSignInUtils {

    private FirebaseAuth mAuth;
    private static FirebaseSignInUtils sInstance;
    private OnSignInRequestCompleteListener listener;


    public void signInWithEmailAndPassword(String email, String password, Context context) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getUser().isEmailVerified() || mAuth.getUid().equals(context.getString(R.string.admin_uid))) {
                        listener.onSuccess();
                    } else {
                        FirebaseSignUpUtils.sendVerificationEmail((BaseActivity) listener, null);
                    }
                } else {
                    listener.onFailure(task.getException());
                }
            }
        });
    }


    public static FirebaseSignInUtils getInstance() {
        if (sInstance != null) return sInstance;
        sInstance = new FirebaseSignInUtils();
        return sInstance;
    }

    public FirebaseSignInUtils setListener(OnSignInRequestCompleteListener listener) {
        sInstance.listener = listener;
        return sInstance;
    }

    public void sendPasswordResetLink(String email, LoginActivity activity) {
        activity.showProgressDialog("Please Wait...");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String message = "A password reset link has been sent to your email, use that to reset your password and then login";
                    FirebaseSignUpUtils.showAlertDialog(activity, message, null);
                } else {
                    Toast.makeText(activity, "Unable to send password reset email", Toast.LENGTH_SHORT).show();
                    task.getException().printStackTrace();
                }
                activity.closeProgressDialog();
            }
        });
    }

    public interface OnSignInRequestCompleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    private FirebaseSignInUtils() {
        mAuth = FirebaseAuth.getInstance();
    }


}
