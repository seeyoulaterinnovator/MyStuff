package ru.alamics.sso.registration.service;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.repository.MessengerRepository;
import ru.alamics.sso.registration.dto.MessengerDto;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
@Slf4j
public class MessengerService {

    @EJB
    private MessengerRepository messengerRepository;

    public List<MessengerDto> getAllMessengers() {
        return DataMapper.toMessengerDtos(messengerRepository.getAllMessenger());
    }

}
