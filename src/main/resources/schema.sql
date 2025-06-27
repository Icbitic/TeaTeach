create table teateach.courses
(
    id          bigint auto_increment
        primary key,
    course_code varchar(100)                       not null comment 'Unique code for the course (e.g., CS101)',
    course_name varchar(255)                       not null,
    instructor  varchar(255)                       null,
    credits     double                             null,
    hours       int                                null comment 'Total learning hours for the course',
    description text                               null,
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint course_code
        unique (course_code)
);

create table teateach.ability_points
(
    id                             bigint auto_increment
        primary key,
    name                           varchar(255)                       not null,
    description                    text                               null,
    course_id                      bigint                             null,
    prerequisite_ability_point_ids text                               null comment 'JSON array of IDs of prerequisite APs',
    related_ability_point_ids      text                               null comment 'JSON array of IDs of related APs',
    created_at                     datetime default CURRENT_TIMESTAMP null,
    updated_at                     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint ability_points_ibfk_1
        foreign key (course_id) references teateach.courses (id)
            on delete cascade
);

create index idx_ability_points_course_id
    on teateach.ability_points (course_id);

create index idx_courses_code
    on teateach.courses (course_code);

create table teateach.knowledge_points
(
    id                               bigint auto_increment
        primary key,
    name                             varchar(255)                       not null comment 'Name of the knowledge point',
    brief_description                text                               null,
    detailed_content                 text                               null comment 'More extensive content or link to learning material',
    course_id                        bigint                             null,
    difficulty_level                 varchar(50)                        null comment 'e.g., BEGINNER, INTERMEDIATE, ADVANCED',
    prerequisite_knowledge_point_ids text                               null comment 'JSON array of IDs of prerequisite KPs',
    related_knowledge_point_ids      text                               null comment 'JSON array of IDs of related KPs',
    created_at                       datetime default CURRENT_TIMESTAMP null,
    updated_at                       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint knowledge_points_ibfk_1
        foreign key (course_id) references teateach.courses (id)
            on delete set null
);

create index idx_knowledge_points_course_id
    on teateach.knowledge_points (course_id);

create table teateach.learning_tasks
(
    id                bigint auto_increment
        primary key,
    course_id         bigint                             not null,
    task_name         varchar(255)                       not null,
    task_type         varchar(50)                        not null comment 'e.g., CHAPTER_HOMEWORK, EXAM_QUIZ, VIDEO_WATCH, REPORT_UPLOAD',
    task_description  text                               null,
    deadline          datetime                           null,
    submission_method varchar(100)                       null comment 'e.g., upload, online_quiz, text_input',
    created_at        datetime default CURRENT_TIMESTAMP null,
    updated_at        datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create index idx_tasks_course_id
    on teateach.learning_tasks (course_id);

create table teateach.playback_vectors
(
    id             bigint auto_increment
        primary key,
    student_id     bigint    not null,
    resource_id    bigint    not null,
    playback_data  json      null,
    video_duration int       null,
    last_updated   timestamp null
);

create index idx_student_resource
    on teateach.playback_vectors (student_id, resource_id);

create table teateach.questions
(
    id                   bigint auto_increment
        primary key,
    question_text        text                               not null,
    question_type        varchar(50)                        not null comment 'e.g., SINGLE_CHOICE, MULTIPLE_CHOICE, SHORT_ANSWER, PROGRAMMING',
    options              text                               null comment 'JSON string for multiple choice options',
    correct_answer       text                               null comment 'JSON string for correct answer(s)',
    explanation          text                               null,
    difficulty           varchar(50)                        null comment 'e.g., EASY, MEDIUM, HARD',
    knowledge_point_ids  text                               null comment 'JSON array of IDs of associated knowledge points',
    programming_language varchar(50)                        null,
    template_code        text                               null,
    test_cases           text                               null comment 'JSON string for programming question test cases',
    created_at           datetime default CURRENT_TIMESTAMP null,
    updated_at           datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create index idx_questions_type_difficulty
    on teateach.questions (question_type, difficulty);

create table teateach.resources
(
    id            bigint auto_increment
        primary key,
    resource_name varchar(255)                       not null,
    file_path     varchar(512)                       not null comment 'Path to stored file or external URL',
    file_type     varchar(50)                        not null comment 'e.g., pdf, pptx, mp4, docx',
    uploaded_by   bigint                             null comment 'ID of the user (e.g., teacher) who uploaded it',
    file_size     bigint                             null comment 'Size of the file in bytes',
    description   text                               null,
    created_at    datetime default CURRENT_TIMESTAMP null,
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table teateach.students
(
    id            bigint auto_increment
        primary key,
    student_id    varchar(50)                        not null comment 'Unique student identification number',
    name          varchar(255)                       not null,
    email         varchar(255)                       not null,
    major         varchar(255)                       null,
    date_of_birth date                               null,
    created_at    datetime default CURRENT_TIMESTAMP null,
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint email
        unique (email),
    constraint student_id
        unique (student_id)
);

create table teateach.course_enrollments
(
    id              bigint auto_increment
        primary key,
    course_id       bigint                                                           not null,
    student_id      bigint                                                           not null,
    enrollment_date timestamp                              default CURRENT_TIMESTAMP null,
    status          enum ('ACTIVE', 'INACTIVE', 'DROPPED') default 'ACTIVE'          null,
    created_at      timestamp                              default CURRENT_TIMESTAMP null,
    updated_at      timestamp                              default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint unique_course_student
        unique (course_id, student_id),
    constraint course_enrollments_ibfk_1
        foreign key (course_id) references teateach.courses (id)
            on delete cascade,
    constraint course_enrollments_ibfk_2
        foreign key (student_id) references teateach.students (id)
            on delete cascade
);

create index idx_course_id
    on teateach.course_enrollments (course_id);

create index idx_enrollment_date
    on teateach.course_enrollments (enrollment_date);

create index idx_student_id
    on teateach.course_enrollments (student_id);

create table teateach.grade_analysis
(
    id            bigint auto_increment
        primary key,
    student_id    varchar(50)                        not null,
    course_id     bigint                             not null,
    overall_grade double                             null comment 'Overall calculated grade for the student in this course',
    task_grades   text                               null comment 'JSON string of task grades (e.g., {"Quiz 1": 85.0, "Report": 92.5})',
    grade_trend   text                               null comment 'JSON string of grade changes over time (e.g., {"2024-01-15": 70.5, "2024-02-20": 78.0})',
    created_at    datetime default CURRENT_TIMESTAMP null,
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint student_id
        unique (student_id, course_id),
    constraint grade_analysis_students_student_id_fk
        foreign key (student_id) references teateach.students (student_id)
);

create index course_id
    on teateach.grade_analysis (course_id);

create index idx_grades_student_course
    on teateach.grade_analysis (student_id, course_id);

create table teateach.student_task_submissions
(
    id                 bigint auto_increment
        primary key,
    task_id            bigint                             not null,
    student_id         varchar(50)                        not null,
    submission_content text                               null comment 'Path to file, text answer, or JSON data',
    submission_time    datetime                           not null,
    score              double                             null comment 'Nullable until graded',
    feedback           text                               null comment 'Teacher feedback or automated feedback',
    completion_status  int      default 0                 not null comment '0: Not Started, 1: In Progress, 2: Submitted, 3: Graded',
    created_at         datetime default CURRENT_TIMESTAMP null,
    updated_at         datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint task_id
        unique (task_id, student_id),
    constraint student_task_submissions_ibfk_1
        foreign key (task_id) references teateach.learning_tasks (id)
            on delete cascade,
    constraint student_task_submissions_students_student_id_fk
        foreign key (student_id) references teateach.students (student_id)
);

create index idx_submissions_student_task
    on teateach.student_task_submissions (student_id, task_id);

create index idx_students_email
    on teateach.students (email);

create table teateach.submission_files
(
    id               bigint auto_increment
        primary key,
    submission_id    bigint                             not null,
    file_name        varchar(255)                       not null comment 'Original file name',
    stored_file_name varchar(255)                       not null comment 'UUID-based stored file name',
    file_path        varchar(512)                       not null comment 'Path to stored file',
    file_type        varchar(50)                        not null comment 'File extension (e.g., pdf, docx, jpg)',
    file_size        bigint                             null comment 'Size of the file in bytes',
    mime_type        varchar(100)                       null comment 'MIME type of the file',
    created_at       datetime default CURRENT_TIMESTAMP null,
    updated_at       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint fk_submission_files_submission_id
        foreign key (submission_id) references teateach.student_task_submissions (id)
            on delete cascade
);

create index idx_submission_files_submission_id
    on teateach.submission_files (submission_id);

create table teateach.task_resources
(
    id          bigint auto_increment
        primary key,
    task_id     bigint                              not null,
    resource_id bigint                              not null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    updated_at  timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_task_resource
        unique (task_id, resource_id),
    constraint fk_task_resources_resource_id
        foreign key (resource_id) references teateach.resources (id)
            on delete cascade,
    constraint fk_task_resources_task_id
        foreign key (task_id) references teateach.learning_tasks (id)
            on delete cascade
);

create index idx_task_resources_resource_id
    on teateach.task_resources (resource_id);

create index idx_task_resources_task_id
    on teateach.task_resources (task_id);

create table teateach.teachers
(
    id            bigint auto_increment
        primary key,
    teacher_id    varchar(50)                         not null,
    name          varchar(255)                        not null,
    email         varchar(255)                        not null,
    department    varchar(255)                        null,
    date_of_birth date                                null,
    created_at    timestamp default CURRENT_TIMESTAMP null,
    updated_at    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint email
        unique (email),
    constraint teacher_id
        unique (teacher_id)
);

create table teateach.test_papers
(
    id                bigint auto_increment
        primary key,
    paper_name        varchar(255)                       not null,
    course_id         bigint                             not null,
    instructor_id     bigint                             null comment 'ID of the user (teacher) who created this paper',
    question_ids      text                               not null comment 'JSON array of IDs of questions included in this paper',
    total_score       double                             null,
    duration_minutes  int                                null,
    generation_method varchar(100)                       null comment 'e.g., RANDOM, BY_KNOWLEDGE_POINT, BY_DIFFICULTY',
    created_at        datetime default CURRENT_TIMESTAMP null,
    updated_at        datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint test_papers_ibfk_1
        foreign key (course_id) references teateach.courses (id)
            on delete cascade
);

create index idx_test_papers_course_id
    on teateach.test_papers (course_id);

create table teateach.users
(
    id                 bigint auto_increment
        primary key,
    username           varchar(50)                          not null,
    password           varchar(255)                         not null,
    email              varchar(255)                         not null,
    user_type          varchar(20)                          not null,
    reference_id       varchar(50)                          not null,
    active             tinyint(1) default 1                 null,
    reset_token        varchar(255)                         null,
    reset_token_expiry timestamp                            null,
    created_at         timestamp  default CURRENT_TIMESTAMP null,
    updated_at         timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint email
        unique (email),
    constraint username
        unique (username)
);

