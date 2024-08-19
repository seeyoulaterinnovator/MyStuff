package ru.alamics.sso.antifraud;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.jpa.entity.antifraud.BlackListEntity;
import ru.alamics.sso.jpa.repository.BlackListRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.BlackListMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class BlackListService {
    @Inject
    private BlackListRepository blackListRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private SettingsService settingsService;
    // 43200L 12 часов

    public void limitUserBySmsOrPhone(User user, String cause, AuthenticationSessionModel context) {
        BlackListEntity blackList = new BlackListEntity();
        UserEntity userEntity = userRepository.findUser(user.getId());
        List<BlackListEntity> existEntity = blackListRepository.findByEmail(user.getEmail());
        long blockDuration = settingsService.getSettingsLongValue(SettingConstants.BLOCK_DURATION_SEC, context.getRealm().getName());

        blackList.setId(UUID.randomUUID().toString());
        blackList.setPhone(user.getPhone());
        blackList.setUser(userEntity);
        blackList.setEmail(user.getEmail());
        blackList.setBlockDurationSec(blockDuration);
        blackList.setLimitationCause(cause);
        blackList.setCreatedAt(LocalDateTime.now());
        blackList.setUnblockedAt(blackList.getCreatedAt().plusSeconds(blockDuration));
        blackList.setRealm(context.getRealm().getName());
        //default 0 mb todo default 1 | do we need it?
        if (existEntity.isEmpty()) {
            blackList.setBlockCount(1);
            blackListRepository.save(blackList);
            return;
        }
        blackList.setBlockCount(existEntity.stream().findFirst().get().getBlockCount() + 1);
        blackListRepository.update(blackList);
    }

    public BlackListDto limitUserBySmsOrPhoneV2(User user, String cause, AuthenticationSessionModel context) {
        BlackListEntity blackList = new BlackListEntity();
        UserEntity userEntity = userRepository.findUser(user.getId());
        List<BlackListEntity> existEntity = blackListRepository.findFirstByPhoneAndLimitationCause(user.getPhone(), cause);
        long blockDuration = settingsService.getSettingsLongValue(SettingConstants.BLOCK_DURATION_SEC, context.getRealm().getName());

        blackList.setId(UUID.randomUUID().toString());
        blackList.setPhone(user.getPhone());
        blackList.setUser(userEntity);
        blackList.setEmail(user.getEmail());
        blackList.setBlockDurationSec(blockDuration);
        blackList.setLimitationCause(cause);
        blackList.setCreatedAt(LocalDateTime.now());
        blackList.setUnblockedAt(blackList.getCreatedAt().plusSeconds(blockDuration));
        blackList.setRealm(context.getRealm().getName());
        if (existEntity.isEmpty()) {
            blackList.setBlockCount(1);
            blackListRepository.save(blackList);
            return BlackListMapper.toDto(blackList);
        }
        blackList.setBlockCount(existEntity.stream().findFirst().get().getBlockCount() + 1);
        blackListRepository.update(blackList);
        return BlackListMapper.toDto(blackList);
    }

    public boolean isUserBlockedAuthBySms(String phone, AuthenticationFlowContext context) {

        return blackListRepository.findBlockedByPhone(phone, context).stream()
                .anyMatch(it -> it.getLimitationCause().equals(ActivationCodeType.CODE_TO_SMS.name()));
    }

    public boolean isUserBlockedAuthBySms(String phone, RequiredActionContext context) {
        return blackListRepository.findBlockedByPhone(phone, context).stream()
                .anyMatch(it -> it.getLimitationCause().equals(ActivationCodeType.CODE_TO_SMS.name()));
    }

    public boolean isUserBlockedAuthByPhoneCall(String phone, AuthenticationFlowContext context) {
        return blackListRepository.findBlockedByPhone(phone, context).stream()
                .anyMatch(it -> it.getLimitationCause().equals(ActivationCodeType.CODE_BY_PHONE_NUMBER.name()));
    }

    public boolean isUserBlockedAuthByPhoneCall(String phone, RequiredActionContext context) {
        return blackListRepository.findBlockedByPhone(phone, context).stream()
                .anyMatch(it -> it.getLimitationCause().equals(ActivationCodeType.CODE_BY_PHONE_NUMBER.name()));
    }

    public boolean isUserBlockedAuthByCause(String phone, AuthenticationFlowContext context, String cause) {
        return blackListRepository.findBlockedByPhone(phone, context).stream()
                .anyMatch(it -> cause.equals(it.getLimitationCause()));
    }

    public BlackListDto getBlockedUser(String phone, AuthenticationFlowContext context) {
        List<BlackListEntity> entities = blackListRepository.findBlockedByPhone(phone, context);
        if (!entities.isEmpty()) {
            return BlackListMapper.toDto(entities.stream().findFirst().get());
        }
        return null;
    }

    public BlackListDto getBlockedUserByCause(String phone, AuthenticationFlowContext context, String cause) {
        List<BlackListEntity> entities = blackListRepository.findBlockedByPhone(phone, context).stream()
                .filter(it -> cause.equals(it.getLimitationCause()))
                .collect(Collectors.toList());
        if (!entities.isEmpty()) {
            return BlackListMapper.toDto(entities.stream().findFirst().get());
        }
        return null;
    }

    public BlackListDto getBlockedUser(String phone, RequiredActionContext context) {
        List<BlackListEntity> entities = blackListRepository.findBlockedByPhone(phone, context);
        if (!entities.isEmpty()) {
            return BlackListMapper.toDto(entities.stream().findFirst().get());
        }
        return null;
    }
}
