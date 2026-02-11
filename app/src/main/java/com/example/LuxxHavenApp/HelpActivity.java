package com.example.LuxxHavenApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    private EditText etBookingId, etEmail;
    private Button btnFindBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        etBookingId = findViewById(R.id.et_booking_id);
        etEmail = findViewById(R.id.et_email);
        btnFindBooking = findViewById(R.id.btn_find_booking);

        // Navigation Setup (Fixed Consistency)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_help);
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
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_help) {
                return true;
            }
            return false;
        });

        btnFindBooking.setOnClickListener(v -> {
            String ref = etBookingId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (!ref.isEmpty() && !email.isEmpty()) {
                findBooking(ref, email);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findBooking(final String bookingRef, final String email) {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/find_booking.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {

                    Log.d("API_DEBUG", "Server Response: " + response);

                    try {
                        JSONObject jsonResponse = new JSONObject(response);


                        if (!jsonResponse.has("status")) {
                            Toast.makeText(this, "Invalid API Response: No status", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            JSONObject data = jsonResponse.getJSONObject("data");


                            int unitId;
                            if (data.has("unit_id")) {
                                unitId = data.optInt("unit_id", -1);
                                if (unitId == -1) {

                                    String idStr = data.optString("unit_id");
                                    try {
                                        unitId = Integer.parseInt(idStr);
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(this, "Error: unit_id is not a number", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Error: unit_id missing in response", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String checkIn = data.optString("check_in_date", "N/A");
                            String checkOut = data.optString("check_out_date", "N/A");

                            Intent intent = new Intent(HelpActivity.this, RescheduleDetailsActivity.class);
                            intent.putExtra("BOOKING_REF", bookingRef);
                            intent.putExtra("UNIT_ID", unitId);
                            intent.putExtra("CHECK_IN", checkIn);
                            intent.putExtra("CHECK_OUT", checkOut);

                            startActivity(intent);

                        } else {
                            String message = jsonResponse.optString("message", "Unknown error");
                            Toast.makeText(this, "❌ " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("API_ERROR", "Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Data Error: API response invalid.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Connection Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("booking_ref", bookingRef);
                params.put("email", email);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}