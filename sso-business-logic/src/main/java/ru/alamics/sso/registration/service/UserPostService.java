package ru.alamics.sso.registration.service;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.jpa.repository.CustomerRepository;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.*;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.util.validator.DmpIdValidator;
import ru.alamics.sso.util.validator.NotValidException;
import ru.alamics.sso.util.validator.TomsIdValidator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
@Slf4j
public class UserPostService {

    @EJB
    private UserPostRepository userPostRepository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private CustomerRepository customerRepository;

    public UserPostResponse save(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserEntity user = userRepository.findUser(userPostRequest.getUserId());
        if (user == null) {
            throw new NotFoundException("УЗ с таким ID не найдена");
        }

        UserPostRoleEntity role = userPostRepository.findUserPostRoleById(userPostRequest.getRoleId());
        if (role == null) {
            throw new NotFoundException("Роль не найдена");
        }

        checkUserPost(userPostRequest);

        UserPostEntity userPost = DataMapper.toUserPost(userPostRequest);
        userPost.setUser(user);
        userPost.setRole(role);
        userPost.setCustomer(customerRepository.save(userPost.getCustomer()));

        return DataMapper.toUserPostResponse(userPostRepository.save(userPost));
    }

    private void checkUserPost(UserPostRequest postRequest) throws FoundUserPostException, NotValidException {
        UserPostEntity post = userPostRepository.findUserPostByUserIdAndTomsId(postRequest.getUserId(), postRequest.getTomsId());
        if (post != null) {
            throw new FoundUserPostException(String.format("УЗ уже имеет должность с таким tomsId: userId=%s, postId=%s, tomsId=%s",
                    postRequest.getUserId(), post.getId(), postRequest.getTomsId()));
        }

        TomsIdValidator.validate(postRequest.getTomsId());

        if (postRequest.getDmpId() != null && !postRequest.getDmpId().isEmpty()) {
            DmpIdValidator.validate(postRequest.getDmpId());
        }
    }

    public UserPostResponse edit(UserPostEditRequest userPostEditRequest) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(userPostEditRequest.getId());
        if (userPost == null) {
            throw new NotFoundException("Должность не найдена");
        }

        UserPostRoleEntity role = userPostRepository.findUserPostRoleById(userPostEditRequest.getRoleId());
        if (role == null) {
            throw new NotFoundException("Роль не найдена");
        }

        userPost.setRole(role);

        return DataMapper.toUserPostResponse(userPostRepository.update(userPost));
    }

    public void remove(String id) throws NotFoundException {
        if (userPostRepository.getUserPost(id) == null) {
            throw new NotFoundException("Должность с таким ID не найдена");
        }
        userPostRepository.remove(id);
    }

    public UserPostResponse get(String id) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(id);
        if (userPost == null) {
            throw new NotFoundException("Должность с таким ID не найдена");
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
            throw new NotFoundException("Должность не найдена");
        }
        // TODO практически константа
        ExternalSystemRoleEntity externalSystemRole = userPostRepository.findExternalSystemRole(externalSystemRoleRequest.getSystemRoleId());
        if (externalSystemRole == null) {
            throw new NotFoundException("Доступ в систему не найден");
        }

        Set<ExternalSystemRoleEntity> systemRoles = userPost.getSystemRoles();
        if (systemRoles == null) {
            systemRoles = new HashSet<>();
        }

        systemRoles.add(externalSystemRole);
        userPost.setSystemRoles(systemRoles);
        return DataMapper.toUserPostResponse(userPostRepository.update(userPost));
    }

    public UserPostResponse removeSystemRole(ExternalSystemRoleRequest externalSystemRoleRequest) throws NotFoundException {
        UserPostEntity userPost = userPostRepository.getUserPost(externalSystemRoleRequest.getUserPostId());
        if (userPost == null) {
            throw new NotFoundException("Должность не найдена");
        }
        if (userPost.getSystemRoles() == null || userPost.getSystemRoles().isEmpty() ||
                !userPost.getSystemRoles().stream().anyMatch(o ->
                        o.getId().equals(externalSystemRoleRequest.getSystemRoleId()))) {
            throw new NotFoundException("Роль не найдена в этой должности");
        }
        userPost.getSystemRoles().remove(userPost.getSystemRoles().stream()
                .filter(o -> o.getId().equals(externalSystemRoleRequest.getSystemRoleId()))
                .findFirst().get());
        return DataMapper.toUserPostResponse(userPostRepository.update(userPost));
    }

    public Long getUserPostRole(String name) throws NotFoundException {
        UserPostRoleEntity userPostRole = userPostRepository.getUserPostRole(name);
        if (userPostRole == null) {
            throw new NotFoundException("Роль не найдена");
        }
        return userPostRepository.getUserPostRole(name).getId();
    }

    public Long getExternalSystemRoleId(String sysName) throws NotFoundException {
        ExternalSystemRoleEntity externalSystemRole = userPostRepository.getExternalSystemRole(sysName);
        if (externalSystemRole == null) {
            throw new NotFoundException("Роль клиента не найдена");
        }
        return externalSystemRole.getId();
    }

    public UserPostResponse addUserPostAndSystemRole(UserPostRequest userPostRequest) throws NotFoundException, FoundUserPostException, NotValidException {
        UserPostResponse userPost = save(userPostRequest);
        for (ExternalSystemRoleDto systemRoleDto : getExternalSystemRoles()) {
            ExternalSystemRoleRequest systemRole = new ExternalSystemRoleRequest();
            systemRole.setUserPostId(userPost.getId());
            systemRole.setSystemRoleId(systemRoleDto.getId());
            userPost = addSystemRole(systemRole);
        }
        return userPost;
    }
}
