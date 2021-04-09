package com.messenger.mand.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.messenger.mand.interactions.DataInteraction;
import com.messenger.mand.interactions.DatabaseInteraction;
import com.messenger.mand.R;

import java.util.Objects;

import static com.messenger.mand.values.Navigation.LINK_PROFILE;

public class EditProfileActivity extends AppCompatActivity  {

    ListView listView;

    public EditProfileActivity() {}

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.profile));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] titles = { getString(R.string.nickname),  };
        ItemAdapter itemAdapter = new ItemAdapter(this, titles);

        listView = findViewById(R.id.editListView);
        listView.setAdapter(itemAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {

        });
    }

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getBaseContext(), DashboardActivity.class);
            intent.putExtra("position", LINK_PROFILE);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onResume() {
        super.onResume();
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onPause() {
        super.onPause();
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
    }

    private void showEditDialog() {
        String[] options = {"Edit name", "Edit phone", "About you"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Title");
        builder.setItems(options, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    //
                    break;
                case 1:
                    //
                    break;
                case 2:
                    //
                    break;

            }
        });
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null){
            dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
        }

        dialog.show();
    }

    private class ItemAdapter extends ArrayAdapter<String> {
        private final String[] titles;

        public ItemAdapter(Context context, String[] titles) {
            super(context, R.layout.edit_profile_item, R.id.item, titles);
            this.titles = titles;
        }

        @NonNull
        @Override
        public final View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.edit_profile_item, parent, false);
            TextView text = row.findViewById(R.id.item);
            text.setText(titles[position]);
            return row;
        }
    }
}
