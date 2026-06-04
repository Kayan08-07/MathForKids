package com.example.mathforkids;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class ExerciseFragment extends Fragment {
    private static final String ARG_OPERATION = "operation";

    private final MathQuestionGenerator generator = new MathQuestionGenerator();
    private MathQuestion currentQuestion;
    private TextToSpeech textToSpeech;
    private TextView tvQuestion;
    private TextView tvFeedback;
    private TextView tvCloudStatus;
    private EditText etAnswer;
    private boolean answerConfirmedCorrect = false;

    public static ExerciseFragment newInstance(String operation) {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OPERATION, operation);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        ImageView imgMascot = view.findViewById(R.id.imgMascot);
        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvFeedback = view.findViewById(R.id.tvFeedback);
        tvCloudStatus = view.findViewById(R.id.tvCloudStatus);
        etAnswer = view.findViewById(R.id.etAnswer);
        Button btnSpeak = view.findViewById(R.id.btnSpeak);
        Button btnCamera = view.findViewById(R.id.btnCamera);
        Button btnCheck = view.findViewById(R.id.btnCheck);

        Glide.with(this)
                .load("https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=400")
                .into(imgMascot);

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("ar"));
            }
        });

        createNewQuestion();
        btnSpeak.setOnClickListener(v -> speakQuestion());
        btnCamera.setOnClickListener(v -> saveAnswerScreenshot());
        btnCheck.setOnClickListener(v -> checkAnswer());

        return view;
    }

    private void createNewQuestion() {
        String operation = "+";
        if (getArguments() != null) {
            operation = getArguments().getString(ARG_OPERATION, "+");
        }
        currentQuestion = generator.generate(operation);
        answerConfirmedCorrect = false;
        tvQuestion.setText(currentQuestion.getQuestionText());
        etAnswer.setText("");
        tvFeedback.setText("فكّر بهدوء ثم اكتب الإجابة");
        tvFeedback.setTextColor(Color.parseColor("#263238"));
        tvCloudStatus.setText("جاهز لحفظ النتيجة في جدول results");
        tvCloudStatus.setTextColor(Color.parseColor("#263238"));
    }

    private void speakQuestion() {
        if (textToSpeech != null) {
            textToSpeech.speak(currentQuestion.getArabicQuestion(), TextToSpeech.QUEUE_FLUSH, null, "question");
        }
    }

    private void checkAnswer() {
        String answerText = etAnswer.getText().toString().trim();
        if (answerText.isEmpty()) {
            tvFeedback.setText("اكتب إجابتك أولاً");
            tvFeedback.setTextColor(Color.parseColor("#C62828"));
            return;
        }

        int childAnswer = Integer.parseInt(answerText);
        boolean correct = childAnswer == currentQuestion.getAnswer();
        ProgressItem item = new ProgressItem(
                currentQuestion.getQuestionText(),
                childAnswer,
                currentQuestion.getAnswer(),
                correct
        );
        ProgressStore.addProgress(item);

        // نحفظ نتيجة الطفل في جدول results داخل Appwrite إذا كانت الصلاحيات جاهزة.
        tvCloudStatus.setText("جارٍ حفظ النتيجة في جدول results...");
        AppwriteCloudService.saveResult(
                currentQuestion.getQuestionText(),
                childAnswer,
                currentQuestion.getAnswer(),
                correct,
                new AppwriteCloudService.CloudCallback() {
                    @Override
                    public void onSuccess(String message) {
                        tvCloudStatus.setText("تم حفظ النتيجة في جدول results");
                        tvCloudStatus.setTextColor(Color.parseColor("#2E7D32"));
                    }

                    @Override
                    public void onError(String message) {
                        tvCloudStatus.setText("فشل حفظ النتيجة في results: " + message);
                        tvCloudStatus.setTextColor(Color.parseColor("#C62828"));
                    }
                }
        );

        if (correct) {
            answerConfirmedCorrect = true;
            tvFeedback.setText("أحسنت! إجابة صحيحة. اضغط حفظ اللقطة");
            tvFeedback.setTextColor(Color.parseColor("#2E7D32"));
            speak("أحسنت، إجابة صحيحة. احفظ اللقطة الآن");
        } else {
            answerConfirmedCorrect = false;
            tvFeedback.setText("محاولة جميلة. الإجابة الصحيحة هي: " + currentQuestion.getAnswer());
            tvFeedback.setTextColor(Color.parseColor("#C62828"));
            speak("حاول مرة أخرى");
        }
    }

    private void saveAnswerScreenshot() {
        if (!answerConfirmedCorrect) {
            tvFeedback.setText("أجب إجابة صحيحة أولاً، ثم احفظ لقطة الإجابة");
            tvFeedback.setTextColor(Color.parseColor("#C62828"));
            speak("أجب إجابة صحيحة أولاً");
            return;
        }

        tvFeedback.setText("أحسنت! يتم الآن حفظ لقطة الإجابة الصحيحة...");
        tvFeedback.setTextColor(Color.parseColor("#2E7D32"));
        tvCloudStatus.setText("جارٍ رفع لقطة الشاشة إلى Storage...");

        String studentAnswer = etAnswer.getText().toString().trim();
        Bitmap screenshot = createAnswerCertificateBitmap(studentAnswer);

        // نرفع لقطة الشاشة التي تحتوي على كلمة "أحسنت" إلى Storage ثم نسجل رابطها في جدول solution_images.
        AppwriteCloudService.uploadSolutionImage(currentQuestion.getQuestionText(), screenshot, new AppwriteCloudService.CloudCallback() {
            @Override
            public void onSuccess(String message) {
                ProgressStore.addCloudImage(new CloudImage(currentQuestion.getQuestionText(), message));
                tvFeedback.setText("تم حفظ لقطة الإجابة الصحيحة في Appwrite");
                tvFeedback.setTextColor(Color.parseColor("#2E7D32"));
                tvCloudStatus.setText("تم رفع اللقطة وتسجيلها كإجابة صحيحة في جدول solution_images");
                tvCloudStatus.setTextColor(Color.parseColor("#2E7D32"));
                tvFeedback.postDelayed(() -> createNewQuestion(), 1500);
            }

            @Override
            public void onError(String message) {
                tvFeedback.setText("لم يتم حفظ لقطة الإجابة: " + message);
                tvFeedback.setTextColor(Color.parseColor("#C62828"));
                tvCloudStatus.setText("فشل رفع/تسجيل اللقطة: " + message);
                tvCloudStatus.setTextColor(Color.parseColor("#C62828"));
            }
        });
    }

    private Bitmap createAnswerCertificateBitmap(String studentAnswer) {
        int width = 1080;
        int height = 1400;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawColor(Color.parseColor("#FFF7D6"));

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        paint.setColor(Color.parseColor("#6C63FF"));
        paint.setTextSize(110.4f);
        canvas.drawText("أحسنت!", width / 2f, 220, paint);

        paint.setColor(Color.parseColor("#263238"));
        paint.setTextSize(64.8f);
        canvas.drawText("إجابة صحيحة", width / 2f, 320, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(57.6f);
        canvas.drawText("السؤال:", width / 2f, 470, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(91.2f);
        canvas.drawText(currentQuestion.getQuestionText(), width / 2f, 580, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(57.6f);
        canvas.drawText("إجابة الطالب:", width / 2f, 760, paint);

        paint.setColor(Color.parseColor("#2E7D32"));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(110.4f);
        canvas.drawText(studentAnswer, width / 2f, 880, paint);

        paint.setColor(Color.parseColor("#263238"));
        paint.setTextSize(50.4f);
        canvas.drawText("الإجابة الصحيحة: " + currentQuestion.getAnswer(), width / 2f, 1020, paint);

        paint.setColor(Color.parseColor("#FFB703"));
        paint.setTextSize(55.2f);
        canvas.drawText("تعلّم الحساب للأطفال", width / 2f, 1220, paint);

        return bitmap;
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "feedback");
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
