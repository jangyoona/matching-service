package com.boyug.dto;

import com.boyug.entity.BoyugProgramDetailEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoyugProgramDetailDto {

    private int boyugProgramDetailId;

    private LocalDate boyugProgramDetailDate;

    private LocalTime boyugProgramDetailStartTime;

    private LocalTime boyugProgramDetailEndTime;

    private int boyugProgramDetailChild;

    private int programDetailPerson;

    private boolean boyugProgramDetailActive;

    private boolean boyugProgramDetailIsOpen;

    private int boyugProgramDetailRequest;

    private int boyugProgramId;

    private int sessionId;
    private String sessionName;

    private Boolean applied;

    private int boyugToUserOkCount;
    private int userToBoyugOkCount;

    // 추가된 프로그램 DTO
    private BoyugProgramDto boyugProgram;

    private List<UserToBoyugDto> userToBoyugs;

    private List<BoyugToUserDto> boyugToUsers;

    // DTO를 엔티티로 변환
    public BoyugProgramDetailEntity toEntity() {
        return BoyugProgramDetailEntity.builder()
//                .boyugProgramDetailId(boyugProgramDetailId)
                .boyugProgramDetailDate(boyugProgramDetailDate)
                .boyugProgramDetailStartTime(boyugProgramDetailStartTime)
                .boyugProgramDetailEndTime(boyugProgramDetailEndTime)
                .boyugProgramDetailChild(boyugProgramDetailChild)
                .programDetailPerson(programDetailPerson)
//                .boyugProgramDetailActive(boyugProgramDetailActive)
                .boyugProgramDetailIsOpen(boyugProgramDetailIsOpen)
//                .session(sessionId)
//                .boyugProgramId(boyugProgramId)
//                .boyugProgramDetailRequest(boyugProgramDetailRequest)
                .build();
    }

    // 엔티티를 DTO로 변환
    public static BoyugProgramDetailDto of(BoyugProgramDetailEntity entity) {
        return BoyugProgramDetailDto.builder()
                .boyugProgramDetailId(entity.getBoyugProgramDetailId())
                .boyugProgramDetailDate(entity.getBoyugProgramDetailDate())
                .boyugProgramDetailStartTime(entity.getBoyugProgramDetailStartTime())
                .boyugProgramDetailEndTime(entity.getBoyugProgramDetailEndTime())
                .boyugProgramDetailChild(entity.getBoyugProgramDetailChild())
                .programDetailPerson(entity.getProgramDetailPerson())
                .boyugProgramDetailActive(entity.isBoyugProgramDetailActive())
                .boyugProgramDetailIsOpen(entity.isBoyugProgramDetailIsOpen())
                .boyugProgramDetailRequest(entity.getBoyugProgramDetailRequest())
                .boyugProgramId(entity.getBoyugProgram().getBoyugProgramId())
                .sessionName(entity.getSession().getSessionName())
                .sessionId(entity.getSession().getSessionId())
                // 프로그램 DTO 포함
                .boyugProgram(BoyugProgramDto.of(entity.getBoyugProgram()))
//                .userToBoyug(UserToBoyugDto.of(entity.getUserToBoyugs()))
                .build();
    }

}
