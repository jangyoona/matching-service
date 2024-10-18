package com.boyug.service;

import com.boyug.dto.UserDto;
import com.boyug.entity.SangdamEntity;
import com.boyug.repository.SangDamRepository;
import lombok.Setter;

public class SangDamServiceImpl implements SangDamService {

    @Setter
    SangDamRepository sangDamRepository;

    @Override
    public void requestAdviceCall(UserDto user, String content) {
        SangdamEntity entity = new SangdamEntity();
        entity.setUser(user.toEntity());
        entity.setSangdamContent(content);
        sangDamRepository.save(entity);
    }
}
