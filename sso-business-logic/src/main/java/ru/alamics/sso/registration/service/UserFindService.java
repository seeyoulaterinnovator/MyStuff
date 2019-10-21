package ru.alamics.sso.registration.service;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserRepository;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.UserSearchDto;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class UserFindService {
    private final static String SORT_FIELD_NAME = "firstName";
    private final static String SORT_FIELD_EMAIL = "email";

    @EJB
    private UserRepository userRepository;

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

    public List<UserSearchDto> getUsersByParameters(String realm, String search, String searchUser, String searchToms, String sortField, boolean sortAsc) {
        return UserMapper.toUserDtoList(userRepository.getTupleUsersByParameters(realm, search, searchUser, searchToms, sortField, sortAsc, getSort(sortField, sortAsc)));
    }

    private String getSort(String sortField, boolean sortAsc) {
        String sort = "";
        if (SORT_FIELD_NAME.equalsIgnoreCase(sortField)) {
            sort += "ORDER BY first_name";
        } else if (SORT_FIELD_EMAIL.equalsIgnoreCase(sortField)) {
            sort += "ORDER BY email";
        }
        if (sort.isBlank()) {
            return sort;
        }
        if (!sortAsc) {
            sort += " DESC";
        }
        return sort;
    }
}
