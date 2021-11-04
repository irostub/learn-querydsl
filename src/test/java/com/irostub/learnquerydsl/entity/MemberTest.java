package com.irostub.learnquerydsl.entity;

import com.irostub.learnquerydsl.dto.MemberDto;
import com.irostub.learnquerydsl.dto.QMemberDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.ArrayList;
import java.util.List;

import static com.irostub.learnquerydsl.entity.QMember.member;
import static com.irostub.learnquerydsl.entity.QTeam.*;
import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @PersistenceUnit
    private EntityManagerFactory emf;

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
                .limit(1)
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

    @Test
    @DisplayName("페이징 쿼리 & 카운트 테스트")
    void pagingAndCountTest() {
        QueryResults<Member> memberQueryResults = query
                .selectFrom(member)
                .orderBy(member.username.asc())
                .offset(0)
                .limit(2)
                .fetchResults();
        assertEquals(memberQueryResults.getResults().size(), 2);
        assertEquals(memberQueryResults.getTotal(), 4);
        assertEquals(memberQueryResults.getOffset(), 0);
        assertEquals(memberQueryResults.getLimit(), 2);
    }

    @Test
    @DisplayName("집합 쿼리 테스트")
    void aggregationTest() {
        List<Tuple> fetch = query
                .select(
                        member.count().as("memberCount"),
                        member.age.sum().as("ageSum"),
                        member.age.avg().as("ageAvg"),
                        member.age.max().as("ageMax"),
                        member.age.min().as("ageMin"))
                .from(member)
                .fetch();

        assertThat(fetch).extracting(tuple ->
                tuple.get(member.count().as("memberCount"))).containsExactly(4L);
        assertThat(fetch).extracting(tuple ->
                tuple.get(member.age.sum().as("ageSum"))).containsExactly(100);
        assertThat(fetch).extracting(tuple ->
                tuple.get(member.age.avg().as("ageAvg"))).containsExactly(25.0);
        assertThat(fetch).extracting(tuple ->
                tuple.get(member.age.max().as("ageMax"))).containsExactly(31);
    }

    @Test
    @DisplayName("그룹핑 테스트")
    void groupTest() {
        List<Tuple> fetch = query
                .select(team.name, member.age.avg().as("ageAvg"))
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        assertThat(fetch).extracting(tuple ->
                tuple.get(team.name)).containsExactly("teamA", "teamB");
        assertThat(fetch).extracting(tuple ->
                tuple.get(member.age.avg().as("ageAvg"))).containsExactly(19.5, 30.5);
    }

    @Test
    @DisplayName("기본 조인 테스트")
    void defaultJoinTest() {
        List<Member> fetch = query
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(fetch).extracting(Member::getUsername)
                .containsExactly("testName1", "testName2");
    }

    @Test
    @DisplayName("세타 조인 테스트")
    void thetaJoinTest() {
        Member newMember1 = Member.createMember("teamA", 49);
        Member newMember2 = Member.createMember("teamB", 55);

        em.persist(newMember1);
        em.persist(newMember2);

        List<Member> fetch = query
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(fetch).extracting(Member::getUsername)
                .containsExactly("teamA", "teamB");
    }

    @Test
    @DisplayName("on을 활용한 조인 테스트")
    void onJoinTest() {
        Member newMember1 = Member.createMember("teamA", 49);
        Member newMember2 = Member.createMember("teamB", 55);

        em.persist(newMember1);
        em.persist(newMember2);

        List<Member> fetch = query
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .on(member.username.eq(team.name))
                .orderBy(member.username.asc().nullsLast())
                .fetch();

        assertThat(fetch).extracting(Member::getUsername)
                .containsExactly("teamA", "teamB", "testName1", "testName2", "testName3", "testName4");
    }

    @Test
    @DisplayName("페치 조인 테스트")
    void fetchJoinTest() {
        List<Member> fetch = query
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .fetch();
        assertThat(fetch).extracting(Member::getTeam).isNotNull();
        assertThat(fetch).extracting(member ->
                        emf.getPersistenceUnitUtil().isLoaded(member.getTeam()))
                .containsExactly(true, true, true, true);
    }

    @Test
    @DisplayName("서브쿼리 eq")
    void subQueryEqTest() {
        QMember subMember = new QMember("subMember");
        List<Member> fetch = query
                .selectFrom(member)
                .where(member.id.in(
                        select(subMember.id)
                                .from(subMember)))
                .fetch();
        assertEquals(4, fetch.size());
    }

    @Test
    @DisplayName("서브쿼리 loe")
    void subQueryLoeTest() {
        QMember subMember = new QMember("subMember");
        List<Member> fetch = query
                .selectFrom(member)
                .where(member.age.loe(
                        select(subMember.age.avg())
                                .from(subMember)))
                .fetch();
        assertEquals(2, fetch.size());
    }

    @Test
    @DisplayName("Case 문")
    void caseTest() {
        QMember subMember = new QMember("subMember");
        List<String> fetch = query
                .select(new CaseBuilder()
                        .when(member.age.goe(
                                select(subMember.age.avg()).from(subMember)))
                        .then("이상")
                        .otherwise("미만")
                        .as("eval"))
                .from(member)
                .fetch();
        assertThat(fetch).contains("이상", "미만");
        assertEquals(4, fetch.size());
    }

    @Test
    @DisplayName("Simple Case 문")
    void simpleCaseTest() {
        List<Integer> fetch = query
                .select(member.age
                        .when(19).then(1)
                        .otherwise(2))
                .from(member)
                .fetch();
        assertEquals(4, fetch.size());
    }

    @Test
    @DisplayName("case 문 분리")
    void separateCaseTest() {
        StringExpression casePath = new CaseBuilder()
                .when(member.age.goe(25))
                .then("이상")
                .otherwise("미만");
        List<Tuple> fetch = query
                .select(member.username, casePath)
                .from(member)
                .orderBy(casePath.asc())
                .fetch();

        assertThat(fetch).extracting(tuple -> tuple.get(casePath)).contains("이상", "미만");
    }

    @Test
    @DisplayName("상수, 문자 더하기")
    void calcTest() {
        List<Tuple> fetch1 = query
                .select(member.username, Expressions.constant("EE"))
                .from(member)
                .fetch();

        List<String> fetch2 = query
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        List<Integer> fetch3 = query
                .select(member.age.add(5))
                .from(member)
                .fetch();
    }

    @Test
    @DisplayName("Dto반환 프로젝션 테스트")
    void returnDtoTest() {
        //프로퍼티 접근
        List<MemberDto> fetch = query
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //필드 접근
        List<MemberDto> fetch1 = query
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //생성자 접근
        List<MemberDto> fetch2 = query
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
    }


    //일반적인 프로젝션과 다르게 select 에서 타입체크 가능,
    //단, Dto 가 Querydsl 을 의존하게됨
    @Test
    @DisplayName("@QueryProjection 사용한 프로젝션 테스트")
    void queryProjectionTest() {
        List<MemberDto> fetch = query
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
    }

    @Test
    @DisplayName("동적 쿼리 where 절 테스트")
    void dynamicWhereConditionTest() {
        final String usernameCondition = "t";
        final int ageCondition = 25;

        List<MemberSearchCondition> testConditions = getMemberSearchConditions(usernameCondition, ageCondition);

        for (MemberSearchCondition testCondition : testConditions) {
            BooleanBuilder builder = new BooleanBuilder();
            if (StringUtils.hasText(testCondition.getUsername())) {
                builder.and(member.username.like(testCondition.getUsername()));
            }
            if (testCondition.getAge() != null) {
                builder.and(member.age.goe(testCondition.getAge()));
            }

            List<Member> fetch = query
                    .selectFrom(member)
                    .where(builder).fetch();
        }
    }

    @Test
    @DisplayName("동적 쿼리 where 절 분리 테스트")
    void dynamicQuerySeparateMethodTest() {
        final String usernameCondition = "t";
        final int ageCondition = 25;

        List<MemberSearchCondition> testConditions = getMemberSearchConditions(usernameCondition, ageCondition);

        for (MemberSearchCondition testCondition : testConditions) {
            List<Member> fetch = query
                    .selectFrom(member)
                    .where(
                            usernameLike(testCondition.getUsername()),
                            ageGreaterEqThan(testCondition.getAge())
                    ).fetch();
        }
    }

    @Test
    @DisplayName("수정 벌크 연산")
    void updateBulkQueryTest() {
        Member newMember = Member.createMember("updateMember", 20);
        em.persist(newMember);

        //업데이트 적용 건수 return
        long execute = query
                .update(member)
                .set(member.age, member.age.add(1))
                .where(member.username.eq("updateMember"))
                .execute();

        em.clear();

        assertEquals(1L, execute);
    }

    @Test
    @DisplayName("삭제 벌크 연산")
    void deleteBulkQueryTest() {
        Member newMember = Member.createMember("deleteMember", 20);
        em.persist(newMember);

        long execute = query
                .delete(member)
                .where(member.username.eq("deleteMember"))
                .execute();

        em.clear();

        assertEquals(1L, execute);
    }

    @Test
    @DisplayName("sql function call 테스트")
    void sqlFunctionCallTest() {
        String s = query
                .select(Expressions
                        .stringTemplate("function('upper',{0})", member.username))
                .from(member)
                .fetchFirst();
    }

    private BooleanExpression ageGreaterEqThan(Integer age) {
        return age != null ? member.age.goe(age) : null;
    }

    private BooleanExpression usernameLike(String username) {
        return StringUtils.hasText(username) ? member.username.like(username) : null;
    }

    private List<MemberSearchCondition> getMemberSearchConditions(String usernameCondition, int ageCondition) {
        List<MemberSearchCondition> testConditions = new ArrayList<>();
        testConditions.add(new MemberSearchCondition(usernameCondition, ageCondition));
        testConditions.add(new MemberSearchCondition(usernameCondition, null));
        testConditions.add(new MemberSearchCondition(null, ageCondition));
        testConditions.add(new MemberSearchCondition(null, null));
        return testConditions;
    }
}