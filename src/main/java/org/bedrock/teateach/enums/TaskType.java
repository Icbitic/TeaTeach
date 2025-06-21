package org.bedrock.teateach.enums;

import lombok.Getter;

@Getter
public enum TaskType {
    CHAPTER_HOMEWORK("章节作业"),
    EXAM_QUIZ("试卷答题"),
    VIDEO_WATCH("课程视频观看"),
    READING_MATERIAL("阅读教材/PPT"),
    REPORT_UPLOAD("报告类文档上传"),
    PRACTICE_PROJECT("实践项目");

    private final String description;

    TaskType(String description) {
        this.description = description;
    }

}
