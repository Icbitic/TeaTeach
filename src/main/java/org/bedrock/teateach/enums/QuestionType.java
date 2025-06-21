package org.bedrock.teateach.enums;

import lombok.Getter;

@Getter
public enum QuestionType {
    SINGLE_CHOICE("单选题"),
    MULTIPLE_CHOICE("多选题"),
    FILL_IN_THE_BLANK("填空题"),
    SHORT_ANSWER("简答题"),
    PROGRAMMING("编程题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

}
