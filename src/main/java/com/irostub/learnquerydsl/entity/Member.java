package com.irostub.learnquerydsl.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Getter
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = javax.persistence.FetchType.LAZY)
    private Team team;

    public static Member createMember(String username, int age) {
        Member member = new Member();

        member.username = username;
        member.age = age;

        return member;
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", age=" + age +
                '}';
    }
}
