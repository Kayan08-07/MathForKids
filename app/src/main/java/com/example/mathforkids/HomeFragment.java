package com.example.mathforkids;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class HomeFragment extends Fragment {
    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView tvConnection = view.findViewById(R.id.tvConnection);
        RadioGroup operationGroup = view.findViewById(R.id.operationGroup);
        Button btnStart = view.findViewById(R.id.btnStart);

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });

        operationGroup.setOnCheckedChangeListener((group, checkedId) -> speakOperationName(checkedId));

        AppwriteClient.testConnection(new AppwriteClient.ConnectionCallback() {
            @Override
            public void onSuccess() {
                tvConnection.setText("الاتصال بالسحابة Appwrite ناجح");
                tvConnection.setTextColor(Color.parseColor("#2E7D32"));
                saveDemoChild(tvConnection);
            }

            @Override
            public void onError(String message) {
                tvConnection.setText("الاتصال بالسحابة لم ينجح: " + message);
                tvConnection.setTextColor(Color.parseColor("#C62828"));
            }
        });

        btnStart.setOnClickListener(v -> {
            String operation = "+";
            int selectedId = operationGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.rbSubtract) operation = "-";
            if (selectedId == R.id.rbMultiply) operation = "*";
            if (selectedId == R.id.rbDivide) operation = "/";

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openExercise(operation);
            }
        });

        return view;
    }

    private void speakOperationName(int selectedId) {
        String operationName = "عملية الجمع";
        if (selectedId == R.id.rbSubtract) operationName = "عملية الطرح";
        if (selectedId == R.id.rbMultiply) operationName = "عملية الضرب";
        if (selectedId == R.id.rbDivide) operationName = "عملية القسمة";

        if (textToSpeech != null) {
            textToSpeech.speak(operationName, TextToSpeech.QUEUE_FLUSH, null, "operation");
        }
    }

    private void saveDemoChild(TextView tvConnection) {
        AppwriteCloudService.saveChildProfile("طفل الحساب", new AppwriteCloudService.CloudCallback() {
            @Override
            public void onSuccess(String message) {
                tvConnection.setText("الاتصال ناجح وتم استخدام جدول children");
                tvConnection.setTextColor(Color.parseColor("#2E7D32"));
            }

            @Override
            public void onError(String message) {
                tvConnection.setText("الاتصال ناجح لكن جدول children لم يحفظ: " + message);
                tvConnection.setTextColor(Color.parseColor("#C62828"));
            }
        });
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
