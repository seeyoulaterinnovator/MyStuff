package ru.alamics.sso.status;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import ru.alamics.sso.jpa.repository.StatusRepository;

@ApplicationScoped
public class StatusService {
    private final String NODE_NAME = System.getProperty("jboss.node.name"); // TODO upgrade: check

    @Inject
    StatusRepository statusRepository;

    void onStart(@Observes StartupEvent ev) {
        statusRepository.tryInsertNodeName(NODE_NAME);
    }

    public boolean checkStatusDb() {
        return statusRepository.checkStatusDb(NODE_NAME);
    }
}
