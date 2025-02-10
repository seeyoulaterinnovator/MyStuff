CREATE TABLE ispn_cache_sessions(
    id VARCHAR(768),
    data MEDIUMBLOB,
    timestamp BIGINT,
    segment INT,
    created TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY(id)
);