package com.example.LuxxHavenApp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UnitAdapter adapter;
    private List<JSONObject> unitList = new ArrayList<>();
    private List<String> activeUnitNames = new ArrayList<>();
    private List<JSONObject> homeBookedRanges = new ArrayList<>();
    private TextView tvSelectUnit, tvSelectDate;
    private int currentSelectedUnitId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSelectUnit = findViewById(R.id.tv_select_unit);
        tvSelectDate = findViewById(R.id.tv_select_date);
        recyclerView = findViewById(R.id.rv_units);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        adapter = new UnitAdapter(unitList);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            else if (itemId == R.id.nav_wishlist) {
                startActivity(new Intent(this, WishlistActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_help) {
                startActivity(new Intent(this, HelpActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        tvSelectUnit.setOnClickListener(v -> showUnitSelector());
        tvSelectDate.setOnClickListener(v -> showDatePicker());

        fetchActiveUnitNames();
        fetchActiveUnits("All Units");
    }

    private void fetchActiveUnitNames() {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/get_unit_names.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        activeUnitNames.clear();
                        for (int i = 0; i < response.length(); i++) {
                            activeUnitNames.add(response.getString(i));
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> Log.e("API", "Error fetching names"));
        Volley.newRequestQueue(this).add(request);
    }

    private void showUnitSelector() {
        if (activeUnitNames.isEmpty()) {
            fetchActiveUnitNames();
            return;
        }
        String[] units = activeUnitNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Select a Unit")
                .setItems(units, (dialog, which) -> {
                    String selected = units[which];
                    tvSelectUnit.setText(selected);
                    updateUnitIdAndFetchDates(selected);
                    fetchActiveUnits(selected);
                }).show();
    }

    private void updateUnitIdAndFetchDates(String unitName) {
        for (JSONObject unit : unitList) {
            try {
                if (unit.getString("name").equals(unitName)) {
                    currentSelectedUnitId = unit.getInt("id");
                    fetchBookedDatesForHome(currentSelectedUnitId);
                    break;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void fetchBookedDatesForHome(int unitId) {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/get_booked_dates.php?unit_id=" + unitId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    homeBookedRanges.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try { homeBookedRanges.add(response.getJSONObject(i)); } catch (Exception e) {}
                    }
                }, error -> Log.e("API", "Error fetching booked dates"));
        Volley.newRequestQueue(this).add(request);
    }

    private void showDatePicker() {
        if (tvSelectUnit.getText().toString().equals("Select Unit")) {
            Toast.makeText(this, "Please select a unit first", Toast.LENGTH_SHORT).show();
            return;
        }

        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dateToCheck = String.format(Locale.US, "%d-%02d-%02d", year, (month + 1), dayOfMonth);
                    if (isHomeDateBooked(dateToCheck)) {
                        Toast.makeText(this, "Unit is already booked on this date!", Toast.LENGTH_SHORT).show();
                        tvSelectDate.setText("Check-in Date");
                    } else {
                        tvSelectDate.setText(dateToCheck);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean isHomeDateBooked(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date target = sdf.parse(dateString);
            for (JSONObject range : homeBookedRanges) {
                Date start = sdf.parse(range.getString("check_in_date"));
                Date end = sdf.parse(range.getString("check_out_date"));
                if (target != null && (target.equals(start) || target.equals(end) || (target.after(start) && target.before(end)))) {
                    return true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private void fetchActiveUnits(String filter) {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/get_home_data.php?unit_type=" + filter;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        unitList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            unitList.add(response.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) { e.printStackTrace(); }
                }, error -> Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(request);
    }
}