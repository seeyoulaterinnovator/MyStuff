package ru.alamics.sso.antifraud.exception;

public class ReachLimitCodeException extends RuntimeException{
    public ReachLimitCodeException(String message) {
        super(message);
    }
}
