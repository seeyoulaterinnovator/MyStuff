package ru.alamics.sso.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SslContextKind {
    RIAS("rias.ssl.relaxed", "rias.ssl.thumbprints"),
    RIAS_LOGIN("riasLogin.ssl.relaxed", "riasLogin.ssl.thumbprints"),
    TBAPI_REGISTRATION("tbapi.registration.ssl.relaxed", "tbapi.registration.ssl.thumbprints"),
    TBAPI_CUSTOMER("tbapi.customer.ssl.relaxed", "tbapi.customer.ssl.thumbprints"),
    CITIES("cities.ssl.relaxed", "cities.ssl.thumbprints"),
    DA_DATA("dadata.ssl.relaxed", "dadata.ssl.thumbprints"),
    SMS_SENDER("smsSender.ssl.relaxed", "smsSender.ssl.thumbprints");

    final String relaxedProperty;

    final String thumbprintsProperty;
}
