package ru.alamics.sso.registration.service;

import javassist.NotFoundException;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.*;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class UserPostService {

    @EJB
    private UserPostRepository userPostRepository;

    public UserPostResponse save(UserPostRequest userPostRequest) throws FoundUserPostException {
        if (userPostRepository.getUserPost(userPostRequest.getUserId(), userPostRequest.getTomsId()) != null) {
            throw new FoundUserPostException();
        }
        UserPost userPost = DataMapper.toUserPost(new UserPost(), userPostRequest);
        return DataMapper.toUserPostResponse(userPostRepository.save(userPost));
    }

    public UserPostResponse edit(UserPostRequest userPostRequest) throws NotFoundException {
        UserPost userPost = userPostRepository.getUserPost(userPostRequest.getId());
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostResponse(userPostRepository.update(DataMapper.toUserPost(userPost, userPostRequest)));
    }

    public void remove(String id) throws NotFoundException {
        if (userPostRepository.getUserPost(id) == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        userPostRepository.remove(id);
    }

    public UserPostResponse get(String id) throws NotFoundException{
        UserPost userPost = userPostRepository.getUserPost(id);
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostResponse(userPost);
    }

    public List<UserPostResponse> getAll() {
        return DataMapper.toUserPostResponseList(userPostRepository.getAllUserPost());
    }

    public List<UserPostRoleDto> getUserPostRoleDtos(){
        return DataMapper.toUserPostRoleDtoList(userPostRepository.getAllUserPostRoles());
    }

    public List<ExternalSystemRoleDto> getExternalSystemRoles(){
        return DataMapper.toExternalSystemRoleDtos(userPostRepository.getAllExternalSystemRole());
    }

    public List<ExternalSystemDto> getExternalSystems(){
        return DataMapper.toExternalSystemDtos(userPostRepository.getAllExternalSystem());
    }

    public UserPostResponse addSystemRole(String userPostId, Long extSystemRoleId) throws NotFoundException {
        UserPost userPost = userPostRepository.getUserPost(userPostId);
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostResponse(userPostRepository.addSystemRole(userPost, extSystemRoleId));
    }

    public UserPostResponse removeSystemRole(String userPostId, Long extSystemRoleId) throws NotFoundException {
        UserPost userPost = userPostRepository.getUserPost(userPostId);
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        if (userPost.getSystemRoles() == null || userPost.getSystemRoles().isEmpty() ||
                !userPost.getSystemRoles().stream().anyMatch(o ->
                        o.getId().equals(extSystemRoleId))) {
            throw new NotFoundException("SystemRole is not exist in this UserPost");
        }
        return DataMapper.toUserPostResponse(userPostRepository.removeSystemRole(userPost, extSystemRoleId));
    }
}
