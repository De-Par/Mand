package com.messenger.mand.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.messenger.mand.Activities.ChatActivity;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;
import java.util.ArrayList;

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

        if (user.getAvatar().equals("default")){
            userViewHolder.avatar.setImageResource(R.drawable.user_image);
        } else {
            Glide.with(context).load(user.getAvatar()).into(userViewHolder.avatar);
        }

        String status;
        if (user.getStatus().equals("online")) {
            status = context.getString(R.string.online);
            userViewHolder.statusOnline.setTextColor(context.getResources().getColor(R.color.green, context.getTheme()));
        } else {
            status = context.getString(R.string.lastSeen) + " " + user.getStatus();
            userViewHolder.statusOnline.setTextColor(context.getResources().getColor(R.color.black_overlay, context.getTheme()));
        }
        userViewHolder.statusOnline.setText(status);

        userViewHolder.itemView.setOnClickListener(v -> {
            final Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("user_id", user.getId());
            intent.putExtra("user_name", user.getName());
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return users.size();
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
