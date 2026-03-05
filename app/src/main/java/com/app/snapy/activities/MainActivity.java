package com.app.snapy.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.snapy.R;
import com.app.snapy.adapters.PostAdapter;
import com.app.snapy.databinding.ActivityMainBinding;
import com.app.snapy.fierbaseutils.FirebaseUtils;
import com.app.snapy.models.Post;
import com.app.snapy.models.User;
import com.app.snapy.utils.LinearLayoutManagerWrapper;
import com.app.snapy.utils.PostStatus;
import com.app.snapy.utils.Session;
import com.app.snapy.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final int READ_STORAGE_PERMISSION_REQUEST = 123;
    private static final int PICK_IMAGE_REQUEST = 1;
    Uri imagefilePath;
    private Uri uri;
    Bitmap bitmap;
    ActivityMainBinding binding;
    View profile_header_view;
    PostAdapter postAdapter; // Create Object of the Adapter class
    DatabaseReference postReference;
    FirebaseRecyclerOptions<Post> options;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    List<Post> postList;
    private ActivityResultLauncher<Intent> getImageUriFromGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        setListeners();
        loadCurrentUser();
    }

    private void setListeners() {
        getImageUriFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null) {
                    imagefilePath = result.getData().getData();
                    Picasso.get().load(imagefilePath).into(binding.profileImage);
                    saveProfileImage();
                }
            }
        });
        binding.ivAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddPostActivity.class));
            }
        });
        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("image is selected...");
                if (hasGalleryPermissions(MainActivity.this)) {
                    getImageFromGallery(getImageUriFromGallery);
                }
            }
        });
        binding.tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        setTitle("");
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout,
                R.string.profile_open, R.string.profile_close);
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //set the profile email
        binding.tvEmailProfile.setText(FirebaseUtils.getUserEmail());
        //set the profile name
        binding.tvProfileName.setText(FirebaseUtils.getUserName());

        //fethch the firebase posts rows
        postReference = FirebaseDatabase.getInstance().getReference("posts");
        binding.recyclerview.setLayoutManager(new LinearLayoutManagerWrapper(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        binding.recyclerview.setAdapter(postAdapter);
    }

    private void loadAllPost() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    if (Utils.isAdmin(MainActivity.this)) {
                        if (!post.getStatus().equals(PostStatus.APPROVED + "")) {
                            postList.add(post);
                        }
                    } else {
                        if (post.getStatus().equals(PostStatus.APPROVED + "")) {
                            postList.add(post);
                        }
                    }

                    Collections.reverse(postList);
                    postAdapter.notifyDataSetChanged();
                }
                closeProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error");
                closeProgressDialog();
            }
        });
    }

    private void saveProfileImage() {
        final ProgressDialog dialog = new ProgressDialog(this);
        showProgressDialog("Profile is uploading...");
        dialog.show();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersNode = db.getReference("users");
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference("Image_Profile" + Utils.random());
        storageReference.putFile(imagefilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Session.CURRENT_USER.setProfileUrl(uri.toString());
                        usersNode.child(FirebaseUtils.getUserUuid()).setValue(Session.CURRENT_USER).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                                showToast("Profile Image successfully uploaded");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                showToast("Failed to upload profile image");
                            }
                        });
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                float percent = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                dialog.setMessage("Uploaded :" + (int) percent + " %");
            }
        });
    }

    private void loadCurrentUser() {
        showProgressDialog("Loading...");
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseUtils.getUserUuid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Session.CURRENT_USER = snapshot.getValue(User.class);
                if (Session.CURRENT_USER.getProfileUrl() != null) {
                    if (!Session.CURRENT_USER.getProfileUrl().equals("")) {
                        Picasso.get().load(Session.CURRENT_USER.getProfileUrl()).into(binding.profileImage);
                    }
                }
                loadAllPost();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                closeProgressDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }// end pf onBackPressed

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.profileImage:
                showToast("profile Image is selected...");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean hasGalleryPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (activity.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                // Ask for Permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
                return false;
            }
        } else {
            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                // Ask for Permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
    }

    protected void getImageFromGallery(ActivityResultLauncher<Intent> getImageUriFromGallery) {
        Intent intentGalley = new Intent(Intent.ACTION_PICK);
        intentGalley.setType("image/*");
        getImageUriFromGallery.launch(intentGalley);
    }

}