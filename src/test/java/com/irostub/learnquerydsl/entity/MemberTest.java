package com.irostub.learnquerydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.irostub.learnquerydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory query;

    @BeforeEach
    void setUp() {
        query = new JPAQueryFactory(em);
    }

    @BeforeEach
    void init() {
        Member newMember1 = Member.createMember("testName1", 19);
        Member newMember2 = Member.createMember("testName2", 20);
        Member newMember3 = Member.createMember("testName3", 30);
        Member newMember4 = Member.createMember("testName4", 31);
        Team newTeam1 = Team.createTeam("teamA");
        Team newTeam2 = Team.createTeam("teamB");
        newMember1.changeTeam(newTeam1);
        newMember2.changeTeam(newTeam1);
        newMember3.changeTeam(newTeam2);
        newMember4.changeTeam(newTeam2);

        em.persist(newTeam1);
        em.persist(newTeam2);
        em.persist(newMember1);
        em.persist(newMember2);
        em.persist(newMember3);
        em.persist(newMember4);
    }

    @Test
    @DisplayName("회원 영속 테스트 - pure jpa")
    void memberPersistenceTest() {
        Member saveMember = em.createQuery("select m from Member m order by m.id asc", Member.class)
                .setFirstResult(0)
                .setMaxResults(1)
                .getSingleResult();
        assertNotNull(saveMember);
    }

    @Test
    @DisplayName("회원 팀 영속 테스트 - pure jpa")
    void memberTeamPersistenceTest() {
        Member saveMember = em.createQuery("select m from Member m join fetch m.team", Member.class)
                .setFirstResult(0)
                .setMaxResults(1)
                .getSingleResult();
        assertNotNull(saveMember);
        assertNotNull(saveMember.getTeam());
    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - querydsl")
    void findMemberByName() {
        final String testName = "testName1";
        List<Member> result = query
                .selectFrom(member)
                .where(member.username.eq(testName))
                .fetch();
        assertThat(result).extracting(Member::getUsername).containsExactly(testName);
    }

    @Test
    @DisplayName("회원 이름과 나이로 회원 찾기 - querydsl")
    void findMemberByNameAndAge() {
        final String testName = "testName1";
        final int testAge = 19;

        List<Member> result = query
                .selectFrom(member)
                .where(member.username.eq(testName), member.age.eq(testAge))
                .fetch();
        assertThat(result).extracting(Member::getUsername).containsExactly(testName);
        assertThat(result).extracting(Member::getAge).containsExactly(testAge);
    }

    @Test
    @DisplayName("조회 방식 테스트")
    void findTypeTest() {
        //list 조회
        List<Member> result1 = query
                .selectFrom(member)
                .fetch();
        //단건 조회
        Member result2 = query
                .selectFrom(member)
                .fetchOne();
        //첫번째 단건 limit 조회
        Member result3 = query
                .selectFrom(member)
                .fetchFirst();
        //count 쿼리 조회
        long count = query
                .selectFrom(member)
                .fetchCount();
        //페이징 포함 조회
        QueryResults<Member> memberQueryResults = query
                .selectFrom(member)
                .fetchResults();
    }

    //null data last or first sort 적용
    @Test
    @DisplayName("정렬 쿼리 테스트")
    void sortTest() {
        List<Member> fetch = query
                .selectFrom(member)
                .orderBy(member.age.desc().nullsLast())
                .fetch();
        assertThat(fetch).extracting(Member::getAge).containsExactly(31, 30, 20, 19);
    }

    @Test
    @DisplayName("페이징 쿼리 테스트")
    void pagingTest() {
        List<Member> fetch = query
                .selectFrom(member)
                .orderBy(member.age.asc())
                .offset(0)
                .limit(2)
                .fetch();
        assertThat(fetch).extracting(Member::getAge).containsExactly(19, 20);
    }
}