package com.irostub.learnquerydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TeamTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @BeforeEach
    void setUp() {
        query = new JPAQueryFactory(em);
    }

    @Test
    @DisplayName("팀 영속 테스트")
    @Transactional
    void persistTest() {
        Team team = Team.createTeam("testTeam");
        em.persist(team);
        assertNotNull(team.getId());
    }
}