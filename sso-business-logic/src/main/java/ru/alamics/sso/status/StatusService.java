package ru.alamics.sso.status;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.repository.StatusRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;

@ApplicationScoped
@Slf4j
public class StatusService {
    @Inject
    StatusRepository statusRepository;

    void onStart(@Observes StartupEvent ev) {
        statusRepository.tryInsertNodeName(getNodeName());
    }

    public boolean checkStatusDb() {
        return statusRepository.checkStatusDb(getNodeName());
    }

    // TODO k8s
    public String getNodeName() {
        String name = System.getProperty("jboss.node.name");
        if(name == null) {
            // i.e. k8s metadata.name
            name = System.getenv("POD_NAME");
        }
        if(name == null) {
            try {
                name = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn(e.getMessage(), e);
            }
        }
        if(name == null) {
            name = "";
        }
        return name;
    }
}
