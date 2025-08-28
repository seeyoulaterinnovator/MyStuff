package ru.alamics.sso.remote.message;

public record SmsRequest(String destination, String text, String serviceName, Boolean simulate) {
}
