package ru.alamics.sso.status;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.repository.StatusRepository;

import javax.annotation.PostConstruct;
import javax.ejb.*;

@Singleton
@Startup
@Slf4j
@Lock(LockType.READ)
public class StatusService {

    private final String NODE_NAME = System.getProperty("jboss.node.name");

    @EJB
    private StatusRepository statusRepository;

    @PostConstruct
    @Lock(LockType.WRITE)
    public void init() {

        tryInsertNodeName();
    }

    private void tryInsertNodeName() {
        statusRepository.tryInsertNodeName(NODE_NAME);
    }

    public boolean checkStatusDb() {
        return statusRepository.checkStatusDb(NODE_NAME);
    }
}
