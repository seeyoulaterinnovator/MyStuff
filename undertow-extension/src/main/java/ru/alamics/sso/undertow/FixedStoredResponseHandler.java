package ru.alamics.sso.undertow;

import io.undertow.server.ConduitWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.StoredResponseHandler;
import io.undertow.util.ConduitFactory;
import org.xnio.conduits.StreamSinkConduit;

public class FixedStoredResponseHandler extends StoredResponseHandler {
    private final HttpHandler next;

    public FixedStoredResponseHandler(HttpHandler next) {
        super(next);
        this.next = next;

    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if(!Boolean.TRUE.toString().equals(System.getenv().get("UNDERTOW_HTTP_DUMP_ENABLED"))) {
            next.handleRequest(exchange);
            return;
        }

        exchange.addResponseWrapper(new ConduitWrapper<StreamSinkConduit>() {
            @Override
            public StreamSinkConduit wrap(ConduitFactory<StreamSinkConduit> factory, HttpServerExchange exchange) {
                return new FixedStoredResponseStreamSinkConduit(factory.create(), exchange);
            }
        });
        next.handleRequest(exchange);
    }

}
