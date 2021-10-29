package com.irostub.learnquerydsl;

import com.irostub.learnquerydsl.entity.Hello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.irostub.learnquerydsl.entity.QHello.hello;


@SpringBootTest
class LearnQuerydslApplicationTests {

    @Autowired
    EntityManager em;

    @Test
    @Transactional
    void contextLoads() {
        Hello helloEntity = new Hello();
        em.persist(helloEntity);

        JPAQueryFactory query = new JPAQueryFactory(em);
        Hello result = query.selectFrom(hello).fetchOne();

        Assertions.assertNotNull(result.getId());
    }

}
