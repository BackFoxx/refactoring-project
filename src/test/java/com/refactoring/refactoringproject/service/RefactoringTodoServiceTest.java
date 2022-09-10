package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.CareerFormat;
import com.refactoring.refactoringproject.dto.MemberSignInFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoOrderFormat;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RefactoringTodoServiceTest {
    @PersistenceContext
    EntityManager em;

    @Autowired
    RefactoringTodoService refactoringTodoService;

    @Autowired
    RefactoringTodoRepository refactoringTodoRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("리팩토링 대상 코드 - 유효한 새 게시글이 제공되면 글이 정상적으로 등록된다.")
    void givenNormalRefactoringTodo_whenSavingRefactoringTodo_thenSuccess() {
        // given
        Member member = this.signInMember();

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

        // when
        refactoringTodoService.saveRefactoringTodo(format);

        em.flush();
        em.clear();

        // then
        List<RefactoringTodo> allResult = refactoringTodoRepository.findAll(); // 1개만 등록했으므로 1개만 조회되어야 한다.
        assertThat(allResult).hasSize(1);

        RefactoringTodo result = allResult.get(0);

        assertThat(result.getMember())
                .extracting(Member::getId).isEqualTo(member.getId());
        assertThat(result.getLanguage()).isEqualTo(language);
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getOrders())
                .extracting(RefactoringTodoOrder::getContent)
                .containsExactly("메소드 중복을 없애 주십시오.", "개 소리 좀 안 나게 해라!!!!");
    }

    private Member signInMember() {
        String id = "test@gmail.com";
        String password = "testpassword1234";
        String level = "주니어";

        CareerFormat career1 = new CareerFormat("삼성전자 응가부서", 30);
        CareerFormat career2 = new CareerFormat("네이버 핵폭탄부서", 4);

        MemberSignInFormat signInFormat = MemberSignInFormat.of(id, password, level, List.of(career1, career2));

        memberService.signIn(signInFormat);

        return memberRepository.findById(id).get();
    }
}
