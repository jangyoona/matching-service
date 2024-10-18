package com.boyug.service;

import com.boyug.dto.UserDto;

public interface SangDamService {

    void requestAdviceCall(UserDto userId, String content);
}
