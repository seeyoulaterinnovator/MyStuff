package ru.alamics.sso.keycloak.util;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.common.providers.serialisers.FormUrlEncodedProvider;
import org.jboss.resteasy.reactive.common.providers.serialisers.MessageReaderUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class HttpUtil {
    private static final List<MediaType> TEXT_MEDIA_TYPES = List.of(
            TEXT_PLAIN_TYPE,
            TEXT_HTML_TYPE,
            TEXT_XML_TYPE,
            APPLICATION_JSON_TYPE,
            APPLICATION_FORM_URLENCODED_TYPE,
            APPLICATION_XML_TYPE,
            new MediaType("text", "csv")
    );

    @SneakyThrows(IOException.class)
    public static Form parseFormUrlEncoded(MediaType mediaType, byte[] content) {
        return new FormUrlEncodedProviderExt().doReadFrom(mediaType, new ByteArrayInputStream(content));
    }

    @SneakyThrows(IOException.class)
    public static byte[] writeFormUrlEncoded(MediaType mediaType, Form form) {
        var baos = new ByteArrayOutputStream();
        new FormUrlEncodedProviderExt()
                .writeTo(form, null, null, null, mediaType, null, baos);
        return baos.toByteArray();
    }

    public static boolean isTextMediaType(MediaType mediaType) {
        return mediaType != null
                && !mediaType.isWildcardType()
                && TEXT_MEDIA_TYPES.stream().anyMatch(it -> it.isCompatible(mediaType));
    }

    public static Charset getMediaTypeCharset(MediaType mediaType) {
        if (mediaType != null) {
            try {
                return Charset.forName(MessageReaderUtil.charsetFromMediaType(mediaType));
            } catch (Exception e) {
                log.trace(e.getMessage(), e);
            }
        }
        return StandardCharsets.ISO_8859_1;
    }

    public static boolean isSameDomain(String url1, String url2, int level) {
        return getDomainList(url1, level).equals(getDomainList(url2, level));
    }

    public static List<String> getDomainList(String url) {
        var list = Arrays.asList(URI.create(url).getHost().split("\\."));
        Collections.reverse(list);
        return list;
    }

    public static List<String> getDomainList(String url, int level) {
        var list = getDomainList(url);
        list = list.subList(0, Math.min(list.size(), level));
        return list;
    }

    private static class FormUrlEncodedProviderExt extends FormUrlEncodedProvider {
        protected Form doReadFrom(MediaType mediaType, InputStream entityStream) throws IOException {
            return super.doReadFrom(mediaType, entityStream);
        }
    }
}
