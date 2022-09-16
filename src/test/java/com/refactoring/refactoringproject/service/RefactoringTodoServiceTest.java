package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

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
        Long savedId = refactoringTodoService.saveRefactoringTodo(format);

        em.flush();
        em.clear();

        // then
        Optional<RefactoringTodo> resultOptional = refactoringTodoRepository.findById(savedId);

        if (!resultOptional.isPresent()) fail("Id에 해당하는 RefactoringTodo가 조회되어야 한다.");
        RefactoringTodo result = resultOptional.get();

        assertThat(result.getMember())
                .extracting(Member::getId).isEqualTo(member.getId());
        assertThat(result.getLanguage()).isEqualTo(language);
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getOrders())
                .extracting(RefactoringTodoOrder::getContent)
                .containsExactly("메소드 중복을 없애 주십시오.", "개 소리 좀 안 나게 해라!!!!");
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 게시글 id가 주어지면 id에 해당하는 리팩토링 대상 코드 한 건을 반환한다.")
    void givenRefactoringTodoId_whenFindOneByGivenId_ThenReturnsOneRefactoringTodo() {
        // given
        Long savedId = this.saveMemberAndSaveRefactoringTodo();

        // when
        RefactoringTodoResponse response = refactoringTodoService.findOneById(savedId);

        // then
        assertThat(response.getId()).isEqualTo(savedId);
        assertThat(response.getMember().getId()).isEqualTo("test@gmail.com");
        assertThat(response.getLanguage()).isEqualTo("JAVA");
        assertThat(response.getCode()).isEqualTo(
                "    private String signInMember() {\n" +
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
                        "    }"
        );
        assertThat(response.getDescription()).isEqualTo("유효한 새 게시글이 제공되면 글이 정상적으로 등록된다.");
        assertThat(response.getBestPractice()).isNull(); // TODO: BestPractice 구현 후 추가
        assertThat(response.getOrders())
                .extracting(RefactoringTodoOrderResponse::getContent)
                .containsExactly("메소드 중복을 없애 주십시오.", "개 소리 좀 안 나게 해라!!!!");
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 존재하지 않는 게시글 id로 리팩토링 대상 코드를 조회하려 시도하면 예외를 반환한다.")
    void givenNonExistingRefactoringTodoId_whenFindOneByGivenId_ThenThrowsException() {
        // given
        Long savedId = -1L;

        // when & then
        assertThatThrownBy(() -> refactoringTodoService.findOneById(savedId))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessage("there is no RefactoringTodo with id -1");
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - null id로 리팩토링 대상 코드를 조회하려 시도하면 예외를 반환한다.")
    void givenNullId_whenFindOneByGivenId_ThenThrowsException() {
        // given
        Long savedId = null;

        // when & then
        assertThatThrownBy(() -> refactoringTodoService.findOneById(savedId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you tried to find a RefactoringTodo with NULL id");
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 게시글 id가 주어지면 id에 해당하는 리팩토링 대상 코드 한 건을 삭제한다.")
    void givenRefactoringTodoId_whenDeleteOneByGivenId_ThenSuccess() {
        // given
        Long savedId = this.saveMemberAndSaveRefactoringTodo();

        // when
        refactoringTodoService.deleteOneById(savedId);

        // then
        assertThatThrownBy(() -> refactoringTodoService.findOneById(savedId))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessage("there is no RefactoringTodo with id " + savedId);
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 존재하지 않는 게시글 id로 리팩토링 대상 코드를 삭제 시도하면 예외를 반환한다.")
    void givenNonExistingRefactoringTodoId_whenDeleteOneByGivenId_ThenThrowsException() {
        // given
        Long savedId = -1L;

        // when & then
        assertThatThrownBy(() -> refactoringTodoService.deleteOneById(savedId))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - null id로 리팩토링 대상 코드를 삭제 시도하면 예외를 반환한다.")
    void givenNullId_whenDeleteOneByGivenId_ThenThrowsException() {
        // given
        Long savedId = null;

        // when & then
        assertThatThrownBy(() -> refactoringTodoService.deleteOneById(savedId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("you tried to find a RefactoringTodo with NULL id");
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 아무런 조건이 주어지지 않고 리팩토링 대상 코드 목록을 조회 시도하면, 10개 씩, 최신순으로 내림 차순 정렬되어 있다.")
    void givenNothing_whenFindRefactoringTodoList_thenReturnsListWithDefaultSetting() {
        // given
        Member member = this.signInMember();
        for (int i = 0; i < 100; i++) {
            saveRefactoringTodoWithMemberCondition(member);
        }
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        /*
        * 컨트롤러에서 @PageableDefault(size = 10, sort = “createdAt”, direction = Sort.Direction.DESC) Pageable pageable)
        * 옵션의 PageRequest가 들어올 예정이다.
        * */

        // when
        Page<RefactoringTodoResponse> resultList = refactoringTodoService.findList(pageable);

        // then
        assertThat(resultList).hasSize(10);

        Iterator<RefactoringTodoResponse> iterator = resultList.iterator();
        RefactoringTodoResponse next = null;
        while (iterator.hasNext()) {
            if (next == null) next = iterator.next();
            RefactoringTodoResponse next2 = iterator.next();
            assertThat(next.getCreatedAt())
                    .isAfter(next2.getCreatedAt());
            next = next2;
        }
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 최근에 작성한 작성일 순서대로 오름차순 정렬 옵션을 주면 리팩토링 대상 코드 10개를 해당 옵션에 맞게 정렬하여 조회한다.")
    void givenSortingOptionWithCreatedDateASC_whenFindRefactoringTodoList_thenReturnsListWithGivenConditions() {
        // given
        Member member = this.signInMember();
        for (int i = 0; i < 100; i++) {
            saveRefactoringTodoWithMemberCondition(member);
        }
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        /*
        * 컨트롤러에서 @PageableDefault(size = 10, sort = “createdAt”, direction = Sort.Direction.DESC) Pageable pageable)
        * 옵션의 PageRequest가 들어올 예정이다.
        * */

        // when
        Page<RefactoringTodoResponse> resultList = refactoringTodoService.findList(pageable);

        // then
        assertThat(resultList).hasSize(10);

        Iterator<RefactoringTodoResponse> iterator = resultList.iterator();
        RefactoringTodoResponse next = null;
        while (iterator.hasNext()) {
            if (next == null) next = iterator.next();
            RefactoringTodoResponse next2 = iterator.next();
            assertThat(next.getCreatedAt())
                    .isBefore(next2.getCreatedAt());
            next = next2;
        }
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 즐겨찾기 수가 많은 순서대로 내림차순 정렬 옵션을 주면 리팩토링 대상 코드 10개를 해당 옵션에 맞게 정렬하여 조회한다.")
    void givenSortingOptionWithNumberOfFavoritesDESC_whenFindRefactoringTodoList_thenReturnsListWithGivenConditions() {
        // given
        Member member = this.signInMember();

        for (int i = 0; i < 50; i++) {
            this.signInMemberWithEmailCondition("test" + i + "@gmail.com");
        }

        for (int i = 0; i < 30; i++) {
            Long savedRefactoringTodo = saveRefactoringTodoWithMemberCondition(member);

            int random = (int) (Math.random() * 50) + 1;
            for (int j = 0; j < random; j++) {
                Member follower = memberRepository.findById("test" + j + "@gmail.com").get();
                memberService.assignFavorite(savedRefactoringTodo, follower);
            }
            /*
            * 1~50 명 사이의 회원이 해당 리팩토링 대상 코드를 즐겨찾기 한다.
            * */
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "favoriteCount");
        /*
        * 컨트롤러에서 즐겨찾기 순 / 내림차순 옵션의 PageRequest가 들어올 예정이다.
        * */

        // when
        Page<RefactoringTodoResponse> resultList = refactoringTodoService.findList(pageable);

        // then
        assertThat(resultList).hasSize(10);
        Iterator<RefactoringTodoResponse> iterator = resultList.iterator();

        RefactoringTodoResponse next = null;
        while (iterator.hasNext()) {
            if (next == null) next = iterator.next();
            RefactoringTodoResponse next2 = iterator.next();
            assertThat(next.getFavoriteCount())
                    .isGreaterThanOrEqualTo(next2.getFavoriteCount());
            next = next2;
        }
    }

    @Test
    @DisplayName("리팩토링 대상 코드 - 즐겨찾기 수가 많은 순서대로 오름차순 정렬 옵션을 주면 리팩토링 대상 코드 10개를 해당 옵션에 맞게 정렬하여 조회한다.")
    void givenSortingOptionWithNumberOfFavoritesASC_whenFindRefactoringTodoList_thenReturnsListWithGivenConditions() {
        // given
        Member member = this.signInMember();

        for (int i = 0; i < 50; i++) {
            this.signInMemberWithEmailCondition("test" + i + "@gmail.com");
        }

        for (int i = 0; i < 30; i++) {
            Long savedRefactoringTodo = saveRefactoringTodoWithMemberCondition(member);

            int random = (int) (Math.random() * 50) + 1;
            for (int j = 0; j < random; j++) {
                Member follower = memberRepository.findById("test" + j + "@gmail.com").get();
                memberService.assignFavorite(savedRefactoringTodo, follower);
            }
            /*
            * 1~50 명 사이의 회원이 해당 리팩토링 대상 코드를 즐겨찾기 한다.
            * */
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "favoriteCount");
        /*
        * 컨트롤러에서 즐겨찾기 순 / 내림차순 옵션의 PageRequest가 들어올 예정이다.
        * */

        // when
        Page<RefactoringTodoResponse> resultList = refactoringTodoService.findList(pageable);

        // then
        assertThat(resultList).hasSize(10);
        Iterator<RefactoringTodoResponse> iterator = resultList.iterator();

        RefactoringTodoResponse next = null;
        while (iterator.hasNext()) {
            if (next == null) next = iterator.next();
            System.out.println("next = " + next);
            RefactoringTodoResponse next2 = iterator.next();
            assertThat(next.getFavoriteCount())
                    .isLessThanOrEqualTo(next2.getFavoriteCount());
            next = next2;
        }
    }

    /*
    * 고정된 ID의 회원 저장과 리팩토링 대상 코드 저장을 함께 하므로,
    * 동일한 트랜잭션에서 해당 코드를 여러 번 실행하면 예외가 발생한다.
    * */
    private Long saveMemberAndSaveRefactoringTodo() {
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

        Long savedId = refactoringTodoService.saveRefactoringTodo(format);

        em.flush();
        em.clear();

        return savedId;
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
}
