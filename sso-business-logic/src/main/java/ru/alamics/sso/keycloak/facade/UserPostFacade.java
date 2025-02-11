package ru.alamics.sso.keycloak.facade;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.NotFoundException;
import lombok.Getter;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.util.validator.NotValidException;

import java.util.List;

@RequestScoped
@Named("UserPostFacade")
public class UserPostFacade {
    @Getter
    protected UserPostService userPostService;

    protected ApplicationProperties properties;

    public UserPostFacade() {

        properties = Lookup.lookup(ApplicationProperties.class);

        userPostService = Lookup.lookup(UserPostService.class);
    }

    public List<UserPostResponse> findByUserId(String userId) throws NotFoundException {
        return userPostService.getUserPost(userId);
    }

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        return userPostService.save(userPostRequest);
    }
}
