package ru.alamics.sso.util;

import ru.alamics.sso.antifraud.BlackListDto;
import ru.alamics.sso.jpa.entity.antifraud.BlackListEntity;

public class BlackListMapper {
    public static BlackListDto toDto(BlackListEntity entity) {
        BlackListDto blackListDto = new BlackListDto();
        blackListDto.setEmail(entity.getEmail());
        blackListDto.setBlockDurationSec(entity.getBlockDurationSec());
        blackListDto.setCreatedAt(entity.getCreatedAt());
        blackListDto.setPhone(entity.getPhone());
        blackListDto.setLimitationCause(entity.getLimitationCause());
        blackListDto.setUser(entity.getUser());
        blackListDto.setUnblockedAt(entity.getUnblockedAt());
        return blackListDto;
    }
    public static BlackListEntity toEntity(BlackListDto dto) {
        BlackListEntity entity = new BlackListEntity();
        entity.setUnblockedAt(dto.getUnblockedAt());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setBlockDurationSec(dto.getBlockDurationSec());
        entity.setUser(dto.getUser());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setLimitationCause(dto.getLimitationCause());
        return entity;
    }
}
