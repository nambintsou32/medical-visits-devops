CREATE TABLE patients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT NOT NULL DEFAULT 0,
    medical_record_number VARCHAR(30) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    phone VARCHAR(30) NULL,

    CONSTRAINT pk_patients
        PRIMARY KEY (id),

    CONSTRAINT uk_patients_medical_record_number
        UNIQUE (medical_record_number)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
