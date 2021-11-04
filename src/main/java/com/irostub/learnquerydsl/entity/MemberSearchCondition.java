package com.irostub.learnquerydsl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberSearchCondition {
    private String username;
    private Integer age;
}
