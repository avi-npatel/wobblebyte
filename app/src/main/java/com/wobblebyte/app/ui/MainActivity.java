package com.wobblebyte.app.ui;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wobblebyte.app.R;

/** Hosts the four tabs: Generate, Vault, Quiz and Learn. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keeps generated and saved passwords out of screenshots and the recent-apps preview.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            show(fragmentFor(item.getItemId()));
            return true;
        });

        // After a process restart the fragment manager restores the last tab itself.
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_generate);
        }
    }

    private Fragment fragmentFor(int itemId) {
        if (itemId == R.id.nav_vault) {
            return new VaultFragment();
        }
        if (itemId == R.id.nav_quiz) {
            return new QuizHomeFragment();
        }
        if (itemId == R.id.nav_learn) {
            return new LearnFragment();
        }
        return new GeneratorFragment();
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
