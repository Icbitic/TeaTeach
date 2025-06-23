package org.bedrock.teateach.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTypeTest {

    @Test
    void testEnumValues() {
        // Verify the enum contains the expected values
        assertEquals(6, TaskType.values().length);
        assertNotNull(TaskType.CHAPTER_HOMEWORK);
        assertNotNull(TaskType.EXAM_QUIZ);
        assertNotNull(TaskType.VIDEO_WATCH);
        assertNotNull(TaskType.READING_MATERIAL);
        assertNotNull(TaskType.REPORT_UPLOAD);
        assertNotNull(TaskType.PRACTICE_PROJECT);
    }

    @Test
    void testEnumDescriptions() {
        // Verify each enum has the correct description
        assertEquals("章节作业", TaskType.CHAPTER_HOMEWORK.getDescription());
        assertEquals("试卷答题", TaskType.EXAM_QUIZ.getDescription());
        assertEquals("课程视频观看", TaskType.VIDEO_WATCH.getDescription());
        assertEquals("阅读教材/PPT", TaskType.READING_MATERIAL.getDescription());
        assertEquals("报告类文档上传", TaskType.REPORT_UPLOAD.getDescription());
        assertEquals("实践项目", TaskType.PRACTICE_PROJECT.getDescription());
    }

    @Test
    void testValueOf() {
        // Verify valueOf() works correctly
        assertEquals(TaskType.CHAPTER_HOMEWORK, TaskType.valueOf("CHAPTER_HOMEWORK"));
        assertEquals(TaskType.EXAM_QUIZ, TaskType.valueOf("EXAM_QUIZ"));
        assertEquals(TaskType.VIDEO_WATCH, TaskType.valueOf("VIDEO_WATCH"));
        assertEquals(TaskType.READING_MATERIAL, TaskType.valueOf("READING_MATERIAL"));
        assertEquals(TaskType.REPORT_UPLOAD, TaskType.valueOf("REPORT_UPLOAD"));
        assertEquals(TaskType.PRACTICE_PROJECT, TaskType.valueOf("PRACTICE_PROJECT"));
    }

    @Test
    void testValueOfInvalidValue() {
        // Verify that an invalid enum value throws an exception
        assertThrows(IllegalArgumentException.class, () -> TaskType.valueOf("INVALID_TYPE"));
    }
}
