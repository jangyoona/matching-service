package com.boyug.service;

import com.boyug.dto.BoyugProgramDto;
import com.boyug.dto.ProfileImageDto;
import com.boyug.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final AccountService accountService;
    private final ActivityService activityService;


    /*
    * 메인 최신 공고글 20개 노출
    * */
//    @Cacheable("recentProgramList")
    @Override
    public Page<BoyugProgramDto> getLatestBoyugProgramList() {

        Page<BoyugProgramDto> boyugProgramList = activityService.findAllList(0, 20);

        for (BoyugProgramDto program : boyugProgramList) {
            UserDto user = accountService.getUserInfoWithProfileImage(program.getUserId());
            // 화면 출력용 주소 자르기
            String[] addrCut = user.getUserAddr2().split("\\s+");
            String addrSplit = String.format("%s %s", addrCut[0], addrCut[1]);
            user.setUserAddr2(addrSplit);

            setUserProfileImage(user ,program);
        }
        return boyugProgramList;
    }

    /*
    * 등록된 프로필 이미지가 없을 경우 기본 이미지 세팅
    * */
    private void setUserProfileImage(UserDto user, BoyugProgramDto program) {
        if (user == null || user.getImages().isEmpty()) {
            ProfileImageDto basicImage = new ProfileImageDto();
            List<ProfileImageDto> list = new ArrayList<>();
            basicImage.setImgSavedName("no_img.jpg");
            list.add(basicImage);
            user.setImages(list);
        }
        program.setUser(user);
    }

}
