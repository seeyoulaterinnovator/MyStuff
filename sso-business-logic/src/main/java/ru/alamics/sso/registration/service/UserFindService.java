package ru.alamics.sso.registration.service;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserPostRepository;
import ru.alamics.sso.keycloak.repository.UserRepository;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.UserSearch;
import ru.alamics.sso.user.web.UserSearchDto;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class UserFindService {
    @EJB
    private UserRepository userRepository;
    @EJB
    private UserPostRepository userPostRepository;

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

    public UserEntity getUserByPhoneAndExcludedUserId(String phone, String excludedUserId) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(phone, excludedUserId);
        }
        return null;
    }

    public List<UserSearchDto> getUsersByParametersWithoutGrouping(String realm, String search, String searchUser, String searchToms, String sortField, boolean sortAsc) {
        return UserMapper.toUserDtoList(userRepository.getTupleUsersByParametersWithoutGrouping(realm, search, searchUser, searchToms, sortField, sortAsc));
    }

    public List<UserSearch> getUsersByParameters(String realm, String search, String searchUser, String searchToms, String sortField, boolean sortAsc,
                                                 Integer pageNum, Integer pageSize) {
        List<UserSearch> userSearches = UserMapper.toUserSearchList(userRepository.getTupleUsersByParameters(realm, search, searchUser, searchToms, sortField, sortAsc, pageNum, pageSize));
        if (userSearches.isEmpty()) {
            return userSearches;
        }
        
        Map<String, List<UserPostResponse>> userPostEntities = userPostRepository
                .findUserPostsByIds(
                        userSearches.stream()
                                .map(UserSearch::getUserPosts)
                                .flatMap(Collection::stream)
                                .map(UserPostResponse::getId)
                                .collect(Collectors.toList()))
                .stream()
                .map(DataMapper::toUserPostResponse)
                .collect(Collectors.groupingBy(UserPostResponse::getUserId));

        userSearches.forEach(user -> user.setUserPosts(userPostEntities.get(user.getId())));
        return userSearches;
    }


    public long getTotalUsersByParameters(String realm, String search, String searchUser, String searchToms) {
        return userRepository.getTotalUsersByParameters(realm, search, searchUser, searchToms);
    }

    public UserEntity getUserEntity(String userId) {
        return userRepository.findUser(userId);
    }
}
