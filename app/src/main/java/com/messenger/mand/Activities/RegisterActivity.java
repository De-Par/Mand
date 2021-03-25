package com.messenger.mand.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Interactions.LanguageContextWrapper;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.R;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference reference;

    private EditText password, repeatPassword, name, email;
    private Button RegisterButton;
    private Animation butt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        repeatPassword = findViewById(R.id.repeatPasswordEditText);
        name = findViewById(R.id.nameEditText);
        RegisterButton = findViewById(R.id.SignUpButton);

        password.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
            Matcher matcher = pattern.matcher(source);
            if (!matcher.matches()) return "";
            return null;
        }});

        repeatPassword.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
            Matcher matcher = pattern.matcher(source);
            if (!matcher.matches()) return "";
            return null;
        }});

        RegisterButton.setOnClickListener(v -> {
            butt = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_scale_button_pressing);
            RegisterButton.startAnimation(butt);

            UserInteraction.hideKeyboard(RegisterActivity.this);  //  not important for such situation

            String txt_email = email.getText().toString().trim();
            String txt_password = password.getText().toString().trim();
            String txt_password_repeat = repeatPassword.getText().toString().trim();
            String txt_name = name.getText().toString().trim();

            if (txt_email.length() <= 7 || !txt_email.contains("@mail.ru")) {
                UserInteraction.showPopUpSnackBar(getString(R.string.input_email), v, getApplicationContext());

            } else if (txt_password.length() < 7 || txt_password_repeat.length() < 7) {
                UserInteraction.showPopUpSnackBar(getString(R.string.password_min), v, getApplicationContext());

            } else if (!txt_password.equals(txt_password_repeat)) {
                UserInteraction.showPopUpSnackBar(getString(R.string.password_match), v, getApplicationContext());

            } else if (txt_password.length() > 34) {
                UserInteraction.showPopUpSnackBar(getString(R.string.password_max), v, getApplicationContext());

            } else if (txt_name.equals("")) {
                UserInteraction.showPopUpSnackBar(getString(R.string.input_name), v, getApplicationContext());

            } else if (txt_name.length() > 20) {
                UserInteraction.showPopUpSnackBar(getString(R.string.name_max), v, getApplicationContext());

            } else {
                register(txt_email, txt_password, txt_name,
                        UserInteraction.hasInternetConnection(getApplicationContext()), v);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    private void register(final String email, final String password, final String username,
                           final boolean isConnected, final View view) {

        final Dialog progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_bar);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, getString(R.string.auth_suc),
                                Toast.LENGTH_SHORT).show();
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        final String id = firebaseUser.getUid();
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(id);

                        reference.setValue(createUserMap(id, username, firebaseUser)).addOnCompleteListener(task1 -> {
                            Intent intent = new Intent(RegisterActivity.this, NavigationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        });

                    } else if (!isConnected) {
                        UserInteraction.showPopUpSnackBar(getString(R.string.internet_connection), view,
                                getApplicationContext());
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.auth_fail),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private HashMap<String, String> createUserMap(String id, String username, FirebaseUser firebaseUser) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("id", id);
        hashMap.put("name", username);
        hashMap.put("email", firebaseUser.getEmail());
        hashMap.put("avatar", "default");
        hashMap.put("dateCreation", DataInteraction.getTimeNow());
        hashMap.put("dateBirth", getString(R.string.no_info));
        hashMap.put("phone", getString(R.string.no_info));
        hashMap.put("sex", getString(R.string.no_info));
        hashMap.put("aboutMe", getString(R.string.no_info));
        hashMap.put("status", "online");
        hashMap.put("searchName", username.toLowerCase());

        return hashMap;
    }

}

