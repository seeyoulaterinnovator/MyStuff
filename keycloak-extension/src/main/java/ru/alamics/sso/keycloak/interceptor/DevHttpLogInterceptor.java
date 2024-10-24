package ru.alamics.sso.keycloak.interceptor;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.*;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.repository.DevHttpLogRepository;
import ru.alamics.sso.keycloak.util.HttpUtil;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.status.StatusService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.alamics.sso.keycloak.util.HttpUtil.getMediaTypeCharset;

@Provider
@Slf4j
public class DevHttpLogInterceptor implements ContainerRequestFilter, ContainerResponseFilter, ReaderInterceptor, WriterInterceptor {
    private static final String LOG_ID = "X-Dev-Log-ID";

    private static final AtomicBoolean ENABLED = new AtomicBoolean(true);

    private static final AtomicLong COUNTER = new AtomicLong();

    @Inject
    ApplicationProperties properties;

    @Inject
    StatusService statusService;

    @Inject
    DevHttpLogRepository devHttpLogRepository;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        if(!isEnabled()) return;

        if(Stream.of(properties.getProperty("dev.httpLogPaths", "").split(","))
                .map(it -> {
                    try {
                        return Pattern.compile(it);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .noneMatch(path -> path.asMatchPredicate().test(context.getUriInfo().getPath()))
        ) return;

        context.setProperty(
                LOG_ID,
                devHttpLogRepository.addRequest(
                        statusService.getNodeName(),
                        context.getUriInfo().getRequestUriBuilder().build().toURL(),
                        context.getMethod(),
                        context.getHeaders()
                )
        );

        if(COUNTER.incrementAndGet() % 100 == 0) {
            long maxCount = properties.getPropertyLong("dev.httpLogMaxCountPerNode", -1);
            if(maxCount <= 0) maxCount = 10_000;
            devHttpLogRepository.rollup(statusService.getNodeName(), maxCount);
        }
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        var logId = context.getProperty(LOG_ID);
        if (logId == null) return context.proceed();
        InputStream stream = context.getInputStream();
        try {
            var data = stream.readAllBytes();
            context.setInputStream(new ByteArrayInputStream(data));
            var result = context.proceed();
            if(HttpUtil.isTextMediaType(context.getMediaType())) {
                devHttpLogRepository.addRequestBody(
                        (UUID) logId,
                        new String(data, getMediaTypeCharset(context.getMediaType()))
                );
            } else {
                devHttpLogRepository.addRequestBody((UUID) logId, data);
            }
            return result;
        } finally {
            context.setInputStream(stream);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        var logId = requestContext.getProperty(LOG_ID);
        if (logId == null) return;
        devHttpLogRepository.addResponse(
                (UUID) logId,
                responseContext.getStatus(),
                responseContext.getHeaders()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream().map(Object::toString).collect(Collectors.toList())
                        ))
        );
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
            if(HttpUtil.isTextMediaType(context.getMediaType())) {
                devHttpLogRepository.addResponseBody(
                        (UUID) logId,
                        baos.toString(getMediaTypeCharset(context.getMediaType()))
                );
            } else {
                devHttpLogRepository.addResponseBody((UUID) logId, baos.toByteArray());
            }
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
