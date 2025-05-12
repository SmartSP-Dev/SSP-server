DROP TABLE IF EXISTS timetables;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS group_table;
DROP TABLE IF EXISTS routine_records;
DROP TABLE IF EXISTS routine;
DROP TABLE IF EXISTS quiz_questions;
DROP TABLE IF EXISTS quizzes;
DROP TABLE IF EXISTS study_records;
DROP TABLE IF EXISTS studies;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    university VARCHAR(100),
    department VARCHAR(100),
    profile_image VARCHAR(1000),
    everytime_url VARCHAR(1000),
    provider VARCHAR(100)
);

CREATE TABLE studies (
    study_id INT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(100),
    user_id INT NOT NULL,
    CONSTRAINT fk_study_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE study_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    study_id INT NOT NULL,
    date DATE,
    time INT,
    CONSTRAINT fk_record_study FOREIGN KEY (study_id)
        REFERENCES studies(study_id)
        ON DELETE CASCADE
);

CREATE TABLE quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255),
    keywords TEXT,
    question_type VARCHAR(255),
    summary TEXT,
    CONSTRAINT fk_quiz_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE quiz_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_title VARCHAR(255),
    question_content VARCHAR(255),
    quiz_number INT,
    correct_answer VARCHAR(255),
    incorrect_answer JSON,
    question_type VARCHAR(255),
    CONSTRAINT fk_question_quiz FOREIGN KEY (quiz_id)
        REFERENCES quizzes(id)
        ON DELETE CASCADE
);

CREATE TABLE routine (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255),
    is_active BOOLEAN,
    created_at DATE,
    deleted_at DATE,
    started_at DATE,
    CONSTRAINT fk_routine_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE routine_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    routine_id INT NOT NULL,
    user_id INT NOT NULL,
    date DATE,
    completed BOOLEAN,
    CONSTRAINT fk_record_routine FOREIGN KEY (routine_id)
        REFERENCES routine(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_record_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE group_table (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    leader_id INT NOT NULL,
    group_key VARCHAR(40) NOT NULL UNIQUE,
    created_at DATE NOT NULL,
    expires_at DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (leader_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE group_members (
    group_id INT NOT NULL,
    member_id INT NOT NULL,
    PRIMARY KEY (group_id, member_id),
    FOREIGN KEY (group_id) REFERENCES group_table(group_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE timetables (
    group_id INT NOT NULL,
    member_id INT NOT NULL,
    day_of_week ENUM('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN') NOT NULL,
    time_block TIME NOT NULL,
    weight INT DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (group_id, member_id, day_of_week, time_block),
    FOREIGN KEY (group_id) REFERENCES group_table(group_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users(user_id) ON DELETE CASCADE
);
