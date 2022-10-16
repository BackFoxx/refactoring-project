package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringDoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@Transactional
class RefactoringDoneServiceTest {
    @Autowired
    EntityManager em;

    @Autowired
    RefactoringTodoService refactoringTodoService;

    @Autowired
    RefactoringDoneRepository refactoringDoneRepository;

    @Autowired
    RefactoringDoneService refactoringDoneService;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("존재하는 리팩토링 대상 코드에 대해 올바른 내용으로 리팩토링 한 코드를 등록 요청 시 성공한다.")
    void givenValidRefactoringDoneToExistingRefactoringTodo_whenSavingRefactoringTodo_thenSuccess() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition("nano@gmail.com");
        Member baboMember = this.signInMemberWithEmailCondition("babo@gmail.com");
        Long savedRefactoringTodoIdByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        String code = "String id = email;\n" +
                "        String password = \"testpassword1234\";\n" +
                "        String level = \"주니어\";";
        String description = "완벽한 균형을 이루는 하나의 리팩토링 코드";

        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                savedRefactoringTodoIdByNano,
                baboMember,
                code,
                description
        );

        // when
        Long savedRefactoringDoneId = refactoringDoneService.saveRefactoringDone(format);
        em.flush();
        em.clear();

        // then
        Optional<RefactoringDone> resultOptional = refactoringDoneRepository.findById(savedRefactoringDoneId);
        if (resultOptional.isEmpty()) fail("target Not Saved");
        RefactoringDone result = resultOptional.get();

        assertThat(savedRefactoringDoneId).isEqualTo(result.getId());
        assertThat(result.getRefactoringTodo().getId()).isEqualTo(savedRefactoringTodoIdByNano);
        assertThat(result.getMember()).isEqualTo(baboMember);
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getDescription()).isEqualTo(description);
    }

    private Member signInMemberWithEmailCondition(String email) {
        String id = email;
        String password = "testpassword1234";
        String level = "주니어";

        CareerFormat career1 = new CareerFormat("삼성전자 응가부서", 30);
        CareerFormat career2 = new CareerFormat("네이버 핵폭탄부서", 4);

        MemberSignInFormat signInFormat = MemberSignInFormat.of(id, password, level, List.of(career1, career2));

        memberService.signIn(signInFormat);

        return memberRepository.findById(id).get();
    }

    private Long saveRefactoringTodoWithMemberCondition(Member member) {
        String language = "JAVA";
        String code = "    private String signInMember() {\n" +
                "        String email = \"test@gmail.com\";\n" +
                "        String password = \"testpassword1234\";\n" +
                "        String level = \"주니어\";\n" +
                "\n" +
                "        CareerFormat career1 = new CareerFormat(\"삼성전자 응가부서\", 30);\n" +
                "        CareerFormat career2 = new CareerFormat(\"네이버 핵폭탄부서\", 4);\n" +
                "\n" +
                "        MemberSignInFormat signInFormat = MemberSignInFormat.of(email, password, level, List.of(career1, career2));\n" +
                "\n" +
                "        memberService.signIn(signInFormat);\n" +
                "\n" +
                "        return email;\n" +
                "    }";
        String description = "유효한 새 게시글이 제공되면 글이 정상적으로 등록된다.";
        RefactoringTodoOrderFormat todoOrderFormat1 = RefactoringTodoOrderFormat.of("메소드 중복을 없애 주십시오.");
        RefactoringTodoOrderFormat todoOrderFormat2 = RefactoringTodoOrderFormat.of("개 소리 좀 안 나게 해라!!!!");

        RefactoringTodoFormat format = RefactoringTodoFormat.of(member, language, code, description, List.of(todoOrderFormat1, todoOrderFormat2));

        Long savedId = refactoringTodoService.saveRefactoringTodo(format);

        em.flush();
        em.clear();

        return savedId;
    }
}