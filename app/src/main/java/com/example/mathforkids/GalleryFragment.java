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

public class GalleryFragment extends Fragment {
    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        TextView tvGalleryStatus = view.findViewById(R.id.tvGalleryStatus);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        final CloudImageAdapter[] adapterRef = new CloudImageAdapter[1];
        adapterRef[0] = createCloudImageAdapter(tvGalleryStatus, recyclerView);
        recyclerView.setAdapter(adapterRef[0]);

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });

        tvGalleryStatus.setText("جارٍ تحميل اللقطات من Appwrite...");
        AppwriteCloudService.loadCorrectAnswerImages(new AppwriteCloudService.ImagesCallback() {
            @Override
            public void onSuccess(ArrayList<CloudImage> images) {
                ProgressStore.setCloudImages(images);
                adapterRef[0] = createCloudImageAdapter(tvGalleryStatus, recyclerView);
                recyclerView.setAdapter(adapterRef[0]);
                if (images.isEmpty()) {
                    tvGalleryStatus.setText("لا توجد لقطات محفوظة بعد. أجب إجابة صحيحة ثم اضغط احفظ لقطة.");
                } else {
                    tvGalleryStatus.setText("تم تحميل " + images.size() + " لقطة. اضغط على أي لقطة لتكبيرها.");
                }
                tvGalleryStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

            @Override
            public void onError(String message) {
                tvGalleryStatus.setText("لم يتم تحميل اللقطات: " + message);
                tvGalleryStatus.setTextColor(Color.parseColor("#C62828"));
            }
        });

        return view;
    }

    private CloudImageAdapter createCloudImageAdapter(TextView tvGalleryStatus, RecyclerView recyclerView) {
        return new CloudImageAdapter(ProgressStore.getCloudImages(), item -> {
            showDeleteConfirmation(item, tvGalleryStatus, recyclerView);
        });
    }

    private void showDeleteConfirmation(CloudImage item, TextView tvGalleryStatus, RecyclerView recyclerView) {
        speak("هل تريد حذف هذه الصورة؟");
        new AlertDialog.Builder(requireContext())
                .setTitle("تأكيد الحذف")
                .setMessage("هل تريد حذف هذه الصورة فقط؟")
                .setIcon(R.drawable.ic_delete_alert)
                .setPositiveButton("نعم، احذف", (dialog, which) -> deleteCloudImage(item, tvGalleryStatus, recyclerView))
                .setNegativeButton("إلغاء", (dialog, which) -> speak("تم إلغاء الحذف"))
                .show();
    }

    private void deleteCloudImage(CloudImage item, TextView tvGalleryStatus, RecyclerView recyclerView) {
        speak("جار حذف الصورة");
        tvGalleryStatus.setText("جارٍ حذف هذه الصورة من Appwrite...");
        tvGalleryStatus.setTextColor(Color.parseColor("#263238"));
        AppwriteCloudService.deleteImage(item.getDocumentId(), item.getImageUrl(), new AppwriteCloudService.CloudCallback() {
            @Override
            public void onSuccess(String message) {
                ProgressStore.removeCloudImage(item);
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter instanceof CloudImageAdapter) {
                    adapter.notifyDataSetChanged();
                }
                speak("تم حذف الصورة");
                tvGalleryStatus.setText(message);
                tvGalleryStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

            @Override
            public void onError(String message) {
                speak("فشل حذف الصورة");
                tvGalleryStatus.setText("فشل حذف هذه الصورة: " + message);
                tvGalleryStatus.setTextColor(Color.parseColor("#C62828"));
            }
        });
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gallery-delete");
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
