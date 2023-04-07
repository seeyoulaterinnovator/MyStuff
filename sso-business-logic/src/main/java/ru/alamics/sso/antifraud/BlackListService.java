package ru.alamics.sso.antifraud;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.antifraud.BlackListEntity;
import ru.alamics.sso.jpa.repository.BlackListRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.jpa.util.LimitationCauseType;
import ru.alamics.sso.registration.model.User;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class BlackListService {
    @EJB
    private BlackListRepository blackListRepository;
    @EJB
    private UserRepository userRepository;
    private final Long BLOCK_DURATION_SEC = 43200L; // 12 часов

    public void limitUserBySmsOrPhone(User user, LimitationCauseType cause) {
        BlackListEntity blackList = new BlackListEntity();
        UserEntity userEntity = userRepository.findUser(user.getId());

        blackList.setPhone(user.getPhone());
        blackList.setUser(userEntity);
        //default 0
        blackList.setBlockCount(blackList.getBlockCount() + 1);
        blackList.setUserLogin(user.getName());
        blackList.setBlockDurationSec(BLOCK_DURATION_SEC);
        blackList.setLimitationCause(cause);
        blackListRepository.save(blackList);
    }

    public boolean isUserBlockedAuthBySms(String phone) {
        return blackListRepository.findByPhone(phone).stream().anyMatch(it -> it.getLimitationCause().equals(LimitationCauseType.SMS));
    }
    public boolean isUserBlockedAuthByPhoneCall(String phone) {
        return blackListRepository.findByPhone(phone).stream().anyMatch(it -> it.getLimitationCause().equals(LimitationCauseType.PHONE_CALL));
    }
}
