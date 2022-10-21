package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.Liked;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.LikedRepository;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringDoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @Autowired
    LikedRepository likedRepository;

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

    @Test
    @DisplayName("유효한 id를 이용해 리팩토링 한 코드 한 건을 조회할 수 있다.")
    void givenValidRefactoringDoneId_whenFindOneByGivenId_thenReturnsRefactoringDone() {
        // given
        Member member = this.signInMemberWithEmailCondition("nano@gmail.com");

        String code = "String id = email;\n" +
                "        String password = \"testpassword1234\";\n" +
                "        String level = \"주니어\";";
        String description = "완벽한 균형을 이루는 하나의 리팩토링 코드";

        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member);

        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                savedRefactoringTodoId,
                member,
                code,
                description
        );

        Long savedRefactoringDoneId = this.refactoringDoneService.saveRefactoringDone(format);

        // when
        RefactoringDoneResponse result = this.refactoringDoneService.findOneById(savedRefactoringDoneId);

        // then
        assertThat(result.getId()).isEqualTo(savedRefactoringDoneId);
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getRefactoringTodoResponse().getId()).isEqualTo(savedRefactoringTodoId);
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("유효하지 않은 id를 이용해 리팩토링 한 코드 한 건을 조회시 예외를 던진다.")
    void givenInValidRefactoringDoneId_whenFindOneByGivenId_thenThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> this.refactoringDoneService.findOneById(-1L))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessage("there is no RefactoringDone with id -1");
    }

    private static String makeString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append("*");
        }
        return stringBuilder.toString();
    }

    @Test
    @DisplayName("다른 사람이 작성한 리팩토링 한 코드 게시글에 좋아요를 등록할 수 있다.")
    void givenMemberAndRefactoringDoneWrittenByAnother_whenPostingLike_thenSuccess() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition("nano@gmail.com");
        Member baboMember = this.signInMemberWithEmailCondition("babo@gmail.com");
        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        Long savedRefactoringDoneByBabo = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

        // when
        this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo);

        // then
        List<Liked> result = this.likedRepository.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)
                .getMember())
                .isEqualTo(nanoMember);

        assertThat(result.get(0)
                .getRefactoringDone().getId())
                .isEqualTo(savedRefactoringDoneByBabo);
    }

    @Test
    @DisplayName("내가 작성한 리팩토링 한 코드 게시글을 좋아요할 수 없다.")
    void givenMemberAndRefactoringDoneWrittenByMe_whenPostingLike_thenThrowsException() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition("nano@gmail.com");
        Member baboMember = this.signInMemberWithEmailCondition("babo@gmail.com");
        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        Long savedRefactoringDoneByBabo = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

        // when && then
        assertThatThrownBy(() -> this.refactoringDoneService.assignLike(baboMember, savedRefactoringDoneByBabo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can't post like to RefactoringDone written by yourself");
        // 바보가 쓴 글을 바보가 좋아요한다.
    }

    @Test
    @DisplayName("이미 좋아요 누른 게시글을 또 좋아요 요청할 수 없다.")
    void givenMemberAndRefactoringDoneAlreadyLiked_whenPostingLike_thenThrowsException() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition("nano@gmail.com");
        Member baboMember = this.signInMemberWithEmailCondition("babo@gmail.com");
        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        Long savedRefactoringDoneByBabo = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

        this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo);

        // when & then
        assertThatThrownBy(() -> this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can't post like to RefactoringDone you already posted");
    }

    private Long saveRefactoringDoneWithMemberAndRefactoringTodoCondition(Member member, Long refactoringTodoId) {
        String code = "String id = email;\n" +
                "        String password = \"testpassword1234\";\n" +
                "        String level = \"주니어\";";
        String description = "완벽한 균형을 이루는 하나의 리팩토링 코드";

        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                refactoringTodoId,
                member,
                code,
                description
        );

        return this.refactoringDoneService.saveRefactoringDone(format);
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