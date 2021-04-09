package com.messenger.mand.core;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();
        try {
            if (!FirebaseApp.getApps(this).isEmpty())
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
