package com.example.mathforkids;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

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
        RadioGroup operationGroup = view.findViewById(R.id.operationGroup);
        Button btnStart = view.findViewById(R.id.btnStart);

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });

        operationGroup.setOnCheckedChangeListener((group, checkedId) -> speakOperationName(checkedId));

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

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
