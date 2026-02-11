package com.example.LuxxHavenApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setSelectedItemId(R.id.nav_home);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();


            if (itemId == R.id.nav_home) {

                return true;
            } else if (itemId == R.id.nav_wishlist) {
                startActivity(new Intent(getApplicationContext(), WishlistActivity.class));
                overridePendingTransition(0, 0); // Removes the animation to make it feel instant
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(getApplicationContext(), BookingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_help) {
                startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}