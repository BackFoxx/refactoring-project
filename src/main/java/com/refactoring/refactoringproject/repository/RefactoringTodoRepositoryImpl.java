package com.refactoring.refactoringproject.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.refactoring.refactoringproject.dto.QRefactoringTodoResponse;
import com.refactoring.refactoringproject.dto.RefactoringTodoResponse;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;

import static com.refactoring.refactoringproject.entity.QFavorite.favorite;
import static com.refactoring.refactoringproject.entity.QRefactoringTodo.refactoringTodo;

public class RefactoringTodoRepositoryImpl implements RefactoringTodoSearchRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public RefactoringTodoRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<RefactoringTodoResponse> findListWithFavoriteCount(Pageable pageable) {
        JPAQuery<RefactoringTodoResponse> query = queryFactory
                .select(new QRefactoringTodoResponse(
                        refactoringTodo.id,
                        refactoringTodo.language,
                        refactoringTodo.code,
                        refactoringTodo.description,
                        ExpressionUtils.as(countFavorites(), "favoriteCount"),
                        refactoringTodo.createdAt,
                        refactoringTodo.modifiedAt
                ))
                .from(refactoringTodo);

        for (Sort.Order order : pageable.getSort()) {
            query.orderBy(
                    new OrderSpecifier<>(
                            order.isAscending() ? Order.ASC : Order.DESC, Expressions.stringPath(order.getProperty())
                    )
            );
        }

        query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return new PageImpl<>(query.fetch());
    }

    private JPQLQuery<Long> countFavorites() {
        JPQLQuery<Long> countFavorites = JPAExpressions
                .select(favorite.count())
                .from(favorite)
                .where(favorite.refactoringTodo.id.eq(refactoringTodo.id));
        return countFavorites;
    }
}
