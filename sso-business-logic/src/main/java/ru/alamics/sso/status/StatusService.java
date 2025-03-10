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

    public String getNodeName() {
        String dc = System.getenv("DC_NAME");
        if (dc == null) dc = "";
        String pod = System.getenv("POD_NAME"); // i.e. k8s metadata.name
        if (pod == null) pod = System.getenv("HOSTNAME");
        if(pod == null) {
            try {
                pod = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn(e.getMessage(), e);
            }
        }
        if(pod == null) pod = "";
        return dc + pod;
    }
}
