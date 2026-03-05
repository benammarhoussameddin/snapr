package com.app.snapy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.app.snapy.R;
import com.app.snapy.databinding.ActivityLoginBinding;
import com.app.snapy.fierbaseutils.FirebaseSignInUtils;
import com.app.snapy.fierbaseutils.FirebaseSignUpUtils;
import com.app.snapy.fierbaseutils.FirebaseSignUpUtils.OnSignUpRequestCompleteListener;
import com.app.snapy.models.User;
import com.app.snapy.utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import kotlinx.coroutines.sync.SemaphoreKt;

public class LoginActivity extends BaseActivity implements FirebaseSignInUtils.OnSignInRequestCompleteListener, OnSignUpRequestCompleteListener {
    ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;
    String email, password;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        firebaseAuth = FirebaseAuth.getInstance();
        // If user is already signed in, redirect to main activity
        if (firebaseAuth.getCurrentUser() != null && (firebaseAuth.getCurrentUser().isEmailVerified() || firebaseAuth.getUid().equals(getString(R.string.admin_uid)))) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        setListeners();
    }

    private void initViews() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Utils.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);
    }

    private void setListeners() {
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allValuesEntered()) {
                    showProgressDialog("Signing In");
                    FirebaseSignInUtils.getInstance().setListener(LoginActivity.this).signInWithEmailAndPassword(email, password, LoginActivity.this);
                }
            }
        });//end
        binding.btnGamailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                // Start activity for result
               startActivityForResult(intent, 100);
            }
        });
        binding.tvNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        binding.forgotPassTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                email = binding.etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseSignInUtils.getInstance().sendPasswordResetLink(email, LoginActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if (requestCode == 100) {
            showProgressDialog("Signing In...");
            // When request code is equal to 100 initialize task
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            // check condition
            if (signInAccountTask.isSuccessful()) {
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        // Check credential
                        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Check condition
                                if (task.isSuccessful()) {
                                    // When task is successful, check if its a new user, and upload data if it is
                                    if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                        User user = new User(firebaseAuth.getUid(), googleSignInAccount.getEmail(), googleSignInAccount.getPhotoUrl().toString());
                                        FirebaseSignUpUtils.getInstance().setListener(LoginActivity.this).uploadUserData(user);
                                    } else {
                                        startMainActivity();
                                    }
                                } else {
                                    // When task is unsuccessful display Toast
                                    showToast("Authentication Failed :" + task.getException().getMessage());
                                }

                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean allValuesEntered() {
        email = binding.etEmail.getText().toString();
        password = binding.etPassword.getText().toString();
        if (email.length() == 0 || password.length() == 0) {
            showToast("Please enter all required values");
            return false;
        }
        return true;
    }

    @Override
    public void onSuccess() {
        startMainActivity();
    }

    @Override
    public void onFailure(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            showToast("Either the email is invalid or password is incorrect");
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            showToast("No account found for this email");
        } else {
            showToast("Some error has occurred");
            e.printStackTrace();
        }
        closeProgressDialog();
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        showToast("Successfully logged in");
        closeProgressDialog();
        finish();
    }
}