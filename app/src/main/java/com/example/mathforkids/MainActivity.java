package com.example.mathforkids;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تهيئة Appwrite مرة واحدة عند فتح التطبيق.
        AppwriteClient.init(this);
        setContentView(R.layout.activity_main);

        Button btnHome = findViewById(R.id.btnHome);
        Button btnProgress = findViewById(R.id.btnProgress);
        Button btnGallery = findViewById(R.id.btnGallery);

        btnHome.setOnClickListener(v -> openFragment(new HomeFragment()));
        btnProgress.setOnClickListener(v -> openFragment(new ProgressFragment()));
        btnGallery.setOnClickListener(v -> openFragment(new GalleryFragment()));

        openFragment(new HomeFragment());
    }

    public void openExercise(String operation) {
        openFragment(ExerciseFragment.newInstance(operation));
    }

    private void openFragment(Fragment fragment) {
        // كل شاشة داخل التطبيق عبارة عن Fragment داخل نفس الـ Activity.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
