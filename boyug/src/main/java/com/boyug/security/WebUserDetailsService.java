package com.boyug.security;

import com.boyug.dto.RoleDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.RoleEntity;
import com.boyug.entity.UserEntity;
import com.boyug.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

public class WebUserDetailsService implements UserDetailsService {

    @Setter(onMethod_ = { @Autowired })
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userPhone) throws UsernameNotFoundException {

        // 데이터베이스에서 데이터 조회
        WebUserDetails userDetails = null;

        Optional<UserEntity> result = accountRepository.findByUserPhone(userPhone); // 이 방식에선 유저Id만 가져오고, Id를 가져온 후 Pw를 체크한다.

        if (result.isEmpty()) {
            throw new UsernameNotFoundException("isUser" + userPhone);
        } else {
//            List<RoleDto> roles = accountRepository.selectRolesById(username);
            UserEntity userEntity = result.get();
            Set<RoleEntity> roles = userEntity.getRoles();

            userDetails = new WebUserDetails(UserDto.of(userEntity), roles.stream().map(RoleDto::of).toList());
        }

        return userDetails;
    }

    public class UserNotConfirmedException extends UsernameNotFoundException {
        public UserNotConfirmedException(String msg) {
            super(msg);
        }
    }
}
