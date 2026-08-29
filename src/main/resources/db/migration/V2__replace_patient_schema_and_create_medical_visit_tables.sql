DROP TABLE patients;

CREATE TABLE medecins (
    code_med VARCHAR(30) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    grade VARCHAR(100) NOT NULL,

    CONSTRAINT pk_medecins
        PRIMARY KEY (code_med)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE patients (
    code_pat VARCHAR(30) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    sexe VARCHAR(20) NOT NULL,
    adresse VARCHAR(255) NOT NULL,

    CONSTRAINT pk_patients
        PRIMARY KEY (code_pat)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE visiter (
    code_med VARCHAR(30) NOT NULL,
    code_pat VARCHAR(30) NOT NULL,
    visit_date DATE NOT NULL,

    CONSTRAINT pk_visiter
        PRIMARY KEY (code_med, code_pat, visit_date),

    CONSTRAINT fk_visiter_medecin
        FOREIGN KEY (code_med)
        REFERENCES medecins (code_med),

    CONSTRAINT fk_visiter_patient
        FOREIGN KEY (code_pat)
        REFERENCES patients (code_pat)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
