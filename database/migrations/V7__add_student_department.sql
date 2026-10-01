ALTER TABLE students ADD COLUMN department VARCHAR(20);

ALTER TABLE students ADD CONSTRAINT students_department_check
    CHECK (department IN ('ELEC', 'COM', 'MENA', 'MECH', 'CIVIL'));
