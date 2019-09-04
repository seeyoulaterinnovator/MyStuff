package ru.alamics.sso.registration.service;

import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.registration.dto.UserPostDto;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
public class UserPostService {

    @EJB
    private UserPostRepository accessRepository;
    @EJB
    private UserHistoryLoginRepository repository;

    public UserPost getAccess(String userId, String tomsId) {
        return accessRepository.getUserPost(userId, tomsId);
    }

    public UserPostDto save(UserPostDto userPostDto) {
        UserPost userPost = DataMapper.toUserPost(userPostDto);
        /*if (accessRepository.getUserPost(access.getUserId(), access.getTomsId()) != null) {
            return null;
        }*/
        return DataMapper.toUserPostDto(accessRepository.save(userPost));
    }

    public UserPost edit(UserPost access) {
        UserPost accessDb = accessRepository.getUserPost(access.getId());
        if (accessDb != null) {
            return null;
        }
        return accessRepository.update(access);
    }
}
