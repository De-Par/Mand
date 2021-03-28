package com.messenger.mand.Interactions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class DatabaseInteraction {

    public static void pushUserStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().
                        getCurrentUser()).getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }
}
