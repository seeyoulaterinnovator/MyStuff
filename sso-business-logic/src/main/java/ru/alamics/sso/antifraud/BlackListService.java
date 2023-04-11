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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        List<BlackListEntity> existEntity = blackListRepository.findByEmail(user.getEmail());

        blackList.setId(UUID.randomUUID().toString());
        blackList.setPhone(user.getPhone());
        blackList.setUser(userEntity);
        blackList.setEmail(user.getEmail());
        blackList.setBlockDurationSec(BLOCK_DURATION_SEC);
        blackList.setLimitationCause(cause.getCause());
        blackList.setCreatedAt(LocalDateTime.now());
        blackList.setUnblockedAt(blackList.getCreatedAt().plusSeconds(BLOCK_DURATION_SEC));
        //default 0 mb todo default 1 | do we need it?
        if (existEntity.isEmpty()) {
            blackList.setBlockCount(1);
        }
        else {
            blackList.setBlockCount(existEntity.stream().findFirst().get().getBlockCount() + 1);
        }
        blackListRepository.save(blackList);
    }

    public boolean isUserBlockedAuthBySms(String phone) {
        return blackListRepository.findBlockedByPhone(phone).stream()
                .anyMatch(it -> it.getLimitationCause().equals(LimitationCauseType.SMS.getCause()));
    }
    public boolean isUserBlockedAuthByPhoneCall(String phone) {
        return blackListRepository.findBlockedByPhone(phone).stream()
                .anyMatch(it -> it.getLimitationCause().equals(LimitationCauseType.PHONE_CALL.getCause()));
    }
}
