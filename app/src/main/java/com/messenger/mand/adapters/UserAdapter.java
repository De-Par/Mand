package com.messenger.mand.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.messenger.mand.activities.ChatActivity;
import com.messenger.mand.entities.Chat;
import com.messenger.mand.entities.User;
import com.messenger.mand.R;

import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import javax.security.auth.x500.X500Principal;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final String TAG = UserAdapter.class.toString();

    private final ArrayList<User> users;
    private final Context context;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public final UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, viewGroup,false);
        return new UserViewHolder(view);
    }

    @Override
    public final void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, int position) {
        final User user = users.get(position);
        userViewHolder.user_name.setText(user.getName());

        if (!user.getAvatar().equals("default") && !user.getAvatar().equals("")){
            try {
                Glide.with(context).load(user.getAvatar()).into(userViewHolder.avatar);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
                userViewHolder.avatar.setImageResource(R.drawable.profile_image_default);
            }
        } else {
            userViewHolder.avatar.setImageResource(R.drawable.profile_image_default);
        }

        String status;
        if (user.getStatus().equals("online")) {
            status = context.getString(R.string.online);
            userViewHolder.statusOnline.setTextColor(context.getResources().
                    getColor(R.color.greenBright, context.getTheme()));
        } else {
            String[] date = user.getStatus().split(", ");
            status = context.getString(R.string.lastSeen) + " " + date[1] + " " +
                    context.getString(R.string.at) + " " + date[2];
        }
        userViewHolder.statusOnline.setText(status);
        userViewHolder.itemView.setOnClickListener(v -> isChatCreated(v, user));
    }

    @Override
    public final int getItemCount() {
        return users.size();
    }

    private void isChatCreated(View v, User recipient) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chats");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int pos = -1;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.child("info").getValue(Chat.class);
                    assert chat != null;
                    assert currentUser != null;
                    if ((chat.getInitiator().equals(currentUser.getUid()) && chat.getRecipient().
                            equals(recipient.getId())) || (chat.getInitiator().equals(recipient.
                            getId()) && chat.getRecipient().equals(currentUser.getUid()))) {
                        pos = 1;
                    }
                }
                if (pos == -1) {
                    createChatDialog(v, recipient);
                } else {
                    gotoChat(recipient);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createChatDialog(@NotNull View v, User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.AlertDialogTheme);
        View view = LayoutInflater.from(v.getContext()).inflate(R.layout.chat_dialog,
                v.findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.dialogChatTitle)).setText(context.
                getString(R.string.start_chatting_title));
        ((TextView) view.findViewById(R.id.textMessage)).setText(context.
                getString(R.string.start_chatting_q));
        view.findViewById(R.id.imgCreateChat).setBackground(ContextCompat.
                getDrawable(context, R.drawable.img_create_chat));
        ((Button) view.findViewById(R.id.buttonCreateChat)).setText(context.
                getString(R.string.start));

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.closeDialog).setOnClickListener(vi -> alertDialog.dismiss());
        view.findViewById(R.id.buttonCreateChat).setOnClickListener(vi -> {
            alertDialog.dismiss();
            try {
                sendListOfChatsToDataBase(user);
                gotoChat(user);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void sendListOfChatsToDataBase(User recipient) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                    child("chats").child(generateOneToOneId(firebaseUser.getUid(), recipient.getId())).child("info");

            String publicKey = Base64.encodeBase64String(Objects.requireNonNull(initPublicKey(
                    generateOneToOneId(recipient.getId(), firebaseUser.getUid()))));

            HashMap<String, Object> chatMap = new HashMap<>();
            chatMap.put("initiator", firebaseUser.getUid());
            chatMap.put("recipient", recipient.getId());
            chatMap.put("iniPublicKey", publicKey);
            chatMap.put("recPublicKey", "");

            databaseReference.setValue(chatMap);
        }
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private byte[] initPublicKey(String alias) {
        String uId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            PrivateKey privateKey;
            PublicKey publicKey;

            if (!keyStore.containsAlias(alias)) {
                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 100);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .setKeySize(2048)
                        .setSubject(new X500Principal("CN=test"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
            } else {
                // Retrieve the keys
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
                privateKey = privateKeyEntry.getPrivateKey();
                publicKey = privateKeyEntry.getCertificate().getPublicKey();
            }

            Log.e(TAG, "private key = " + Arrays.toString(privateKey.getEncoded()));
            Log.e(TAG, "public key = " + Arrays.toString(publicKey.getEncoded()));

            return publicKey.getEncoded();

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
        return null;
    }



    @NotNull
    private String generateOneToOneId(@NotNull String first, @NotNull String second) {
        if (first.hashCode() > second.hashCode()) {
            return second + first;
        } else {
            return first + second;
        }
    }

    private void gotoChat(@NotNull User recipient) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("user_id", recipient.getId());
        intent.putExtra("user_name", recipient.getName());
        intent.putExtra("user_public_key", "");
        context.startActivity(intent);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatar;
        private final TextView user_name;
        private final TextView statusOnline;

        UserViewHolder(@NonNull View view) {
            super(view);
            avatar = view.findViewById(R.id.profile_image);
            user_name = view.findViewById(R.id.userName);
            statusOnline = view.findViewById(R.id.statusOnline);
        }
    }
}
