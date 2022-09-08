package com.refactoring.refactoringproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.EntityListeners;

@SpringBootApplication
public class RefactoringProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(RefactoringProjectApplication.class, args);
	}

}
