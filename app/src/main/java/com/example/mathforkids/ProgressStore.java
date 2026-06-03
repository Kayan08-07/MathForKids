package com.example.mathforkids;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProgressStore {
    private static final List<ProgressItem> progressItems = new ArrayList<>();
    private static final List<CloudImage> cloudImages = new ArrayList<>();

    public static void addProgress(ProgressItem item) {
        progressItems.add(0, item);
    }

    public static void setProgressItems(List<ProgressItem> items) {
        progressItems.clear();
        progressItems.addAll(items);
    }

    public static void clearProgressItems() {
        progressItems.clear();
    }

    public static void removeProgressItem(ProgressItem item) {
        progressItems.remove(item);
    }

    public static List<ProgressItem> getProgressItems() {
        return Collections.unmodifiableList(progressItems);
    }

    public static void addCloudImage(CloudImage image) {
        cloudImages.add(0, image);
    }

    public static void setCloudImages(List<CloudImage> images) {
        cloudImages.clear();
        cloudImages.addAll(images);
    }

    public static void clearCloudImages() {
        cloudImages.clear();
    }

    public static void removeCloudImage(CloudImage image) {
        cloudImages.remove(image);
    }

    public static List<CloudImage> getCloudImages() {
        return Collections.unmodifiableList(cloudImages);
    }
}
