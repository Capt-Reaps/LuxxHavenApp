package com.example.LuxxHavenApp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RescheduleDetailsActivity extends AppCompatActivity {

    private TextView tvPolicy, tvNewCheckIn, tvNewCheckOut;
    private CalendarView calendarView;
    private Button btnSubmit;
    private ImageButton btnBack;

    private long durationDays = 1;
    private String selectedCheckInDate = "";
    private String computedCheckOutDate = "";
    private String bookingRef = "";
    private int unitId = 0;
    private final List<JSONObject> bookedRanges = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule_details);


        tvPolicy = findViewById(R.id.tv_policy_text);
        tvNewCheckIn = findViewById(R.id.tv_new_checkin);
        tvNewCheckOut = findViewById(R.id.tv_new_checkout);
        calendarView = findViewById(R.id.calendar_view);
        btnSubmit = findViewById(R.id.btn_submit_reschedule);
        btnBack = findViewById(R.id.btn_back);


        Intent intent = getIntent();
        if (intent != null) {
            bookingRef = intent.getStringExtra("BOOKING_REF");
            unitId = intent.getIntExtra("UNIT_ID", 0);
            String oldCheckIn = intent.getStringExtra("CHECK_IN");
            String oldCheckOut = intent.getStringExtra("CHECK_OUT");

            if (unitId == 0) {
                Toast.makeText(this, "Error: Invalid Unit ID", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            calculateDuration(oldCheckIn, oldCheckOut);


            long minSelectableMillis = calculateMinDate(oldCheckIn);
            calendarView.setMinDate(minSelectableMillis);
        }

        fetchBookedDates();


        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String dateToCheck = String.format(Locale.US, "%d-%02d-%02d", year, (month + 1), dayOfMonth);

            if (isDateBooked(dateToCheck)) {
                Toast.makeText(this, "This date is unavailable.", Toast.LENGTH_SHORT).show();
                tvNewCheckIn.setText("Select Valid Date");
                tvNewCheckOut.setText("-");
                selectedCheckInDate = "";
            } else {
                selectedCheckInDate = dateToCheck;
                tvNewCheckIn.setText(selectedCheckInDate);
                calculateNewCheckOut(year, month, dayOfMonth);
            }
        });

        // Submit Button
        btnSubmit.setOnClickListener(v -> {
            if (selectedCheckInDate.isEmpty()) {
                Toast.makeText(this, "Please select a valid date", Toast.LENGTH_SHORT).show();
            } else {
                submitRescheduleRequest();
            }
        });


        if (btnBack != null) {
            btnBack.setOnClickListener(v -> showExitConfirmation());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
    }

    private void fetchBookedDates() {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/get_booked_dates.php?unit_id=" + unitId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        bookedRanges.clear();
                        for (int i = 0; i < response.length(); i++) {
                            bookedRanges.add(response.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("API_ERROR", "Volley Error: " + error.getMessage()));

        Volley.newRequestQueue(this).add(request);
    }

    private boolean isDateBooked(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date target = sdf.parse(dateString);

            for (JSONObject range : bookedRanges) {
                String startStr = range.optString("check_in_date");
                String endStr = range.optString("check_out_date");

                if (startStr.isEmpty() || endStr.isEmpty()) continue;

                Date start = sdf.parse(startStr);
                Date end = sdf.parse(endStr);

                if (target != null && start != null && end != null) {
                    if (!target.before(start) && !target.after(end)) {
                        return true;
                    }
                }
            }
        } catch (ParseException e) {
            Log.e("DATE_CHECK", "Date parsing failed");
        }
        return false;
    }

    private void submitRescheduleRequest() {
        String url = "https://luxxhaven.bscs3b.com/src/backend/api/submit_reschedule.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.optBoolean("success", false);
                        String status = json.optString("status", "");

                        if (success || "success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Request Sent Successfully!", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(RescheduleDetailsActivity.this, HelpActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {
                            String msg = json.optString("message", "Unknown error");
                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid Server Response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("booking_ref", bookingRef);
                params.put("new_check_in", selectedCheckInDate);
                params.put("new_check_out", computedCheckOutDate);
                params.put("unit_id", String.valueOf(unitId));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void calculateDuration(String start, String end) {
        if (start == null || end == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date d1 = sdf.parse(start);
            Date d2 = sdf.parse(end);
            if (d1 != null && d2 != null) {
                long diff = d2.getTime() - d1.getTime();
                durationDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (durationDays < 1) durationDays = 1;

                tvPolicy.setText(String.format(Locale.US, "Original Booking: %d Night(s)\n(%s to %s)", durationDays, start, end));
            }
        } catch (ParseException e) {
            durationDays = 1;
        }
    }


    private long calculateMinDate(String originalCheckIn) {
        if (originalCheckIn == null) return System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = sdf.parse(originalCheckIn);
            Calendar cal = Calendar.getInstance();
            if (date != null) cal.setTime(date);


            cal.add(Calendar.DAY_OF_MONTH, 7);

            long minAllowedDate = cal.getTimeInMillis();
            long today = System.currentTimeMillis();


            return Math.max(minAllowedDate, today);

        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

    private void calculateNewCheckOut(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        cal.add(Calendar.DAY_OF_MONTH, (int) durationDays);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        computedCheckOutDate = sdf.format(cal.getTime());
        tvNewCheckOut.setText(computedCheckOutDate);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Request?")
                .setMessage("Are you sure you want to go back?")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }
}