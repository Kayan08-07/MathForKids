package com.example.mathforkids;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_QUESTION = "question";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ImageView imgFullScreen = findViewById(R.id.imgFullScreen);
        TextView tvQuestion = findViewById(R.id.tvPreviewQuestion);
        TextView tvClose = findViewById(R.id.tvClose);

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String question = getIntent().getStringExtra(EXTRA_QUESTION);

        tvQuestion.setText(question);
        tvClose.setOnClickListener(v -> finish());

        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .into(imgFullScreen);
    }
}
