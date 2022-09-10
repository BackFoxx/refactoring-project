package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Career;
import com.refactoring.refactoringproject.entity.Member;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MemberSignInFormat {
    public MemberSignInFormat() {
    }

    private MemberSignInFormat(String email, String password, String level, List<CareerFormat> careers) {
        this.email = email;
        this.password = password;
        this.level = level;
        if (careers != null) this.careers = careers;
    }

    public static MemberSignInFormat of(String email, String password, String level, List<CareerFormat> careers) {
        return new MemberSignInFormat(email, password, level, careers);
    }

    public static Member toEntity(MemberSignInFormat dto) {
        Member member = new Member(dto.email, dto.password, dto.level);
        List<Career> careers = dto.careers.stream().map(careerFormat -> CareerFormat.toEntity(careerFormat))
                .collect(Collectors.toList());
        careers.forEach(career -> member.addCareer(career));

        return member;
    }

    private String email;
    private String password;
    private String level;
    private List<CareerFormat> careers = new ArrayList<>();
}
