package ru.alamics.sso.registration.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.jpa.model.UserSummaryView;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.UserSearch;
import ru.alamics.sso.user.web.UserSearchDto;
import ru.alamics.sso.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class UserFindService {
    @Inject
    UserRepository userRepository;

    @Inject
    UserPostRepository userPostRepository;

    public UserEntity getUserByPhone(RealmModel realm, String phone) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(realm, phone, null);
        }
        return null;
    }

    public UserEntity getUserByPhoneAndExcludedUserId(RealmModel realm, String phone, String excludedUserId) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(realm, phone, excludedUserId);
        }
        return null;
    }

    public UserEntity getUserByPhoneAndExcludedUserId(String realmId, String phone, String excludedUserId) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(realmId, phone, excludedUserId);
        }
        return null;
    }

    public List<UserSearchDto> getUsersByParametersWithoutGrouping(
            String realm,
            String search,
            String searchUser,
            String searchToms,
            String sortField,
            boolean sortAsc,
            int pageNum,
            int pageSize,
            List<String> includeOnlyIDs
    ) {
        return UserMapper.toUserDtoList(userRepository.getTupleUsersByParametersWithoutGrouping(realm, search, searchUser, searchToms, sortField, sortAsc, pageNum, pageSize, includeOnlyIDs));
    }

    public List<UserSearch> getUsersByParameters(
            String realm,
            String search,
            String searchUser,
            String searchEmail,
            String searchToms,
            String searchPhone,
            String sortField,
            boolean sortAsc,
            Integer first,
            Integer max
    ) {
        List<UserSummaryView> users = userRepository.findUsersByParameters(realm, search, searchUser, searchEmail, searchPhone, searchToms, sortField, sortAsc, first, max);

        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("getUsersByParameters 1");

        Map<String, List<UserPostResponse>> userPosts = userPostRepository
                .findUserPostsByUserIds(
                        users.stream()
                                .map(UserSummaryView::getId)
                                .collect(Collectors.toList()))
                .stream()
                .map(DataMapper::toUserPostResponse)
                .collect(Collectors.groupingBy(UserPostResponse::getUserId));

        log.info("getUsersByParameters 2");

        List<UserSearch> userSearches = UserMapper.toUserSearchList(users);
        userSearches.forEach(user -> user.setUserPosts(userPosts.get(user.getId())));

        log.info("getUsersByParameters 3");

        return userSearches;
    }


    public long getTotalUsersByParameters(String realm, String search, String searchUser, String searchToms) {
        return userRepository.getTotalUsersByParameters(realm, search, searchUser, searchToms);
    }

    public UserEntity getUserEntity(String userId) {
        return userRepository.findUser(userId);
    }

    public UserPostRoleEntity getRoleEntity(Long id) {
        return userPostRepository.findUserPostRoleById(id);
    }

    public UserEntity getFirstUserByEmail(String realmId, String email) {
        return userRepository.getFirstUserByEmail(realmId, email);
    }
}
