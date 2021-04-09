package com.messenger.mand.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
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
import com.iceteck.silicompressorr.FileUtils;
import com.messenger.mand.R;
import com.messenger.mand.adapters.MessageAdapter;
import com.messenger.mand.entities.Message;
import com.messenger.mand.entities.User;
import com.messenger.mand.interactions.DataInteraction;
import com.messenger.mand.interactions.DatabaseInteraction;
import com.messenger.mand.interactions.UserInteraction;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.messenger.mand.values.Sensor.IMAGE_PICK_STORAGE_CODE;
import static com.messenger.mand.values.Sensor.STORAGE_REQUEST_CODE;

public class ChatActivity extends AppCompatActivity {
    private final String TAG = ChatActivity.class.toString();

    private MessageAdapter adapter;
    private final ArrayList<Message> messages = new ArrayList<>();
    private RecyclerView mesRecyclerView;
    private CircleImageView avatar;
    ImageButton sendImgBtn;
    ImageButton sendMesBtn;
    private EditText mesEditText;
    private TextView uName;
    private TextView uStatus;

    private FirebaseUser firebaseUser;
    FirebaseStorage storage;
    private DatabaseReference chatRef;
    private StorageReference imgStorageRef;
    DatabaseReference userRef;
    private ValueEventListener statusListener;
    private String recipientUserId;
    String recipientUserName;
    String[] galleryPermissions;
    private String url = null;
    AnstronCoreHelper coreHelper;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, DashboardActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recipientUserId = extras.getString("user_id");
            recipientUserName = extras.getString("user_name");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        galleryPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        coreHelper = new AnstronCoreHelper(getApplicationContext());

        String oneToOneId = firebaseUser.getUid().hashCode() > recipientUserId.hashCode() ?
                recipientUserId + firebaseUser.getUid() : firebaseUser.getUid() + recipientUserId;

        chatRef = database.getReference().child("chats").child(oneToOneId);
        chatRef.keepSynced(true);
        userRef = database.getReference().child("users").child(recipientUserId);
        userRef.keepSynced(true);
        imgStorageRef = storage.getReference().child("Chat_images");

        mesRecyclerView = findViewById(R.id.messListRecyclerView);
        sendImgBtn = findViewById(R.id.sendPhotoButton);
        sendMesBtn = findViewById(R.id.sendMessageButton);
        mesEditText = findViewById(R.id.messageEditText);
        uName = findViewById(R.id.username);
        uStatus = findViewById(R.id.statusUser);
        avatar = findViewById(R.id.profile_image);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mesRecyclerView.setHasFixedSize(true);
        mesRecyclerView.setLayoutManager(linearLayoutManager);

        sendMesBtn.setOnClickListener(v -> {
            if (!UserInteraction.getTrimLen(mesEditText).equals("")) {
                sendMessageToDB(firebaseUser.getUid(), recipientUserId, UserInteraction.getTrimLen(mesEditText),
                        "", DataInteraction.getTimeNow());
            } else {
                Toast.makeText(ChatActivity.this, getString(R.string.null_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        avatar.setOnClickListener(v -> profilePreview());

        sendImgBtn.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else {
                pickFromGallery();
            }
        });

        mesEditText.setFilters(new InputFilter[]
                {new InputFilter.LengthFilter(700)});

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String status;
                assert user != null;
                uName.setText(user.getName());

                if (!user.getStatus().equals("online")) {
                    String[] date = user.getStatus().split(", ");
                    status = getString(R.string.lastSeen) + " " + date[1] + " " + getString(R.string.at) + " " + date[2];
                } else {
                    status = getString(R.string.online);
                }
                uStatus.setText(status);

                if (!user.getAvatar().equals("default") && !user.getAvatar().equals("")) {
                    try {
                        url = user.getAvatar();
                        Glide.with(getApplicationContext()).load(url).into(avatar);
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        avatar.setImageResource(R.drawable.profile_image_default);
                    }
                } else {
                    avatar.setImageResource(R.drawable.profile_image_default);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        readMessages();
        seenMessage();
    }

    @Override
    public final void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_STORAGE_CODE) {
                assert data != null;
                Uri imageUri = data.getData();
                uploadImageToChat(imageUri);
            }
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    UserInteraction.showPopUpSnackBar(getString(R.string.error_smth), getCurrentFocus(), getApplicationContext());
                }
            }
            break;
        }
    }

    @Override
    public final void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, DashboardActivity.class);
        startActivity(intent);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        DatabaseInteraction.pushUserStatus("online");

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            Log.e(TAG, keyStore.size()+"");

            if (keyStore.containsAlias("u5XAoZu1doT1x71T8mwsdz62my127FpBSBorKqM7grwqesF6qqWK8K62")) {
                Log.e(TAG, "YES");
            } else {
                Log.e(TAG, "NO");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected final void onResume() {
        super.onResume();
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onPause() {
        super.onPause();
        chatRef.child("data/messages").removeEventListener(statusListener);
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
    }

    private void readMessages() {
        messages.clear();
        chatRef.child("data/messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!messages.isEmpty()) messages.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    assert message != null;
                    if ((message.getSender().equals(firebaseUser.getUid()) && message.getRecipient().
                            equals(recipientUserId)) || (message.getSender().equals(recipientUserId) &&
                            message.getRecipient().equals(firebaseUser.getUid()))) {
                        if (message.getText().equals("")) {
                            message.setText(null);
                        } else {
                            message.setImageUrl(null);
                        }
                        messages.add(message);
                    }
                }
                adapter = new MessageAdapter(ChatActivity.this, messages);
                adapter.notifyDataSetChanged();

                mesRecyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void seenMessage() {
        statusListener = chatRef.child("data/messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    assert message != null;
                    if (message.getRecipient().equals(firebaseUser.getUid()) && message.getSender().
                            equals(recipientUserId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", "Seen");
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @SuppressWarnings("deprecation")
    private void uploadImageToChat(Uri uri) {
        final Dialog progressDialog = new Dialog(ChatActivity.this);
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();

        if (uri != null) {
            final StorageReference fileReference = imgStorageRef.child(coreHelper.
                    getFileNameFromUri(uri));
            fileReference.putBytes(getBytesFromCompressedImage(uri)).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                while (!uriTask.isSuccessful()) {Log.v(TAG, "Waiting... While loading data");}

                if (uriTask.isSuccessful()) {
                    Uri downloadUri = uriTask.getResult();
                    assert downloadUri != null;
                    sendMessageToDB(firebaseUser.getUid(), recipientUserId, "",
                            downloadUri.toString(), DataInteraction.getTimeNow());
                } else {
                    Toast.makeText(ChatActivity.this, R.string.error_smth, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this, R.string.successful, Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        } else {
            progressDialog.dismiss();
        }
    }

    private byte[] getBytesFromCompressedImage(Uri uri) {
        byte[] bytes = null;
        File file = new File(FileUtils.getPath(getApplicationContext(), uri));

        try {
            Bitmap bitmap = new Compressor(getApplicationContext()).
                    setMaxHeight(200).
                    setMaxWidth(200).
                    setQuality(100).
                    compressToBitmap(file);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            bytes = byteArrayOutputStream.toByteArray();
            bitmap.recycle();
            byteArrayOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void sendMessageToDB(final String sender, final String receiver, final String message,
                                 final String URI, final String time) {

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", sender);
        messageMap.put("recipient", receiver);
        messageMap.put("text", message);
        messageMap.put("time", time);
        messageMap.put("imageUrl", URI);
        messageMap.put("isSeen", "Delivered");

        chatRef.child("data/messages").push().setValue(messageMap);
        mesEditText.setText("");

        notifyKey();
    }

    private void notifyKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            PrivateKey privateKey;
            PublicKey publicKey;

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(generateOneToOneId(firebaseUser.getUid(), recipientUserId), null);
            privateKey = privateKeyEntry.getPrivateKey();
            publicKey = privateKeyEntry.getCertificate().getPublicKey();


            Log.e(TAG, "private key = " + Arrays.toString(privateKey.getEncoded()));
            Log.e(TAG, "public key = " + Arrays.toString(publicKey.getEncoded()));


//            // Encrypt the text
//            String plainText = "This text is supposed to be a secret!";
//            String dataDirectory = context.getApplicationInfo().dataDir;
//            String filesDirectory = context.getFilesDir().getAbsolutePath();
//            String encryptedDataFilePath = filesDirectory + File.separator + "keys";
//
//            Log.e(TAG, "plainText = " + plainText);
//            Log.e(TAG, "dataDirectory = " + dataDirectory);
//            Log.e(TAG, "filesDirectory = " + filesDirectory);
//            Log.e(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

//            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//
//            Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            outCipher.init(Cipher.DECRYPT_MODE, privateKey);
//
//            CipherOutputStream cipherOutputStream =
//                    new CipherOutputStream(
//                            new FileOutputStream(encryptedDataFilePath), inCipher);
//            cipherOutputStream.write(plainText.getBytes(StandardCharsets.UTF_8));
//            cipherOutputStream.close();
//
//            CipherInputStream cipherInputStream =
//                    new CipherInputStream(new FileInputStream(encryptedDataFilePath),
//                            outCipher);
//            byte [] roundTrippedBytes = new byte[1000];
//
//            int index = 0;
//            int nextByte;
//            while ((nextByte = cipherInputStream.read()) != -1) {
//                roundTrippedBytes[index] = (byte)nextByte;
//                index++;
//            }
//            String roundTrippedString = new String(roundTrippedBytes, 0, index, StandardCharsets.UTF_8);
//            Log.e(TAG, "round tripped string = " + roundTrippedString);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_STORAGE_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        requestPermissions(galleryPermissions, STORAGE_REQUEST_CODE);
    }

    private void profilePreview() {
        if (url != null) {
            try {
                getApplicationContext().startActivity(new Intent(ChatActivity.this,
                        ZoomImageActivity.class).putExtra("photo", url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            startActivity(new Intent(ChatActivity.this, ZoomImageActivity.class).
                    putExtra("icon", R.drawable.profile_image_default));
        }
    }

    @NotNull
    private String generateOneToOneId(@NotNull String first, @NotNull String second) {
        if (first.hashCode() > second.hashCode()) {
            return second + first;
        } else {
            return first + second;
        }
    }
}