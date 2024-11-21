package ru.alamics.sso.keycloak.interceptor;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import ru.alamics.sso.jpa.repository.DevHttpLogRepository;
import ru.alamics.sso.keycloak.util.HttpUtil;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.status.StatusService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.alamics.sso.keycloak.util.HttpUtil.getMediaTypeCharset;

@Provider
@PreMatching
@Slf4j
public class DevHttpLogInterceptor implements ContainerResponseFilter, WriterInterceptor {
    private static final String LOG_ID = "X-Dev-Log-ID";

    private static final AtomicBoolean ENABLED = new AtomicBoolean(true);

    private static final AtomicLong COUNTER = new AtomicLong();

    @Inject
    ApplicationProperties properties;

    @Inject
    StatusService statusService;

    @Inject
    DevHttpLogRepository devHttpLogRepository;

    @ServerRequestFilter(preMatching = true)
    public Uni<Void> filter(ContainerRequestContext context) throws Exception {
        var url = context.getUriInfo().getRequestUriBuilder().build().toURL();

        return Uni.createFrom().voidItem()
                .chain(() -> {
                    if(isEnabled() &&
                            properties.getPropertyList("dev.httpLogPaths")
                                    .stream()
                                    .map(it -> {
                                        try {
                                            return Pattern.compile(it);
                                        } catch (Exception e) {
                                            return null;
                                        }
                                    })
                                    .filter(Objects::nonNull)
                                    .anyMatch(path -> path.asMatchPredicate().test(context.getUriInfo().getPath()))
                    ) {
                        return Uni.createFrom().item(true);
                    }
                    return Uni.createFrom().nullItem();
                })
                .onItem().ifNotNull().invoke(() -> {
                    byte[] content;
                    try(var stream = context.getEntityStream()) {
                        if(context.getLength() > 0) {
                            content = stream.readNBytes(context.getLength());
                        } else {
                            content = stream.readAllBytes();
                        }
                    } catch (IOException e) {
                        throw new WebApplicationException(e);
                    }
                    var logId = devHttpLogRepository.addRequest(
                            statusService.getNodeName(),
                            url,
                            context.getMethod(),
                            context.getHeaders()
                    );
                    context.setProperty(LOG_ID, logId);
                    if(HttpUtil.isTextMediaType(context.getMediaType())) {
                        devHttpLogRepository.addRequestBody(
                                logId,
                                new String(content, getMediaTypeCharset(context.getMediaType()))
                        );
                    } else if(content.length > 0) {
                        devHttpLogRepository.addRequestBody(logId, content);
                    }
                    context.setEntityStream(new ByteArrayInputStream(content));
                })
                .invoke(() -> {
                    long maxCount = properties.getPositivePropertyLong("dev.httpLogMaxCountPerNode", 10_000);
                    if(COUNTER.incrementAndGet() % Math.max(100, maxCount / 100) == 0) {
                        devHttpLogRepository.rollup(statusService.getNodeName(), maxCount);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .replaceWithVoid();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        var logId = requestContext.getProperty(LOG_ID);
        if (logId == null) return;
        QuarkusTransaction.requiringNew().run(() -> {
            devHttpLogRepository.addResponse(
                    (String) logId,
                    responseContext.getStatus(),
                    responseContext.getHeaders()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().stream().map(Object::toString).collect(Collectors.toList())
                            ))
            );
        });
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        var logId = context.getProperty(LOG_ID);
        if (logId == null) {
            context.proceed();
            return;
        }
        var stream = context.getOutputStream();
        var baos = new ByteArrayOutputStream();
        context.setOutputStream(baos);
        try {
            context.proceed();
            baos.writeTo(stream);
            QuarkusTransaction.requiringNew().run(() -> {
                if (HttpUtil.isTextMediaType(context.getMediaType())) {
                    devHttpLogRepository.addResponseBody(
                            (String) logId,
                            baos.toString(getMediaTypeCharset(context.getMediaType()))
                    );
                } else {
                    devHttpLogRepository.addResponseBody((String) logId, baos.toByteArray());
                }
            });
        } finally {
            context.setOutputStream(stream);
        }
    }

    private boolean isEnabled() {
        boolean enabled = "true".equals(System.getenv().get("DEV_HTTP_LOG_ALLOWED"))
                && "true".equals(properties.getProperty("dev.httpLogEnabled"));
        if(ENABLED.compareAndSet(!enabled, enabled)) {
            log.warn("Dev HTTP DB logger {}", enabled ? "enabled" : "disabled");
        }
        return enabled;
    }
}
