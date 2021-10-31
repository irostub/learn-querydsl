package com.irostub.learnquerydsl.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberTest {

    @Test
    @DisplayName("회원 영속 테스트")
    void memberPersistenceTest() {
        Member member = new Member();
    }
}