INSERT INTO lecture_halls (id, name)
SELECT 'd43bcd22-968d-4bd9-9a1c-42b92d923501'::UUID, 'E-Fac Main Hall'
WHERE NOT EXISTS (
    SELECT 1 FROM lecture_halls WHERE LOWER(name) = LOWER('E-Fac Main Hall')
);

INSERT INTO lecture_halls (id, name)
SELECT 'd43bcd22-968d-4bd9-9a1c-42b92d923502'::UUID, 'Computer Lab 01'
WHERE NOT EXISTS (
    SELECT 1 FROM lecture_halls WHERE LOWER(name) = LOWER('Computer Lab 01')
);

INSERT INTO lecture_halls (id, name)
SELECT 'd43bcd22-968d-4bd9-9a1c-42b92d923503'::UUID, 'Lecture Hall A'
WHERE NOT EXISTS (
    SELECT 1 FROM lecture_halls WHERE LOWER(name) = LOWER('Lecture Hall A')
);
