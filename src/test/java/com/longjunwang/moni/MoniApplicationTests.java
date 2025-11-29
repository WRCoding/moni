package com.longjunwang.moni;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class MoniApplicationTests {

    @Test
    void contextLoads() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        LocalDate lastDay = LocalDate.now().minusDays(1);
        System.out.println(lastWeek.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        System.out.println(lastDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

}
