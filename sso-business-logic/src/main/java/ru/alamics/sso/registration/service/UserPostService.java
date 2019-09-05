package ru.alamics.sso.registration.service;

import javassist.NotFoundException;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostDto;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class UserPostService {

    @EJB
    private UserPostRepository accessRepository;

    public UserPostDto save(UserPostDto userPostDto) throws FoundUserPostException {
        if (accessRepository.getUserPost(userPostDto.getUserId(), userPostDto.getTomsId()) != null) {
            throw new FoundUserPostException();
        }
        UserPost userPost = DataMapper.toUserPost(userPostDto);
        return DataMapper.toUserPostDto(accessRepository.save(userPost));
    }

    public UserPostDto edit(UserPostDto userPostDto) throws NotFoundException {
        UserPost userPost = accessRepository.getUserPost(userPostDto.getId());
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostDto(accessRepository.update(DataMapper.toUserPost(userPostDto)));
    }

    public void remove(String id) throws NotFoundException {
        if (accessRepository.getUserPost(id) == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        accessRepository.remove(id);
    }
}
