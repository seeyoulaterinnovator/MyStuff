package ru.alamics.sso.registration;

public class FoundUserPostException extends Exception {

    private String postId;

    public FoundUserPostException(String postId, String message) {
        super(message);
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }
}
