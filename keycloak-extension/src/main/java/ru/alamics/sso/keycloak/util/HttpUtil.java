package ru.alamics.sso.keycloak.util;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jboss.resteasy.reactive.common.providers.serialisers.FormUrlEncodedProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtil {
    @SneakyThrows(IOException.class)
    public static Form parseFormUrlEncoded(MediaType mediaType, byte[] content) {
        return new FormUrlEncodedProviderExt().doReadFrom(mediaType, new ByteArrayInputStream(content));
    }

    @SneakyThrows(IOException.class)
    public static byte[] writeFormUrlEncoded(MediaType mediaType, Form form) throws IOException {
        var baos = new ByteArrayOutputStream();
        new FormUrlEncodedProviderExt()
                .writeTo(form, null, null, null, mediaType, null, baos);
        return baos.toByteArray();
    }

    private static class FormUrlEncodedProviderExt extends FormUrlEncodedProvider {
        protected Form doReadFrom(MediaType mediaType, InputStream entityStream) throws IOException {
            return super.doReadFrom(mediaType, entityStream);
        }
    }
}
