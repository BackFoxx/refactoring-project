package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.Liked;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.LikedRepository;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringDoneRepository;
import org.junit.jupiter.api.Assertions;
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
    public static final String TEST_GMAIL_EMAIL = "test@gmail.com";
    public static final String TEST_PASSWORD = "testpassword1234";
    public static final String JUNIOR = "주니어";
    public static final String SAMSUNG_POOP_TEAM = "삼성전자 응가부서";
    public static final String NAVER_NUCLEAR_TEAM = "네이버 핵폭탄부서";
    public static final String TEST2_GMAIL_EMAIL = "test2@gmail.com";
    public static final String TEST_DESCRIPTION = "유효한 새 게시글이 제공되면 글이 정상적으로 등록된다.";
    public static final String TEST_CODE = "    private String signInMember() {\n" +
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
    public static final String TEST_LANGUAGE = "JAVA";
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
        Member testMember = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Member testMember2 = this.signInMemberWithEmailCondition(TEST2_GMAIL_EMAIL);
        Long savedRefactoringTodoIdByNano = this.saveRefactoringTodoWithMemberCondition(testMember);

        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                savedRefactoringTodoIdByNano,
                testMember2,
                TEST_CODE,
                TEST_DESCRIPTION
        );

        // when
        Long savedRefactoringDoneId = refactoringDoneService.saveRefactoringDone(format);
        em.flush();
        em.clear();

        // then
        RefactoringDone result = refactoringDoneRepository
                .findById(savedRefactoringDoneId)
                .orElseThrow(() -> fail("실패"));

        assertThat(savedRefactoringDoneId).isEqualTo(result.getId());
        assertThat(result.getRefactoringTodo().getId()).isEqualTo(savedRefactoringTodoIdByNano);
        assertThat(result.getMember()).isEqualTo(testMember2);
        assertThat(result.getCode()).isEqualTo(TEST_CODE);
        assertThat(result.getDescription()).isEqualTo(TEST_DESCRIPTION);
    }

    @Test
    @DisplayName("존재하지 않는 리팩토링 대상 코드에 대해 리팩토링 한 코드를 등록 요청 시 예외를 던진다.")
    void givenValidRefactoringDoneToNonExistingRefactoringTodo_whenSavingRefactoringTodo_thenThrowsException() {
        // given

        // when && then
        assertThatThrownBy(() -> refactoringDoneService.saveRefactoringDone(
                RefactoringDoneFormat.of(
                        -1L, // 존재하지 않는 리팩토링 대상 코드
                        this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL),
                        TEST_CODE,
                        TEST_DESCRIPTION
                )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you tried to post a RefactoringDone of RefactoringTodo which is not existing");
    }

    @ParameterizedTest(name = "{index}번 -> 잘못된 형식의 {1}")
    @MethodSource("invalidFormats")
    @DisplayName("올바르지 않은 내용으로 리팩토링 한 코드를 등록 요청 시 예외를 던진다.")
    void givenInValidRefactoringDoneToExistingRefactoringTodo_whenSavingRefactoringTodo_thenThrowsException(RefactoringDoneFormat format, String errorCause) {
        // given
        Member member = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        format.setRefactoringTodoId(this.saveRefactoringTodoWithMemberCondition(member));
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
                                TEST_DESCRIPTION
                        ),
                        TEST_CODE
                ),
                Arguments.of(
                        RefactoringDoneFormat.of(
                                null,
                                null,
                                TEST_CODE,
                                makeString(1100) // 1,000자를 넘어가는 리팩토링 코드 설명
                        ),
                        TEST_DESCRIPTION
                )
        );
    }

    @Test
    @DisplayName("유효한 id를 이용해 리팩토링 한 코드 한 건을 조회할 수 있다.")
    void givenValidRefactoringDoneId_whenFindOneByGivenId_thenReturnsRefactoringDone() {
        // given
        Member member = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member);
        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                savedRefactoringTodoId,
                member,
                TEST_CODE,
                TEST_DESCRIPTION
        );
        Long savedRefactoringDoneId = this.refactoringDoneService.saveRefactoringDone(format);

        // when
        RefactoringDoneResponse result = this.refactoringDoneService.findOneById(savedRefactoringDoneId);

        // then
        assertThat(result.getId()).isEqualTo(savedRefactoringDoneId);
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getRefactoringTodoResponse().getId()).isEqualTo(savedRefactoringTodoId);
        assertThat(result.getCode()).isEqualTo(TEST_CODE);
        assertThat(result.getDescription()).isEqualTo(TEST_DESCRIPTION);
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
        stringBuilder.append("*".repeat(Math.max(0, length)));
        return stringBuilder.toString();
    }

    @Test
    @DisplayName("다른 사람이 작성한 리팩토링 한 코드 게시글에 좋아요를 등록할 수 있다.")
    void givenMemberAndRefactoringDoneWrittenByAnother_whenPostingLike_thenSuccess() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Member baboMember = this.signInMemberWithEmailCondition(TEST2_GMAIL_EMAIL);

        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);
        Long savedRefactoringDoneByBabo
                = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

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
        Member nanoMember = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Member baboMember = this.signInMemberWithEmailCondition(TEST2_GMAIL_EMAIL);
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
        Member nanoMember = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Member baboMember = this.signInMemberWithEmailCondition(TEST2_GMAIL_EMAIL);
        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        Long savedRefactoringDoneByBabo = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

        this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo);

        // when & then
        assertThatThrownBy(() -> this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can't post like to RefactoringDone you already posted");
    }

    @Test
    @DisplayName("리팩토링 한 코드에 달린 좋아요를 삭제할 수 있다.")
    void givenRefactoringDoneId_whenDeletingLike_thenSuccess() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Member baboMember = this.signInMemberWithEmailCondition(TEST2_GMAIL_EMAIL);
        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        Long savedRefactoringDoneByBabo = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

        this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo);
        List<Liked> likeList = this.likedRepository.findAll();
        assertThat(likeList).hasSize(1);

        // when
        this.refactoringDoneService.deleteLike(nanoMember, savedRefactoringDoneByBabo);

        // then
        likeList = this.likedRepository.findAll();
        assertThat(likeList).hasSize(0);
    }

    @Test
    @DisplayName("좋아요를 등록한 적 없는 게시글에 대해 좋아요를 삭제할 수 없다.")
    void givenRefactoringDoneIdThatNeverLiked_whenDeletingLike_thenThrowsException() {
        // given
        Member nanoMember = this.signInMemberWithEmailCondition(TEST_GMAIL_EMAIL);
        Member baboMember = this.signInMemberWithEmailCondition(TEST2_GMAIL_EMAIL);
        Long savedRefactoringTodoByNano = this.saveRefactoringTodoWithMemberCondition(nanoMember);

        Long savedRefactoringDoneByBabo = this.saveRefactoringDoneWithMemberAndRefactoringTodoCondition(baboMember, savedRefactoringTodoByNano);

        this.refactoringDoneService.assignLike(nanoMember, savedRefactoringDoneByBabo);
        List<Liked> likeList = this.likedRepository.findAll();
        assertThat(likeList).hasSize(1);

        // when && then
        assertThatThrownBy(() -> this.refactoringDoneService.deleteLike(baboMember, savedRefactoringDoneByBabo)) // baboMember로 좋아요를 등록한 적 없다.
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You may not have posted like on this RefactoringDone");
    }

    private Long saveRefactoringDoneWithMemberAndRefactoringTodoCondition(Member member, Long refactoringTodoId) {
        RefactoringDoneFormat format = RefactoringDoneFormat.of(
                refactoringTodoId,
                member,
                TEST_CODE,
                TEST_DESCRIPTION
        );

        return this.refactoringDoneService.saveRefactoringDone(format);
    }

    private Member signInMemberWithEmailCondition(String email) {
        CareerFormat career1 = new CareerFormat(SAMSUNG_POOP_TEAM, 30);
        CareerFormat career2 = new CareerFormat(NAVER_NUCLEAR_TEAM, 4);

        MemberSignInFormat signInFormat = MemberSignInFormat.of(email, TEST_PASSWORD, JUNIOR, List.of(career1, career2));
        memberService.signIn(signInFormat);

        return memberRepository.findById(email).get();
    }

    private Long saveRefactoringTodoWithMemberCondition(Member member) {
        RefactoringTodoFormat format = RefactoringTodoFormat.of(
                member, TEST_LANGUAGE, TEST_CODE, TEST_DESCRIPTION,
                List.of(
                        RefactoringTodoOrderFormat.of("메소드 중복을 없애 주십시오."),
                        RefactoringTodoOrderFormat.of("개 소리 좀 안 나게 해라!!!!")
                )
        );
        Long savedId = refactoringTodoService.saveRefactoringTodo(format);

        em.flush();
        em.clear();

        return savedId;
    }
}