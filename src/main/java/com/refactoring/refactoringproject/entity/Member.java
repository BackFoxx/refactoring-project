package com.refactoring.refactoringproject.entity;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "MEMBER")
public class Member extends BaseEntityTime {
    public Member() {
    }

    public Member(String id, String password, String level) {
        this.id = id;
        this.password = password;
        this.level = level;
    }

    public Member(String id, String password, String level, List<Career> careers) {
        this.id = id;
        this.password = password;
        this.level = level;
        this.careers = careers;
    }

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 10, nullable = false)
    private String level;

    @OneToMany(mappedBy = "member", cascade = CascadeType.MERGE)
    private List<Career> careers = new ArrayList<>();

    public void addCareer(Career career) {
        this.careers.add(career);
        career.assignMember(this);
    }
}
