package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amp_mini_project.Firebase.DatabaseItem;
import com.example.amp_mini_project.Firebase.DatabaseItemAdapter;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseItemAdapter adapter;
    private List<DatabaseItem> itemList;
    private List<DatabaseItem> filteredList;
    private List<String> keyList;
    private DatabaseReference databaseReference;
    private SearchView searchView;
    private TextView appName;

    protected int typeFilter;
    private View loadingOverlay;
    private int itemsToLoad = 0; // Total number of items to load
    private int itemsLoaded = 0; // Number of items fully loaded (image and username)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appName = findViewById(R.id.app_name);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView.clearFocus();

        itemList = new ArrayList<>();
        keyList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new DatabaseItemAdapter(this, filteredList, this::itemLoaded);
        recyclerView.setAdapter(adapter);
        loadingOverlay = findViewById(R.id.loading_overlay);

        databaseReference = FirebaseDatabase.getInstance().getReference("entries");
        fetchItemsFromDatabase();
        setupSearch();

        FloatingActionButton buttonFab = findViewById(R.id.fab_add_item);
        buttonFab.setOnClickListener(v -> {
            Intent intent = new Intent(ListActivity.this, AddItemActivity.class);
            intent.putExtra("itemType", typeFilter);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        setupBottomNavigation();
    }

    private void fetchItemsFromDatabase() {
        MyApp app = (MyApp) getApplication();
        String userId = app.getUserId();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                itemsToLoad = 0;
                itemsLoaded = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DatabaseItem item = dataSnapshot.getValue(DatabaseItem.class);

                    if (item != null) {
                        item.setKey(dataSnapshot.getKey());
                        if ((typeFilter == -1 ? item.getUploaderId().equals(userId) :
                                typeFilter == item.getType() && !item.getUploaderId().equals(userId) && item.getStatus() == 0)) {
                            itemList.add(item);
                            itemsToLoad++;
                        }
                    }
                }

                itemList.sort((item1, item2) -> Long.compare(item2.getUploadTime(), item1.getUploadTime()));
                filteredList.clear();
                filteredList.addAll(itemList);
                adapter.notifyDataSetChanged();

                hideLoadingOverlay(); // Hide overlay immediately after data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingOverlay();
            }
        });
    }


    private void hideLoadingOverlay() {
        loadingOverlay.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    public void itemLoaded() {
        itemsLoaded++;
        if (itemsLoaded >= itemsToLoad) {
            hideLoadingOverlay();
        }
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
        searchView.setOnSearchClickListener(v -> {
            appName.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchView.getLayoutParams();
            params.weight = 1;
            params.width = 0;
            searchView.setLayoutParams(params);
        });

        searchView.setOnCloseListener(() -> {
            appName.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchView.getLayoutParams();
            params.weight = 0;
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            searchView.setLayoutParams(params);
            return false;
        });
    }

    private void filterList(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            for (DatabaseItem item : itemList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase()) ||
                        item.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    protected void setupBottomNavigation()
    {
        LinearLayout lostButton = findViewById(R.id.button_lost);
        LinearLayout foundButton = findViewById(R.id.button_found);
        LinearLayout mineButton = findViewById(R.id.button_mine);
        LinearLayout profileButton = findViewById(R.id.button_profile);
        LinearLayout messagesButton = findViewById(R.id.button_messages);

        lostButton.setOnClickListener(v -> navigateTo(LostListActivity.class));
        foundButton.setOnClickListener(v -> navigateTo(FoundListActivity.class));
        mineButton.setOnClickListener(v -> navigateTo(MineListActivity.class));
        profileButton.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        messagesButton.setOnClickListener(v -> navigateTo(MessagesActivity.class));
    }

    protected void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
