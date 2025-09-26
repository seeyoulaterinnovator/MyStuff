DELIMITER $$

CREATE PROCEDURE safe_drop_index(IN tbl VARCHAR(64), IN idx VARCHAR(64))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.statistics
               WHERE table_schema = DATABASE()
                 AND table_name = tbl
                 AND index_name = idx) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' DROP INDEX ', idx);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE safe_drop_fk(IN tbl VARCHAR(64), IN fk VARCHAR(64))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.table_constraints
               WHERE table_schema = DATABASE()
                 AND table_name = tbl
                 AND constraint_name = fk
                 AND constraint_type = 'FOREIGN KEY') THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' DROP FOREIGN KEY ', fk);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE safe_drop_column(IN tbl VARCHAR(64), IN col VARCHAR(64))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.columns
               WHERE table_schema = DATABASE()
                 AND table_name = tbl
                 AND column_name = col) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' DROP COLUMN ', col);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE safe_drop_table(IN tbl VARCHAR(64))
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_schema = DATABASE()
                 AND table_name = tbl) THEN
        SET @s = CONCAT('DROP TABLE ', tbl);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;