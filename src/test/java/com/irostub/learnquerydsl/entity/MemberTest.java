package com.irostub.learnquerydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.irostub.learnquerydsl.entity.QMember.member;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @BeforeEach
    void setUp() {
        query = new JPAQueryFactory(em);
    }

    @Test
    @DisplayName("회원 팀 영속 테스트")
    @Transactional
    void memberTeamPersistenceTest() {
        Member newMember = Member.createMember("testName", 19);
        Team newTeam = Team.createTeam("testTeam");
        newMember.changeTeam(newTeam);

        em.persist(newTeam);
        em.persist(newMember);

        Member saveMember = query
                .selectFrom(member)
                .fetchOne();

        assertEquals(saveMember, newMember);
    }

    @Test
    @DisplayName("회원 영속 테스트")
    @Transactional
    void memberPersistenceTest() {
        Member newMember = Member.createMember("testName", 19);
        em.persist(newMember);

        Member saveMember = query
                .selectFrom(member)
                .fetchOne();

        assertEquals(saveMember, newMember);
    }
}