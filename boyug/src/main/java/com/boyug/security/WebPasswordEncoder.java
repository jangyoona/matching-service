package com.boyug.security;

import com.boyug.common.Util;
import org.springframework.security.crypto.password.PasswordEncoder;

public class WebPasswordEncoder implements PasswordEncoder {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public String encode(CharSequence rawPassword) {
        return Util.getHashedString(rawPassword.toString(), ALGORITHM);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) { // 비밀번호 맞는지 확인 메서드
        String hashedPassword = Util.getHashedString(rawPassword.toString(), ALGORITHM);

        return hashedPassword.equals(encodedPassword);
    }
}
