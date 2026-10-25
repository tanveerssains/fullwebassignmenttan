CREATE DATABASE IF NOT EXISTS employee_portal
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE employee_portal;

CREATE TABLE IF NOT EXISTS departments (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(80)  NOT NULL,
    location VARCHAR(120) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_name (name)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS projects (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    name   VARCHAR(100) NOT NULL,
    client VARCHAR(100) NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_name (name)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS employees (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    employee_code   VARCHAR(20)   NOT NULL,
    first_name      VARCHAR(60)   NOT NULL,
    last_name       VARCHAR(60)   NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    phone           VARCHAR(20)   NULL,
    designation     VARCHAR(100)  NOT NULL,
    salary          DECIMAL(12, 2) NOT NULL,
    date_of_joining DATE          NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    department_id   BIGINT        NOT NULL,
    project_id      BIGINT        NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_code (employee_code),
    UNIQUE KEY uk_employee_email (email),
    KEY idx_employee_department (department_id),
    KEY idx_employee_project (project_id),
    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_employee_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE SET NULL
) ENGINE = InnoDB;
