CREATE DATABASE IF NOT EXISTS task_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE task_db;

CREATE TABLE categories (
    id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(60) NOT NULL UNIQUE,
    color   VARCHAR(7)  NULL,
    active  TINYINT(1)  NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE tasks (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(150)  NOT NULL,
    description    VARCHAR(1000) NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'TODO',
    priority       VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM',
    due_date       DATE          NULL,
    category_id    BIGINT UNSIGNED NULL,
    owner_user_id  BIGINT UNSIGNED NOT NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    INDEX idx_tasks_owner_user_id (owner_user_id),
    INDEX idx_tasks_status (status)
) ENGINE=InnoDB;

