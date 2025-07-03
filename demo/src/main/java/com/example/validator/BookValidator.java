package com.example.validator;

import com.example.entity.Book;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class BookValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Book.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Book book = (Book) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "field.required", "Назва книги є обов'язковою");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "author", "field.required", "Ім'я автора є обов'язковим");

        if (book.getTitle() != null) {
            String title = book.getTitle().trim();
            if (title.length() < 3) {
                errors.rejectValue("title", "field.min.length", 
                    "Назва має містити мінімум 3 символи");
            }
            if (!Character.isUpperCase(title.charAt(0))) {
                errors.rejectValue("title", "field.capitalLetter", 
                    "Назва має починатися з великої літери");
            }
        }

        if (book.getAuthor() != null) {
            String author = book.getAuthor().trim();
            String[] words = author.split("\\s+");
            if (words.length != 2) {
                errors.rejectValue("author", "field.format", 
                    "Ім'я автора має складатися з двох слів");
            } else {
                if (!words[0].matches("[A-ZА-ЯІЇЄ][a-zа-яіїє]+") || 
                    !words[1].matches("[A-ZА-ЯІЇЄ][a-zа-яіїє]+")) {
                    errors.rejectValue("author", "field.format", 
                        "Кожне слово має починатися з великої літери");
                }
            }
        }
    }
}
