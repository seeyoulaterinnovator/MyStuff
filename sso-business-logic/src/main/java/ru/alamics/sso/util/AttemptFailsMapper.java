package ru.alamics.sso.util;

import ru.alamics.sso.antifraud.AttemptFailsDto;
import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class AttemptFailsMapper {
    public static List<AttemptFailsDto> toDtoList(List<AttemptFailsEntity> entity) {
        List<AttemptFailsDto> attemptFailsDto = new LinkedList<>();
        if (!entity.isEmpty()) {
            entity.forEach(it -> attemptFailsDto.add(new AttemptFailsDto(it.getPhone(), it.getCode(), it.getRealm(), it.getLimitationCause(),it.getCreated(), it.getUserId())));
        }
        return attemptFailsDto;
    }

    public static AttemptFailsEntity toEntity(AttemptFailsDto dto) {
        AttemptFailsEntity entity = new AttemptFailsEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setPhone(dto.getPhone());
        entity.setCode(dto.getCode());
        entity.setRealm(dto.getRealm());
        entity.setLimitationCause(dto.getLimitationCause());
        entity.setCreated(dto.getCreated());
        entity.setUserId(dto.getUserId());
        return entity;
    }

    public static List<AttemptFailsEntity> toEntityList(List<AttemptFailsDto> dtoList) {
        List<AttemptFailsEntity> entities = new LinkedList<>();
        if (!dtoList.isEmpty()) {
            dtoList.forEach(it -> entities.add(new AttemptFailsEntity(UUID.randomUUID().toString(), it.getPhone(), it.getCode(), it.getRealm(), it.getLimitationCause(), it.getUserId(), it.getCreated())));
        }
        return entities;
    }
}
