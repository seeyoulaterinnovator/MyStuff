package ru.alamics.sso.undertow;

import io.undertow.UndertowLogger;
import io.undertow.attribute.ExchangeAttribute;
import io.undertow.attribute.ExchangeAttributeBuilder;
import io.undertow.attribute.ReadOnlyAttributeException;
import io.undertow.conduits.StoredResponseStreamSinkConduit;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @see io.undertow.attribute.StoredResponse
 */
public class FixedStoredResponse implements ExchangeAttribute {

    public static final ExchangeAttribute INSTANCE = new FixedStoredResponse();

    private FixedStoredResponse() {

    }

    @Override
    public String readAttribute(HttpServerExchange exchange) {
        byte[] data = exchange.getAttachment(StoredResponseStreamSinkConduit.RESPONSE);
        if(data == null) {
            return null;
        }
        String charset = extractCharset(exchange.getResponseHeaders());
        if(charset == null) {
            return null;
        }
        try {
            return new String(data, charset);
        } catch (UnsupportedEncodingException e) {
            UndertowLogger.ROOT_LOGGER.debugf(e,"Could not decode response body using charset %s", charset);
            return null;
        }
    }
    private String extractCharset(HeaderMap headers) {
        String contentType = headers.getFirst(Headers.CONTENT_TYPE);
        if (contentType != null) {
            String value = Headers.extractQuotedValueFromHeader(contentType, "charset");
            if (value != null) {
                return value;
            }
            //if it is text we default to ISO_8859_1
            if(contentType.startsWith("text/")) {
                return StandardCharsets.ISO_8859_1.displayName();
            }
            if(contentType.equals("application/json")) {
                return StandardCharsets.UTF_8.displayName();
            }
            return null;
        }
        return null;
    }

    @Override
    public void writeAttribute(HttpServerExchange exchange, String newValue) throws ReadOnlyAttributeException {
        throw new ReadOnlyAttributeException("Stored Response", newValue);
    }

    public static class Builder implements ExchangeAttributeBuilder {

        @Override
        public String name() {
            return "Stored Response";
        }

        @Override
        public ExchangeAttribute build(final String token) {
            if (token.equals("%{STORED_RESPONSE}")) {
                return INSTANCE;
            }
            return null;
        }

        @Override
        public int priority() {
            return 0;
        }
    }
}
