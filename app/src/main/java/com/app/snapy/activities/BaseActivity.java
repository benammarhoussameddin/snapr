package com.app.snapy.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.snapy.R;
import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {
    protected ProgressDialog progressDialog;

    public void showProgressDialog(String message) {
       progressDialog = new ProgressDialog(this);
       progressDialog.setCancelable(false);
       progressDialog.setMessage(message);
       progressDialog.show();

    }
    public void closeProgressDialog() {
        progressDialog.dismiss();
    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
