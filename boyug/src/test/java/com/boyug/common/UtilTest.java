package com.boyug.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

//@ActiveProfiles("live")
@SpringBootTest
class UtilTest {

    @Test
    public void getHashedDataTest() {
        byte[] result = Util.getHashedData("sinahjang", "SHA-256");
        System.out.println("result : " + result);
    }

}