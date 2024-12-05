package com.boyug.oauth2;

import com.boyug.dto.ProfileImageDto;
import com.boyug.dto.RoleDto;
import com.boyug.dto.UserDto;
import com.boyug.entity.RoleEntity;
import com.boyug.entity.UserEntity;
import com.boyug.repository.AccountRepository;
import com.boyug.security.LoginSuccessHandler;
import com.boyug.security.WebUserDetails;
import com.boyug.security.jwt.JwtInfoDto;
import com.boyug.security.jwt.JwtUtil;
import com.boyug.service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService { // implements OAuth2UserService<OAuth2AuthenticationToken, OAuth2User>

    // https://jngsngjn.tistory.com/16
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    // OAuth2 인증 요청을 받아 사용자 정보를 불러오는 메서드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 인가코드 -> 엑세스 토큰
        // userRequest.getAccessToken().getTokenValue();

        // 상위 클래스에 위임하여 사용자 정보를 불러온다.
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("서비스 oAuth2User?= " + oAuth2User.getAttributes());

        // provider 식별
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAut2Response.OAuth2Response oAuth2Response = null;

        // provider에 따라 OAuth2Response의 구현체를 OAuth2Response에 저장한다.
        if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // Provider: kakao | ProvideId: 고유한 ID
        String userId = oAuth2Response.getProviderId();

        // 기존 사용자 조회
        Optional<UserEntity> existData = accountRepository.findLoginUserBySocialId(userId);
        UserDto userDto = existData.isPresent() ? UserDto.of(existData.get()) : new UserDto();

        // 사용자가 없으면 새로운 사용자 저장
        if (existData.isEmpty()) {

            // 이미지 추출
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            String profileImageUrl = (String) properties.get("profile_image");

            // 회원가입 일자 (연결시간)
            String connectedAtString = (String) attributes.get("connected_at");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date regiDate = null;
            try {
                regiDate = sdf.parse(connectedAtString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            List<ProfileImageDto> profileImageList = new ArrayList<>();
            ProfileImageDto profileImage = new ProfileImageDto();
            profileImage.setImgOriginName(profileImageUrl);
            profileImage.setImgSavedName(profileImageUrl);
            profileImageList.add(profileImage);

            userDto.setUserPw("kakaoUser!");
            userDto.setSocialId(userId);
            userDto.setUserCategory(3);
            userDto.setUserType("ROLE_USER");
            userDto.setUserRegDate(regiDate);
            userDto.setImages(profileImageList);

            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest().getSession();
            session.setAttribute("oAuth2User", userDto);

            // 회원가입 페이지로 리디렉션을 유발하기 위해 예외 발생
            throw new OAuth2AuthenticationException(new OAuth2Error("회원가입 필요"), "회원가입 필요");
        } else {
            // 이미 회원가입된 소셜 회원이라면
            // 정보 업데이트 필요시 구현
            Set<RoleEntity> roles = existData.get().getRoles();

            /**
             * JWT 전환으로 추가
             **/
            WebUserDetails userDetails = new WebUserDetails(UserDto.of(existData.get()), roles.stream().map(RoleDto::of).toList());

            // 토큰 발급 + Cookie 저장
            AddAuthenticationJwtToken(userDetails);

            // 인증 정보 저장
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            /**
             *
             **/

            return new CustomOAuth2User(oAuth2Response, "ROLE_USER", userId, userDto, roles.stream().map(RoleDto::of).toList());
        }
    }


    /**
    * loginSuccessHandler 중 토큰 발급 메서드
    **/
    private void AddAuthenticationJwtToken(WebUserDetails userDetails) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        // jwt
        JwtInfoDto token = jwtUtil.createToken(userDetails, null);

        // 토큰 cookie 저장
        String encodedToken = null;
        String encodedToken_Refresh = null;
        try {
            encodedToken = URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8.toString());
            encodedToken_Refresh = URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            System.out.println("토큰 인코딩 오류");
            throw new RuntimeException(e);
        }

        // Token 저장
        Cookie accseeCookie = new Cookie("Authorization", encodedToken);
        accseeCookie.setHttpOnly(true);
        accseeCookie.setPath("/");
        accseeCookie.setMaxAge(60 * 30); // 30분
        response.addCookie(accseeCookie);

        // RefreshToken 저장
        Cookie refreshCookie = new Cookie("RefreshToken", encodedToken_Refresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
        response.addCookie(refreshCookie);

        redisService.addJwtRefreshToken(userDetails.getUser().getUserId(), encodedToken_Refresh);
    }


//            Pair<Set<RoleDto>, List<RoleDto>> rolesData = getRoleDto("ROLE_USER");
//            Set<RoleDto> userRolesSet = rolesData.getLeft();  // Set 가져오기
//            userRolesList = rolesData.getRight(); // List 가져오기


//    public Pair<Set<RoleDto>, List<RoleDto>> getRoleDto(String role) {
//        // Set 생성
//        Set<RoleDto> roleSet = new HashSet<>();
//        RoleDto roleDto = new RoleDto();
//        roleDto.setRoleName(role);
//        roleDto.setRoleDesc(role);
//        roleSet.add(roleDto);
//
//        // List 생성
//        List<RoleDto> roleList = new ArrayList<>(roleSet);
//
//        // Set과 List 반환
//        return Pair.of(roleSet, roleList);
//    }
}