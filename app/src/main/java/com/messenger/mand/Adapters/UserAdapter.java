package com.messenger.mand.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import com.messenger.mand.Activities.ChatActivity;
import com.messenger.mand.Objects.Chat;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javax.crypto.SecretKey;

import static com.messenger.mand.Interactions.EncryptDecryptString.*;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final ArrayList<User> users;
    private final Context context;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, viewGroup,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, int position) {
        final User user = users.get(position);
        userViewHolder.user_name.setText(user.getName());

        if (!user.getAvatar().equals("default") && !user.getAvatar().equals("")){
            try {
                Glide.with(context).load(user.getAvatar()).into(userViewHolder.avatar);
            } catch (Exception e) {
                Log.e("AVATAR", e.getLocalizedMessage());
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
    public int getItemCount() {
        return users.size();
    }

    private void isChatCreated(View v, User recipient) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("ChatsList");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int pos = -1;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Chat chat = ds.getValue(Chat.class);
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

    private void createChatDialog(View v, User user) {
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
            gotoChat(user);
            alertDialog.dismiss();
            sendListOfChatsToDataBase(user);
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void sendListOfChatsToDataBase(User recipient) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SecretKey key = generateKey("AES");

        assert firebaseUser != null;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                child("ChatsList").child(firebaseUser.getUid() + recipient.getId());

        HashMap<String, Object> chatsMap = new HashMap<>();
        chatsMap.put("initiator", firebaseUser.getUid());
        chatsMap.put("recipient", recipient.getId());
        chatsMap.put("key", Arrays.toString(Objects.requireNonNull(key).getEncoded()));

        databaseReference.updateChildren(chatsMap);
    }

    private void gotoChat(User recipient) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("user_id", recipient.getId());
        intent.putExtra("user_name", recipient.getName());
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
