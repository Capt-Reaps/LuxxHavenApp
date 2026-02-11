package com.example.LuxxHavenApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UnitAdapter adapter;
    private List<JSONObject> unitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        recyclerView = findViewById(R.id.rv_booking_units);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UnitAdapter(unitList);
        recyclerView.setAdapter(adapter);

        setupBottomNavigation();

        fetchActiveUnits();
    }

    private void fetchActiveUnits() {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/get_home_data.php?unit_type=All%20Units";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        unitList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            unitList.add(response.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("API", "Parsing error", e);
                    }
                }, error -> Toast.makeText(this, "Unable to load units", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_bookings); // Highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_wishlist) {
                startActivity(new Intent(this, WishlistActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_help) {
                startActivity(new Intent(this, HelpActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else return itemId == R.id.nav_bookings;
        });
    }
}