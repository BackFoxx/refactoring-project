package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.CareerFormat;
import com.refactoring.refactoringproject.dto.MemberSignInFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoOrderFormat;
import com.refactoring.refactoringproject.entity.Career;
import com.refactoring.refactoringproject.entity.Favorite;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Transactional
class MemberServiceTest {
    public static final String TEST_GMAIL_EMAIL = "test@gmail.com";
    public static final String TEST_PASSWORD = "testpassword1234";
    public static final String JIMANG = "지망생";
    public static final String JUNIOR = "주니어";
    public static final String SAMSUNG_POOP_TEAM = "삼성전자 응가부서";
    public static final String NAVER_NUCLEAR_TEAM = "네이버 핵폭탄부서";
    public static final String TEST2_GMAIL_EMAIL = "test2@gmail.com";
    public static final String TEST_DESCRIPTION = "유효한 새 게시글이 제공되면 글이 정상적으로 등록된다.";
    public static final String TEST_TODO_ORDER = "메소드 중복을 없애 주십시오.";
    public static final String TEST_TODO_ORDER_2 = "개 소리 좀 안 나게 해라!!!!";
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
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefactoringTodoService refactoringTodoService;

    @Test
    @DisplayName("유효한 회원가입 정보(경력 없음)가 제공되면 회원가입에 성공한다.")
    void givenNormalSignInFormatWithoutCareer_whenSigningIn_thenSuccess() {
        // given
        String email = TEST_GMAIL_EMAIL;
        String password = TEST_PASSWORD;
        String level = JIMANG;
        MemberSignInFormat signInFormat = MemberSignInFormat.of(email, password, level, null);

        // when
        memberService.signIn(signInFormat);

        // then
        Optional<Member> resultOptional = memberRepository.findById(email);
        if (resultOptional.isEmpty()) fail();

        Member result = resultOptional.get();
        assertThat(result.getId()).isEqualTo(email);
        assertThat(result.getLevel()).isEqualTo(level);
        assertThat(result.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("유효한 회원가입 정보(경력 있음)가 제공되면 회원가입에 성공한다.")
    void givenNormalMemberLoginDto_whenSigningIn_thenSuccess() {
        // given
        MemberSignInFormat signInFormat = MemberSignInFormat.of(TEST_GMAIL_EMAIL, TEST_PASSWORD, JUNIOR,
                List.of(
                        new CareerFormat(SAMSUNG_POOP_TEAM, 30),
                        new CareerFormat(NAVER_NUCLEAR_TEAM, 4)
                )
        );

        // when
        memberService.signIn(signInFormat);

        // then
        Member result = memberRepository.findById(TEST_GMAIL_EMAIL).orElseThrow(Assertions::fail);

        assertThat(result.getId()).isEqualTo(TEST_GMAIL_EMAIL);
        assertThat(result.getLevel()).isEqualTo(JUNIOR);
        assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD);

        List<Career> careers = result.getCareers();
        assertThat(careers)
                .extracting(Career::getCompany).containsExactly(SAMSUNG_POOP_TEAM, NAVER_NUCLEAR_TEAM);
        assertThat(careers)
                .extracting(Career::getMonths).containsExactly(30, 4);
    }

    @Test
    @DisplayName("이미 회원가입 한 이메일로 회원가입 하려는 경우 예외가 발생한다.")
    void givenMemberWithAlreadyExistingId_whenSigningIn_thenThrowsIllegalArgumentException() {
        // given
        memberService.signIn(MemberSignInFormat.of(TEST_GMAIL_EMAIL, TEST_PASSWORD, JUNIOR, null));

        // when
        assertThrows(IllegalArgumentException.class,
                () -> memberService.signIn(MemberSignInFormat.of(TEST_GMAIL_EMAIL, TEST_PASSWORD, JUNIOR, null)));
    }

    @Test
    @DisplayName("어떤 리팩토링 대상 코드 게시글을 즐겨찾기 등록할 수 있다.")
    void givenMemberAndOneRefactoringTodo_whenRequestingFavorite_thenSuccess() {
        // given
        Member member = this.signInMemberWithIdCondition(TEST_GMAIL_EMAIL);
        Member member2 = this.signInMemberWithIdCondition(TEST2_GMAIL_EMAIL); // 다른 사용자

        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member2);

        // when
        memberService.assignFavorite(savedRefactoringTodoId, member);

        // then
        List<Favorite> favorites = member.getFavorites();
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0)
                .getRefactoringTodo().getId())
                .isEqualTo(savedRefactoringTodoId);
    }

    @Test
    @DisplayName("자신이 작성한 리팩토링 대상 코드 게시글을 즐겨찾기 등록할 수 없다.")
    void givenMemberAndOneRefactoringTodoOfHimself_whenRequestingFavorite_thenThrowsException() {
        // given
        Member member = this.signInMemberWithIdCondition(TEST_GMAIL_EMAIL);
        Member member2 = this.signInMemberWithIdCondition(TEST2_GMAIL_EMAIL); // 다른 사용자

        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member2);

        memberService.assignFavorite(savedRefactoringTodoId, member);

        // when & then
        assertThatThrownBy(() -> memberService.assignFavorite(savedRefactoringTodoId, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        String.format(
                                MemberService.ALREADY_ASSIGNED_REFACTORING_TODO_ID_MESSAGE_FORMAT,
                                savedRefactoringTodoId)
                );
    }

    @Test
    @DisplayName("이미 즐겨찾기 등록한 리팩토링 대상 코드 게시글을 중복으로 즐겨찾기 등록할 수 없다.")
    void givenMemberAndOneRefactoringTodoAlreadyAssigned_whenRequestingFavorite_thenThrowsException() {
        // given
        Member member = this.signInMemberWithIdCondition(TEST_GMAIL_EMAIL);
        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member);

        // when & then
        assertThatThrownBy(() -> memberService.assignFavorite(savedRefactoringTodoId, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        String.format(
                                MemberService.INVALID_ASSIGNING_REFACTORING_TODO_MESSAGE_FORMAT,
                                savedRefactoringTodoId)
                );
    }

    private Long saveRefactoringTodoWithMemberCondition(Member member) {
        Long savedId = refactoringTodoService.saveRefactoringTodo(
                RefactoringTodoFormat.of(
                        member, TEST_LANGUAGE, TEST_CODE, TEST_DESCRIPTION,
                        List.of(
                                RefactoringTodoOrderFormat.of(TEST_TODO_ORDER),
                                RefactoringTodoOrderFormat.of(TEST_TODO_ORDER_2)
                        )
                )
        );
        em.flush();
        em.clear();
        return savedId;
    }

    private Member signInMemberWithIdCondition(String email) {
        MemberSignInFormat signInFormat = MemberSignInFormat.of(
                email, TEST_PASSWORD, JUNIOR,
                List.of(new CareerFormat(SAMSUNG_POOP_TEAM, 30),
                        new CareerFormat(NAVER_NUCLEAR_TEAM, 4)
                )
        );
        memberService.signIn(signInFormat);
        return memberRepository.findById(email).orElseThrow(Assertions::fail);
    }
}