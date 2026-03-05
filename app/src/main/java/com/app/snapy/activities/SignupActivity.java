package com.app.snapy.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.app.snapy.R;
import com.app.snapy.databinding.ActivitySignupBinding;
import com.app.snapy.fierbaseutils.FirebaseSignUpUtils;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class SignupActivity extends BaseActivity implements FirebaseSignUpUtils.OnSignUpRequestCompleteListener {
    String email, password, confirmPassword;
    ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allValuesEntered()) {
                    showProgressDialog("Creating your account");
                    FirebaseSignUpUtils.getInstance().setListener(SignupActivity.this).signUpAndUploadData(email, password);
                }
            }
        });//end of btnSignup
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();
                String confirmPass = binding.etConfirmPassword.getText().toString();
                matchPasswords(password, confirmPass);
            }
        });
        binding.etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();
                String confirmPass = binding.etPassword.getText().toString();
                matchPasswords(password, confirmPass);
            }
        });
        binding.etAlreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

    }//

    @Override
    public void onSuccess() {
        closeProgressDialog();
        finish();
    }//end of onSuccess

    @Override
    public void onFailure(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            showToast("User with this email already exists");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            if (e instanceof FirebaseAuthWeakPasswordException) {
                showToast("Please enter a strong password");
            } else {
                showToast("Please enter a valid email");
            }
        } else {
            e.printStackTrace();
            showToast("Some error occurred");
        }
        closeProgressDialog();
    }//end of onFailure

    private boolean allValuesEntered() {
        email = binding.etEmail.getText().toString().trim();
        password = binding.etPassword.getText().toString().trim();
        confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        if (email.length() == 0 || password.length() == 0 || confirmPassword.length() == 0) {
            showToast("Please enter all fields");
            return false;
        }

        return true;
    }//end of allValuesEntered

    private void matchPasswords(String password, String confirmPass) {
        if (!password.equals(confirmPass)) {
            //binding.passwordNotMatchTv.setVisibility(View.VISIBLE);
            binding.btnSignUp.setEnabled(false);
            binding.btnSignUp.setAlpha(0.5f);
        } else {
            //binding.passwordNotMatchTv.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setEnabled(true);
            binding.btnSignUp.setAlpha(1);
        }
    }//end of matchPassword
}