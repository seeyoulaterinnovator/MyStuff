CREATE TABLE TEST_LOG
(
    id SERIAL PRIMARY KEY,
    created TIMESTAMP DEFAULT now(),
    log text
);