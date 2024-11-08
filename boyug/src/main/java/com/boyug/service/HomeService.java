package com.boyug.service;

import com.boyug.dto.BoyugProgramDetailDto;
import com.boyug.dto.BoyugProgramDto;
import org.springframework.data.domain.Page;

public interface HomeService {


    Page<BoyugProgramDto> getLatestBoyugProgramList();
}
