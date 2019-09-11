package ru.alamics.sso.registration.service;

import javassist.NotFoundException;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.dto.UserPostDto;
import ru.alamics.sso.registration.dto.UserPostRoleDto;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class UserPostService {

    @EJB
    private UserPostRepository userPostRepository;

    public UserPostDto save(UserPostDto userPostDto) throws FoundException {
        if (userPostRepository.getUserPost(userPostDto.getUserId(), userPostDto.getTomsId()) != null) {
            throw new FoundException();
        }
        UserPost userPost = DataMapper.toUserPost(userPostDto);
        return DataMapper.toUserPostDto(userPostRepository.save(userPost));
    }

    public UserPostDto edit(UserPostDto userPostDto) throws NotFoundException {
        UserPost userPost = userPostRepository.getUserPost(userPostDto.getId());
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostDto(userPostRepository.update(DataMapper.toUserPost(userPostDto)));
    }

    public void remove(String id) throws NotFoundException {
        if (userPostRepository.getUserPost(id) == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        userPostRepository.remove(id);
    }

    public UserPostDto get(String id) throws NotFoundException{
        UserPost userPost = userPostRepository.getUserPost(id);
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostDto(userPost);
    }

    public List<UserPostDto> getAll() {
        return DataMapper.toUserPostDtoList(userPostRepository.getAllUserPost());
    }

    public List<UserPostRoleDto> getUserPostRoleDtos(){
        return DataMapper.toUserPostRoleDtoList(userPostRepository.getAllUserPostRoles());
    }
}
