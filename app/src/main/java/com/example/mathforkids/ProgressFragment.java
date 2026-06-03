package com.example.mathforkids;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        TextView tvProgressStatus = view.findViewById(R.id.tvProgressStatus);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerProgress);
        final ProgressAdapter[] adapterRef = new ProgressAdapter[1];
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterRef[0] = createProgressAdapter(tvProgressStatus, recyclerView);
        recyclerView.setAdapter(adapterRef[0]);

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });

        tvProgressStatus.setText("جارٍ تحميل سجل التقدم من Appwrite...");
        AppwriteCloudService.loadResults(new AppwriteCloudService.ResultsCallback() {
            @Override
            public void onSuccess(ArrayList<ProgressItem> results) {
                ProgressStore.setProgressItems(results);
                adapterRef[0] = createProgressAdapter(tvProgressStatus, recyclerView);
                recyclerView.setAdapter(adapterRef[0]);
                if (results.isEmpty()) {
                    tvProgressStatus.setText("لا يوجد تقدم محفوظ بعد. حل بعض التمارين أولاً.");
                } else {
                    tvProgressStatus.setText("تم تحميل " + results.size() + " نتيجة من السحابة. الأحدث يظهر أولاً.");
                }
                tvProgressStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

            @Override
            public void onError(String message) {
                tvProgressStatus.setText("لم يتم تحميل سجل التقدم: " + message);
                tvProgressStatus.setTextColor(Color.parseColor("#C62828"));
            }
        });

        return view;
    }

    private ProgressAdapter createProgressAdapter(TextView tvProgressStatus, RecyclerView recyclerView) {
        return new ProgressAdapter(ProgressStore.getProgressItems(), item -> {
            showDeleteConfirmation(item, tvProgressStatus, recyclerView);
        });
    }

    private void showDeleteConfirmation(ProgressItem item, TextView tvProgressStatus, RecyclerView recyclerView) {
        speak("هل تريد حذف هذا السجل؟");
        new AlertDialog.Builder(requireContext())
                .setTitle("تأكيد الحذف")
                .setMessage("هل تريد حذف هذا السجل فقط؟")
                .setIcon(R.drawable.ic_delete_alert)
                .setPositiveButton("نعم، احذف", (dialog, which) -> deleteProgressItem(item, tvProgressStatus, recyclerView))
                .setNegativeButton("إلغاء", (dialog, which) -> speak("تم إلغاء الحذف"))
                .show();
    }

    private void deleteProgressItem(ProgressItem item, TextView tvProgressStatus, RecyclerView recyclerView) {
        speak("جار حذف السجل");
        tvProgressStatus.setText("جارٍ حذف هذا السجل من Appwrite...");
        tvProgressStatus.setTextColor(Color.parseColor("#263238"));
        AppwriteCloudService.deleteResult(item.getDocumentId(), new AppwriteCloudService.CloudCallback() {
            @Override
            public void onSuccess(String message) {
                ProgressStore.removeProgressItem(item);
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter instanceof ProgressAdapter) {
                    adapter.notifyDataSetChanged();
                }
                speak("تم حذف السجل");
                tvProgressStatus.setText(message);
                tvProgressStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

            @Override
            public void onError(String message) {
                speak("فشل حذف السجل");
                tvProgressStatus.setText("فشل حذف هذا السجل: " + message);
                tvProgressStatus.setTextColor(Color.parseColor("#C62828"));
            }
        });
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "progress-delete");
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
