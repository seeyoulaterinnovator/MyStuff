package ru.alamics.sso.jpa.repository;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import ru.alamics.sso.jpa.entity.DevHttpLogEntity;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional(Transactional.TxType.REQUIRES_NEW)
public class DevHttpLogRepository {
    final ObjectMapper objectMapper = new ObjectMapper();

    final ReentrantLock lock = new ReentrantLock();

    @Inject
    EntityManager em;

    public String addRequest(String node, URL url, String method, Map<String, List<String>> headers) {
        var id = UUID.randomUUID().toString();
        em.persist(
                DevHttpLogEntity.builder()
                        .id(id)
                        .node(node)
                        .requestedAt(Instant.now())
                        .url(url.toString())
                        .method(method)
                        .requestHeaders(convertHeaders(headers))
                        .build()
        );
        return id;
    }

    public void addRequestBody(String id, String body) {
        var log = em.find(DevHttpLogEntity.class, id);
        if (log != null) log.setRequestBodyText(body);
    }

    public void addRequestBody(String id, byte[] body) {
        var log = em.find(DevHttpLogEntity.class, id);
        if (log != null) log.setRequestBodyBinary(body);
    }

    public void addResponse(String id, int status, Map<String, List<String>> headers) {
        var log = em.find(DevHttpLogEntity.class, id);
        if (log != null) {
            log.setRespondedAt(Instant.now());
            log.setStatus(status);
            log.setResponseHeaders(convertHeaders(headers));
        }
    }

    public void addResponseBody(String id, String body) {
        var log = em.find(DevHttpLogEntity.class, id);
        if (log != null) log.setResponseBodyText(body);
    }

    public void addResponseBody(String id, byte[] body) {
        var log = em.find(DevHttpLogEntity.class, id);
        if (log != null) log.setResponseBodyBinary(body);
    }

    public void rollup(String node, long maxCount) {
        if(!lock.tryLock()) return;
        try {
            long count = Long.parseLong(
                    em.createQuery("select count(e) from DevHttpLogEntity e where e.node = :node and not e.pinned")
                            .setParameter("node", node)
                            .getSingleResult()
                            .toString()
            );
            if(count > 2L * maxCount) {
                em.createNativeQuery("delete from DEV_HTTP_LOG " +
                                "where NODE = :node and not PINNED " +
                                "order by REQUESTED_AT asc " +
                                "limit :limit")
                        .setParameter("node", node)
                        .setParameter("limit", maxCount)
                        .executeUpdate();
            }
        } finally {
            lock.unlock();
        }
    }

    @SneakyThrows(JsonProcessingException.class)
    private String convertHeaders(Map<String, List<String>> headers) {
        headers = headers.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toLowerCase(),
                        Map.Entry::getValue,
                        (value1, value2) -> {
                            var value = new ArrayList<>(value1);
                            value.addAll(value2);
                            return value;
                        },
                        LinkedHashMap::new
                ));
        return objectMapper.writeValueAsString(headers );
    }
}
