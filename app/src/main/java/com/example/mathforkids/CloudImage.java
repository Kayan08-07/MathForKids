package com.example.mathforkids;

public class CloudImage {
    private final String documentId;
    private final String question;
    private final String imageUrl;

    public CloudImage(String question, String imageUrl) {
        this("", question, imageUrl);
    }

    public CloudImage(String documentId, String question, String imageUrl) {
        this.documentId = documentId;
        this.question = question;
        this.imageUrl = imageUrl;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getQuestion() {
        return question;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
