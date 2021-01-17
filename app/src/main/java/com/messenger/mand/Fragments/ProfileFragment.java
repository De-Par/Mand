package com.messenger.mand.Fragments;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
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
import com.messenger.mand.Activities.ZoomViewActivity;
import com.messenger.mand.Objects.Constants;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static final int IMAGE_REQUEST = 3316;
    private Uri profileUri;
    private boolean isIcon;
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
        animationDone = view.findViewById(R.id.lottieAnimationDone);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        imageProfile.setOnClickListener(v -> alertDialogChoicer());

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

                    profileUri = Uri.parse(user.getAvatar());
                    isIcon = false;

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_icon_edit) {
            Log.println(Log.INFO, Constants.TAG, "HEllo");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            profileUri = data.getData();
            uploadImageToProfile();

            if(uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), R.string.upload, Toast.LENGTH_SHORT).show();
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
        animationDone.setSpeed(1.2f);
        animationDone.bringToFront();
        animationDone.playAnimation();
    }

    private void alertDialogChoicer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] actions = {getString(R.string.open_photo), getString(R.string.change_photo),
                getString(R.string.delete_photo)};
        builder.setItems(actions, (dialog, which) -> {
            switch (which) {
                case 0: zoomImage(); break;
                case 1: selectImageProfile(); break;
                case 2: deleteImageProfile(); break;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void zoomImage() {
        if (!isIcon) {
            byte[] arr = DataInteraction.convertDrawableToByteArr(((BitmapDrawable) imageProfile.
                    getDrawable()).getBitmap(), Constants.ORIGINAL);
            startActivity(new Intent(getActivity(), ZoomViewActivity.class).putExtra("photo", arr));
        } else {
            startActivity(new Intent(getActivity(), ZoomViewActivity.class).putExtra("flag", true));
        }
    }

    private void deleteImageProfile() {
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

        public static String compressImage(Context context, String imagePath) {

            final float maxHeight = 1024.0f;
            final float maxWidth = 1024.0f;

            Bitmap scaledBitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;
            float imgRatio = (float) actualWidth / (float) actualHeight;
            float maxRatio = maxWidth / maxHeight;

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
                bmp = BitmapFactory.decodeFile(imagePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            } try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            assert scaledBitmap != null;

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
                    middleY - bmp.getHeight() / 2,
                    new Paint(Paint.FILTER_BITMAP_FLAG));
            bmp.recycle();

            ExifInterface exif;

            try {
                exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                } scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                        matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            String filepath = getFilename(context);

            try {
                out = new FileOutputStream(filepath);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return filepath;
        }

        public static int calculateInSampleSize(BitmapFactory.
                    Options options, int reqWidth, int reqHeight) {

            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = Math.min(heightRatio, widthRatio);
            }

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++; }
            return inSampleSize;
        }

        public static String getFilename(Context context) {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getApplicationContext().getPackageName() + "/Files/Compressed");
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }
            String mImageName = "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
            return (mediaStorageDir.getAbsolutePath() + "/" + mImageName);
        }

    }
