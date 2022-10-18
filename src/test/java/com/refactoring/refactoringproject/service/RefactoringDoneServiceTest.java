package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringDoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class RefactoringDoneServiceTest {
    @PersistenceContext
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

    @Test
    @DisplayName("존재하지 않는 리팩토링 대상 코드에 대해 리팩토링 한 코드를 등록 요청 시 예외를 던진다.")
    void givenValidRefactoringDoneToNonExistingRefactoringTodo_whenSavingRefactoringTodo_thenThrowsException() {
        // given
        Member member = this.signInMemberWithEmailCondition("nano@gmail.com");

        String code = "String id = email;\n" +
                "        String password = \"testpassword1234\";\n" +
                "        String level = \"주니어\";";
        String description = "완벽한 균형을 이루는 하나의 리팩토링 코드";

        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                -1L, // 존재하지 않는 리팩토링 대상 코드
                member,
                code,
                description
        );

        // when && then
        assertThatThrownBy(() -> refactoringDoneService.saveRefactoringDone(format))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you tried to post a RefactoringDone of RefactoringTodo which is not existing");
    }

    @ParameterizedTest(name = "{index}번 -> 잘못된 형식의 {1}")
    @MethodSource("invalidFormats")
    @DisplayName("올바르지 않은 내용으로 리팩토링 한 코드를 등록 요청 시 예외를 던진다.")
    void givenInValidRefactoringDoneToExistingRefactoringTodo_whenSavingRefactoringTodo_thenThrowsException(RefactoringDoneFormat format, String errorCause) {
        // given
        Member member = this.signInMemberWithEmailCondition("nano@gmail.com");
        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member);
        format.setRefactoringTodoId(savedRefactoringTodoId);
        format.setMember(member);

        // when & then
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<RefactoringDoneFormat>> violations = validator.validate(format);

        assertThat(violations).hasSize(1);
        for (ConstraintViolation<RefactoringDoneFormat> violation : violations) {
            assertThat(violation.getPropertyPath().toString()).endsWith(errorCause);
        }
    }

    private static Stream<Arguments> invalidFormats() {
        return Stream.of(
                Arguments.of(
                        RefactoringDoneFormat.of(
                                null,
                                null,
                                makeString(11000), // 10,000자를 넘어가는 리팩토링 코드
                                "정상적인 코드 설명"
                        ),
                        "code"
                ),
                Arguments.of(
                        RefactoringDoneFormat.of(
                                null,
                                null,
                                "System.out.println(\"정상적인 코드\")",
                                makeString(1100) // 1,000자를 넘어가는 리팩토링 코드 설명
                        ),
                        "description"
                )
        );
    }

    private static String makeString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append("*");
        }
        return stringBuilder.toString();
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