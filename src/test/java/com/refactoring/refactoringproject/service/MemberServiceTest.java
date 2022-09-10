package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.CareerFormat;
import com.refactoring.refactoringproject.dto.MemberSignInFormat;
import com.refactoring.refactoringproject.entity.Career;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

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
}