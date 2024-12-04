package ru.alamics.sso.registration.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.repository.MessengerRepository;
import ru.alamics.sso.registration.dto.MessengerDto;
import ru.alamics.sso.registration.mapper.DataMapper;

import java.util.List;

@ApplicationScoped
@Slf4j
public class MessengerService {
    @Inject
    MessengerRepository messengerRepository;

    public List<MessengerDto> getAllMessengers() {
        return DataMapper.toMessengerDtos(messengerRepository.getAllMessenger());
    }
}
