package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Career;

public class CareerFormat {
    public CareerFormat(String company, int months) {
        this.company = company;
        this.months = months;
    }

    public static CareerFormat of(String company, int months) {
        return new CareerFormat(company, months);
    }

    public static Career toEntity(CareerFormat dto) {
        return new Career(
                null,
                null,
                dto.company,
                dto.months
        );
    }

    private String company;
    private int months;
}
