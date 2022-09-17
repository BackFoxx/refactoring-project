package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.CareerFormat;
import com.refactoring.refactoringproject.dto.MemberSignInFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoOrderFormat;
import com.refactoring.refactoringproject.entity.Career;
import com.refactoring.refactoringproject.entity.Favorite;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.repository.MemberRepository;
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
        String email = "test@gmail.com";
        String password = "testpassword1234";
        String level = "지망생";
        MemberSignInFormat signInFormat = MemberSignInFormat.of(email, password, level, null);

        // when
        memberService.signIn(signInFormat);

        // then
        Optional<Member> resultOptional = memberRepository.findById(email);
        if (!resultOptional.isPresent()) fail();

        Member result = resultOptional.get();
        assertThat(result.getId()).isEqualTo(email);
        assertThat(result.getLevel()).isEqualTo(level);
        assertThat(result.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("유효한 회원가입 정보(경력 있음)가 제공되면 회원가입에 성공한다.")
    void givenNormalMemberLoginDto_whenSigningIn_thenSuccess() {
        // given
        String email = "test@gmail.com";
        String password = "testpassword1234";
        String level = "주니어";

        CareerFormat career1 = new CareerFormat("삼성전자 응가부서", 30);
        CareerFormat career2 = new CareerFormat("네이버 핵폭탄부서", 4);

        MemberSignInFormat signInFormat = MemberSignInFormat.of(email, password, level, List.of(career1, career2));

        // when
        memberService.signIn(signInFormat);

        // then
        Optional<Member> resultOptional = memberRepository.findById(email);
        if (!resultOptional.isPresent()) fail();

        Member result = resultOptional.get();

        assertThat(result.getId()).isEqualTo(email);
        assertThat(result.getLevel()).isEqualTo(level);
        assertThat(result.getPassword()).isEqualTo(password);

        List<Career> careers = result.getCareers();
        assertThat(careers)
                .extracting(Career::getCompany).containsExactly("삼성전자 응가부서", "네이버 핵폭탄부서");
        assertThat(careers)
                .extracting(Career::getMonths).containsExactly(30, 4);
    }

    @Test
    @DisplayName("이미 회원가입 한 이메일로 회원가입 하려는 경우 예외가 발생한다.")
    void givenMemberWithAlreadyExistingId_whenSigningIn_thenThrowsIllegalArgumentException() {
        // given
        String email = "test@gmail.com";
        String password = "testpassword1234";
        String level = "주니어";

        MemberSignInFormat signInFormat = MemberSignInFormat.of(email, password, level, null);
        memberService.signIn(signInFormat);

        MemberSignInFormat inValidSignInFormat = MemberSignInFormat.of(email, password, level, null);

        // when
        assertThrows(IllegalArgumentException.class,
                () -> memberService.signIn(inValidSignInFormat));
    }

    @Test
    @DisplayName("어떤 리팩토링 대상 코드 게시글을 즐겨찾기 등록할 수 있다.")
    void givenMemberAndOneRefactoringTodo_whenRequestingFavorite_thenSuccess() {
        // given
        Member member = this.signInMemberWithIdCondition("test@gmail.com");
        Member member2 = this.signInMemberWithIdCondition("test2@gmail.com"); // 다른 사용자

        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member2);

        // when
        memberService.assignFavorite(savedRefactoringTodoId, member);

        // then
        List<Favorite> favorites = member.getFavorites();
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getRefactoringTodo().getId())
                .isEqualTo(savedRefactoringTodoId);
    }

    @Test
    @DisplayName("자신이 작성한 리팩토링 대상 코드 게시글을 즐겨찾기 등록할 수 없다.")
    void givenMemberAndOneRefactoringTodoOfHimself_whenRequestingFavorite_thenThrowsException() {
        // given
        Member member = this.signInMemberWithIdCondition("test@gmail.com");
        Member member2 = this.signInMemberWithIdCondition("test2@gmail.com"); // 다른 사용자

        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member2);

        memberService.assignFavorite(savedRefactoringTodoId, member);

        // when & then
        assertThatThrownBy(() -> memberService.assignFavorite(savedRefactoringTodoId, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("member can't assign RefactoringTodo which is already assigned. RefactoringTodo Id: " + savedRefactoringTodoId);
    }

    @Test
    @DisplayName("이미 즐겨찾기 등록한 리팩토링 대상 코드 게시글을 중복으로 즐겨찾기 등록할 수 없다.")
    void givenMemberAndOneRefactoringTodoAlreadyAssigned_whenRequestingFavorite_thenThrowsException() {
        // given
        Member member = this.signInMemberWithIdCondition("test@gmail.com");
        Long savedRefactoringTodoId = this.saveRefactoringTodoWithMemberCondition(member);

        // when & then
        assertThatThrownBy(() -> memberService.assignFavorite(savedRefactoringTodoId, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("member can't assign RefactoringTodo of himself to favorite. RefactoringTodo Id: " + savedRefactoringTodoId);
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

    private Member signInMemberWithIdCondition(String email) {
        String id = email;
        String password = "testpassword1234";
        String level = "주니어";

        CareerFormat career1 = new CareerFormat("삼성전자 응가부서", 30);
        CareerFormat career2 = new CareerFormat("네이버 핵폭탄부서", 4);

        MemberSignInFormat signInFormat = MemberSignInFormat.of(id, password, level, List.of(career1, career2));

        memberService.signIn(signInFormat);

        return memberRepository.findById(id).get();
    }
}