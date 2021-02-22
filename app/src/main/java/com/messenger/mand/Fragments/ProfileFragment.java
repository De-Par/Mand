package com.messenger.mand.Fragments;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
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
import com.messenger.mand.Activities.EditProfileActivity;
import com.messenger.mand.Activities.ZoomViewActivity;
import com.messenger.mand.Interactions.UserInteraction;
import static com.messenger.mand.Values.Sensor.*;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView imageProfile;
    private TextView userName;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    private StorageReference storageReference;
    private boolean isIcon;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    String[] galleryPermissions;
    String[] cameraPermissions;
    Uri imageUri = null;

    private TextView email;
    private TextView id;
    private TextView dateCreation;
    private LottieAnimationView animationDone;

    public ProfileFragment() {
        //
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("");

        imageProfile = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.username);
        email = view.findViewById(R.id.txtMail);
        id = view.findViewById(R.id.txtId);
        dateCreation = view.findViewById(R.id.txtDate);
        animationDone = view.findViewById(R.id.lottieAnimationDone);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").
                child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference().child("Avatar_Images");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        galleryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        imageProfile.setOnClickListener(v -> alertDialogChoicer());

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
                    try {
                        Glide.with(requireContext().getApplicationContext()).
                                load(user.getAvatar()).into(imageProfile);
                        isIcon = false;
                    } catch (Exception e) {
                        imageProfile.setImageResource(R.drawable.profile_image_default);
                        isIcon = true;
                    }
                } else {
                    imageProfile.setImageResource(R.drawable.profile_image_default);
                    isIcon = true;
                }
                email.setText(user.getEmail());
                id.setText(user.getId());
                dateCreation.setText(user.getDateCreation());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_STORAGE_CODE) {
                assert data != null;
                imageUri = data.getData();
                uploadImageToProfile(imageUri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadImageToProfile(imageUri);
            }
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), R.string.upload, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean camAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        UserInteraction.showPopUpSnackBar(getString(R.string.error_smth), getView(), getContext());
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        UserInteraction.showPopUpSnackBar(getString(R.string.error_smth), getView(), getContext());
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_icon_edit) {
            gotoEditProfile();
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoEditProfile() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void uploadImageToProfile(Uri uri) {
        final Dialog progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();

        String[] pathFileName = uri.getPath().split("/");
        final StorageReference fileReference = storageReference.child(pathFileName[pathFileName.length - 1]);
        fileReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful());
            Uri downloadUri = uriTask.getResult();

            if (uriTask.isSuccessful() && downloadUri != null) {
                String mUri = Objects.requireNonNull(downloadUri).toString();
                databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(firebaseUser.getUid());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("avatar", mUri);
                databaseReference.updateChildren(hashMap);
                successfulAnimation();

            } else {
                Toast.makeText(getContext(), R.string.error_smth, Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void alertDialogChoicer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        String[] actions = {getString(R.string.open_photo), getString(R.string.change_photo),
                getString(R.string.take_photo), getString(R.string.delete_photo)};
        builder.setItems(actions, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    zoomAvatar();
                    break;
                case 1:
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                    break;
                case 2:
//                    if (!checkCameraPermission()) {
//                        requestCameraPermission();
//                    } else {
//                        pickFromCamera();
//                    }
                    pickFromCamera();
                    break;
                case 3:
                    deleteAvatar();
                    break;
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (camIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(camIntent, IMAGE_PICK_CAMERA_CODE);
        }
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_STORAGE_CODE);
    }

    private void zoomAvatar() {
        if (!isIcon) {
            byte[] arr = DataInteraction.convertDrawableToByteArr(((BitmapDrawable) imageProfile.
                    getDrawable()).getBitmap(), ORIGINAL);
            startActivity(new Intent(getActivity(), ZoomViewActivity.class).putExtra("photo", arr));
        } else {
            startActivity(new Intent(getActivity(), ZoomViewActivity.class).putExtra("flag", true));
        }
    }

    private void deleteAvatar() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("avatar", "default");
        reference.updateChildren(hashMap);

//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, profileUri);
//        startActivityForResult(intent, 2222);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        requestPermissions(galleryPermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean flag1 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        boolean flag2 = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        return flag1 && flag2;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void successfulAnimation() {
        animationDone.setVisibility(View.VISIBLE);
        animationDone.setSpeed(1.2f);
        animationDone.bringToFront();
        animationDone.playAnimation();
    }
}
