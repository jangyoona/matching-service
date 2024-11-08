package com.boyug.service;

import com.boyug.dto.BoyugProgramDto;
import com.boyug.dto.ProfileImageDto;
import com.boyug.dto.UserDto;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

public class HomeServiceImpl implements HomeService {

    @Setter
    AccountService accountService;

    @Setter
    ActivityService activityService;


    /*
    * 메인 최신 공고글 20개 노출
    * */
    @Override
    public Page<BoyugProgramDto> getLatestBoyugProgramList() {

        Page<BoyugProgramDto> boyugProgramList = activityService.findAllList(0, 20);

        for (BoyugProgramDto program : boyugProgramList) {
            UserDto user = accountService.getUserInfo(program.getUserId());
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
        List<ProfileImageDto> profile = accountService.getUserProfileImage(user);

        if (profile.isEmpty()) {
            ProfileImageDto basicImage = new ProfileImageDto();
            basicImage.setImgSavedName("no_img.jpg");
            profile.add(basicImage);
            user.setImages(profile);
        } else {
            user.setImages(profile);
        }
        program.setUser(user);
    }

}
