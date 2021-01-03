package com.messenger.mand.Fragments;

import android.animation.Animator;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.ImageWriter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView imageProfile;
    private TextView userName;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 3316;
    private Uri profileUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    private TextView email;
    private TextView id;
    private TextView dateCreation;
    private LottieAnimationView animationDone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageProfile = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.username);
        email = view.findViewById(R.id.txtMail);
        id = view.findViewById(R.id.txtId);
        dateCreation = view.findViewById(R.id.txtDate);
        animationDone = view.findViewById(R.id.lottieAnimation);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference().child("Avatar_Images");

        animationDone.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animationDone.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                animationDone.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                userName.setText(user.getName());
                if (!user.getAvatar().equals("default") && isAdded()) {
                    Glide.with(requireContext().getApplicationContext()).
                            load(user.getAvatar()).into(imageProfile);
                } else {
                    imageProfile.setImageResource(R.drawable.user_image);
                }
                email.setText(user.getEmail());
                id.setText(user.getId());
                dateCreation.setText(user.getDateCreation().substring(0, 10));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        imageProfile.setOnLongClickListener(v -> {
            selectImageProfile();
            return true;
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            profileUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), R.string.upload, Toast.LENGTH_SHORT).show();
            } else {
                uploadImageToProfile();
            }
        }
    }

    private void selectImageProfile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImageToProfile() {
        final Dialog progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();

        if (profileUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(profileUri));
            uploadTask = fileReference.putFile(profileUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return fileReference.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    String mUri = downloadUri.toString();

                    databaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(firebaseUser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("avatar", mUri);
                    databaseReference.updateChildren(hashMap);

                } else {
                    Toast.makeText(getContext(), R.string.error_smth, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                successfulAnimation();

            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                successfulAnimation();
            });

        } else {
            Toast.makeText(getContext(), R.string.no_image_select, Toast.LENGTH_SHORT).show();
        }
    }

    private void successfulAnimation() {
        animationDone.setVisibility(View.VISIBLE);
        animationDone.setSpeed(0.8f);
        animationDone.playAnimation();
    }

}
