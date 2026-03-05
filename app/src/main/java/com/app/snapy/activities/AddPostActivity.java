package com.app.snapy.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

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
import android.view.View;
import android.widget.RatingBar;

import com.app.snapy.databinding.ActivityAddPostBinding;
import com.app.snapy.fierbaseutils.FirebaseUtils;
import com.app.snapy.models.Post;
import com.app.snapy.utils.PostStatus;
import com.app.snapy.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddPostActivity extends BaseActivity {

    private final int READ_STORAGE_PERMISSION_REQUEST = 123;
    private final int PICK_IMAGE_REQUEST = 1;

    ActivityResultLauncher<Intent> getImageUriFromGallery;
    Uri imagefilePath;
    Bitmap bitmap;
    int ratePostValue = 0;
    ActivityAddPostBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        getImageUriFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getData() != null) {
                    imagefilePath = result.getData().getData();
                    Picasso.get().load(imagefilePath).into(binding.ivPostImage);
                }
            }
        });
        binding.ivPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasGalleryPermissions(AddPostActivity.this)) {
                    getImageFromGallery(getImageUriFromGallery);
                }
            }
        });//end
        binding.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratePostValue = (int) binding.ratingBar.getRating();

            }
        });
        binding.ivSavePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishPost();
            }
        });//end
        binding.ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }//end of setListeners

    private void publishPost(){
        final ProgressDialog dialog=new ProgressDialog(this);
        dialog.setTitle("Post is uploading...");
        dialog.show();
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference usersNode=db.getReference("users");
        DatabaseReference postsNode=db.getReference("posts");
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference =firebaseStorage.getReference("Image_"+ Utils.random());
        storageReference.putFile(imagefilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        dialog.dismiss();
                        Post post =new Post();
                        post.setPostCaption(binding.etPostCaption.getText().toString());
                        post.setPostRating(String.valueOf(ratePostValue));
                        post.setPostUrl(uri.toString());
                        post.setStatus(PostStatus.WAITING+"");
                        post.setPublishDateTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).toString());
                        post.setUserID(FirebaseUtils.getUserUuid());
                        postsNode.push().setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                showToast("Post successfully uploaded");
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showToast("Post not uploaded..");
                            }
                        });
                        //rootNode.child("users/posts").push().setValue(post);
                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                float percent=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                dialog.setMessage("Uploaded :"+(int)percent+" %");
            }
        });
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