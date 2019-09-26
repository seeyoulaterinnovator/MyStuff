package ru.alamics.sso.registration.service;

import javassist.NotFoundException;
import ru.alamics.sso.keycloak.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.keycloak.entity.UserPostEntity;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.keycloak.repository.UserRepository;
import ru.alamics.sso.registration.dto.*;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class UserPostService {

    @EJB
    private UserPostRepository userPostRepository;
    @EJB
    private UserRepository userRepository;

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException {
        if (userRepository.findUser(userPostRequest.getUserId()) == null) {
            throw new NotFoundException("User with this userId is not exist!");
        } else if (userPostRepository.findUserPostRole(userPostRequest.getRoleId()) == null) {
            throw new NotFoundException("UserPostRole with this roleId is not exist!");
        }
        UserPostEntity userPost = DataMapper.toUserPost(new UserPostEntity(), userPostRequest);
        return DataMapper.toUserPostResponse(userPostRepository.save(userPost));
    }

    public UserPostResponse edit(UserPostEditRequest userPostEditRequest) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(userPostEditRequest.getId());
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        } else if (userPostRepository.findUserPostRole(userPostEditRequest.getRoleId()) == null) {
            throw new NotFoundException("UserPostRole with this roleId is not exist!");
        }
        return DataMapper.toUserPostResponse(userPostRepository.update(DataMapper.toUserPost(userPost, userPostEditRequest)));
    }

    public void remove(String id) throws NotFoundException {
        if (userPostRepository.getUserPost(id) == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        userPostRepository.remove(id);
    }

    public UserPostResponse get(String id) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(id);
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        return DataMapper.toUserPostResponse(userPost);
    }

    public List<UserPostResponse> getAll() {
        return DataMapper.toUserPostResponseList(userPostRepository.getAllUserPost());
    }

    public List<UserPostResponse> getUserPost(String userId) throws NotFoundException {
        return DataMapper.toUserPostResponseList(userPostRepository.findUserPostRoleByUserId(userId));
    }

    public List<UserPostRoleDto> getUserPostRoleDtos() {
        return DataMapper.toUserPostRoleDtoList(userPostRepository.getAllUserPostRoles());
    }

    public List<ExternalSystemRoleDto> getExternalSystemRoles() {
        return DataMapper.toExternalSystemRoleDtos(userPostRepository.getAllExternalSystemRole());
    }

    public List<ExternalSystemDto> getExternalSystems() {
        return DataMapper.toExternalSystemDtos(userPostRepository.getAllExternalSystem());
    }

    public UserPostResponse addSystemRole(ExternalSystemRoleRequest externalSystemRoleRequest) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(externalSystemRoleRequest.getUserPostId());
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        ExternalSystemRoleEntity externalSystemRole = userPostRepository.findExternalSystemRole(externalSystemRoleRequest.getSystemRoleId());
        if (externalSystemRole == null) {
            throw new NotFoundException("SystemRole with this systemRoleId is not exist!");
        }
        userPost.getSystemRoles().add(externalSystemRole);
        return DataMapper.toUserPostResponse(userPostRepository.update(userPost));
    }

    public UserPostResponse removeSystemRole(ExternalSystemRoleRequest externalSystemRoleRequest) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(externalSystemRoleRequest.getUserPostId());
        if (userPost == null) {
            throw new NotFoundException("UserPost is not exist");
        }
        if (userPost.getSystemRoles() == null || userPost.getSystemRoles().isEmpty() ||
                !userPost.getSystemRoles().stream().anyMatch(o ->
                        o.getId().equals(externalSystemRoleRequest.getSystemRoleId()))) {
            throw new NotFoundException("SystemRole is not exist in this UserPost");
        }
        userPost.getSystemRoles().remove(userPost.getSystemRoles().stream()
                .filter(o -> o.getId().equals(externalSystemRoleRequest.getSystemRoleId()))
                .findFirst().get());
        return DataMapper.toUserPostResponse(userPostRepository.update(userPost));
    }
}
