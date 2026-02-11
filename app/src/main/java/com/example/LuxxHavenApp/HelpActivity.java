package com.example.LuxxHavenApp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    private EditText etBookingId, etEmail;
    private Button btnFindBooking;

    private LinearLayout llMenuOptions, llRescheduleForm, llFaqContent, llFaqList;
    private CardView cardOptReschedule, cardOptFaq;
    private TextView btnBackResched, btnBackFaq;
    private ProgressBar pbFaqLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        etBookingId = findViewById(R.id.et_booking_id);
        etEmail = findViewById(R.id.et_email);
        btnFindBooking = findViewById(R.id.btn_find_booking);

        llMenuOptions = findViewById(R.id.ll_menu_options);
        llRescheduleForm = findViewById(R.id.ll_reschedule_form);
        llFaqContent = findViewById(R.id.ll_faq_content);
        llFaqList = findViewById(R.id.ll_faq_list);

        cardOptReschedule = findViewById(R.id.card_opt_reschedule);
        cardOptFaq = findViewById(R.id.card_opt_faq);

        btnBackResched = findViewById(R.id.btn_back_to_menu_resched);
        btnBackFaq = findViewById(R.id.btn_back_to_menu_faq);
        pbFaqLoading = findViewById(R.id.pb_faq_loading);

        setupBottomNavigation();

        cardOptReschedule.setOnClickListener(v -> {
            llMenuOptions.setVisibility(View.GONE);
            llFaqContent.setVisibility(View.GONE);
            llRescheduleForm.setVisibility(View.VISIBLE);
        });

        cardOptFaq.setOnClickListener(v -> {
            llMenuOptions.setVisibility(View.GONE);
            llRescheduleForm.setVisibility(View.GONE);
            llFaqContent.setVisibility(View.VISIBLE);

            fetchFaqs();
        });

        View.OnClickListener backToMenuListener = v -> showMenu();
        btnBackResched.setOnClickListener(backToMenuListener);
        btnBackFaq.setOnClickListener(backToMenuListener);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (llMenuOptions.getVisibility() != View.VISIBLE) {
                    showMenu();
                } else {
                    finish();
                }
            }
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

    private void showMenu() {
        llRescheduleForm.setVisibility(View.GONE);
        llFaqContent.setVisibility(View.GONE);
        llMenuOptions.setVisibility(View.VISIBLE);
    }

    private void fetchFaqs() {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/get_faqs.php";

        pbFaqLoading.setVisibility(View.VISIBLE);
        llFaqList.removeAllViews();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    pbFaqLoading.setVisibility(View.GONE);
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONArray data = response.getJSONArray("data");

                            if (data.length() == 0) {
                                Toast.makeText(this, "No FAQs available.", Toast.LENGTH_SHORT).show();
                            }

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject faq = data.getJSONObject(i);
                                String question = faq.getString("question");
                                String answer = faq.getString("answer");

                                addFaqItem(question, answer);
                            }
                        } else {
                            Toast.makeText(this, "Failed to load FAQs.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing FAQs", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    pbFaqLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void addFaqItem(String question, String answer) {
        TextView tvQuestion = new TextView(this);
        tvQuestion.setText(question);
        tvQuestion.setTextSize(16);
        tvQuestion.setTextColor(Color.parseColor("#333333"));
        tvQuestion.setTypeface(null, Typeface.BOLD);
        tvQuestion.setPadding(0, 0, 0, 8);

        TextView tvAnswer = new TextView(this);
        tvAnswer.setText(answer);
        tvAnswer.setTextSize(14);
        tvAnswer.setTextColor(Color.parseColor("#666666"));
        tvAnswer.setLineSpacing(0, 1.2f);
        tvAnswer.setPadding(0, 0, 0, 24);

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dividerParams.setMargins(0, 8, 0, 32);
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        divider.setLayoutParams(dividerParams);

        llFaqList.addView(tvQuestion);
        llFaqList.addView(tvAnswer);
        llFaqList.addView(divider);
    }

    private void findBooking(final String bookingRef, final String email) {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/find_booking.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (!jsonResponse.has("status")) {
                            showDebugAlert("Invalid JSON", "Missing 'status' field.\n\nRaw: " + response);
                            return;
                        }

                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            JSONObject data = jsonResponse.getJSONObject("data");

                            if (!data.has("unit_id")) {
                                showDebugAlert("Missing Data", "API missing 'unit_id'");
                                return;
                            }

                            int unitId = data.optInt("unit_id", -1);
                            if (unitId == -1) {
                                String idStr = data.optString("unit_id");
                                try { unitId = Integer.parseInt(idStr); }
                                catch (Exception e) { return; }
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
                        showDebugAlert("Parsing Error", e.getMessage());
                    }
                },
                error -> Toast.makeText(this, "Connection Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
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

    private void setupBottomNavigation() {
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
                showMenu();
                return true;
            }
            return false;
        });
    }

    private void showDebugAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}