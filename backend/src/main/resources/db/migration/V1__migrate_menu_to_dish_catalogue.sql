-- V1: Introduce tenant-scoped Dish catalogue and migrate existing Menu data.
--
-- Existing menu rows and menu IDs are preserved so historical MealResponse
-- and MealRecord relationships remain valid.

-- ---------------------------------------------------------------------------
-- 1. Create the Dish catalogue.
-- ---------------------------------------------------------------------------

CREATE TABLE dishes (
    dish_id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    active BIT(1) NOT NULL,
    category ENUM('DAL','RICE','SABJI','SWEET') NOT NULL,
    mess_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,

    PRIMARY KEY (dish_id),

    UNIQUE KEY uk_dishes_mess_normalized_name (mess_id, normalized_name),

    KEY idx_dishes_mess_category (mess_id, category),

    CONSTRAINT fk_dishes_mess
        FOREIGN KEY (mess_id)
        REFERENCES messes (mess_id)
);

-- ---------------------------------------------------------------------------
-- 2. Populate the Dish catalogue from existing Menu values.
--
-- normalized_name follows the application's normalization rule:
-- trim -> collapse repeated spaces -> lowercase.
--
-- A dish name belongs to one category. NOT EXISTS also protects the
-- tenant-wide normalized-name uniqueness constraint if legacy menu data
-- happens to contain the same normalized name in another category.
-- ---------------------------------------------------------------------------

INSERT INTO dishes
    (created_at, updated_at, active, category, mess_id, name, normalized_name)
SELECT
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    b'1',
    'SABJI',
    source.mess_id,
    source.name,
    source.normalized_name
FROM (
    SELECT
        mess_id,
        MIN(TRIM(REGEXP_REPLACE(sabji_one, '[[:space:]]+', ' '))) AS name,
        LOWER(TRIM(REGEXP_REPLACE(sabji_one, '[[:space:]]+', ' '))) AS normalized_name
    FROM menus
    WHERE sabji_one IS NOT NULL
      AND TRIM(sabji_one) <> ''
    GROUP BY
        mess_id,
        LOWER(TRIM(REGEXP_REPLACE(sabji_one, '[[:space:]]+', ' ')))

    UNION

    SELECT
        mess_id,
        MIN(TRIM(REGEXP_REPLACE(sabji_two, '[[:space:]]+', ' '))) AS name,
        LOWER(TRIM(REGEXP_REPLACE(sabji_two, '[[:space:]]+', ' '))) AS normalized_name
    FROM menus
    WHERE sabji_two IS NOT NULL
      AND TRIM(sabji_two) <> ''
    GROUP BY
        mess_id,
        LOWER(TRIM(REGEXP_REPLACE(sabji_two, '[[:space:]]+', ' ')))
) source
WHERE NOT EXISTS (
    SELECT 1
    FROM dishes d
    WHERE d.mess_id = source.mess_id
      AND d.normalized_name = source.normalized_name
);

INSERT INTO dishes
    (created_at, updated_at, active, category, mess_id, name, normalized_name)
SELECT
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    b'1',
    'DAL',
    source.mess_id,
    source.name,
    source.normalized_name
FROM (
    SELECT
        mess_id,
        MIN(TRIM(REGEXP_REPLACE(dal, '[[:space:]]+', ' '))) AS name,
        LOWER(TRIM(REGEXP_REPLACE(dal, '[[:space:]]+', ' '))) AS normalized_name
    FROM menus
    WHERE dal IS NOT NULL
      AND TRIM(dal) <> ''
    GROUP BY
        mess_id,
        LOWER(TRIM(REGEXP_REPLACE(dal, '[[:space:]]+', ' ')))
) source
WHERE NOT EXISTS (
    SELECT 1
    FROM dishes d
    WHERE d.mess_id = source.mess_id
      AND d.normalized_name = source.normalized_name
);

INSERT INTO dishes
    (created_at, updated_at, active, category, mess_id, name, normalized_name)
SELECT
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    b'1',
    'RICE',
    source.mess_id,
    source.name,
    source.normalized_name
FROM (
    SELECT
        mess_id,
        MIN(TRIM(REGEXP_REPLACE(rice, '[[:space:]]+', ' '))) AS name,
        LOWER(TRIM(REGEXP_REPLACE(rice, '[[:space:]]+', ' '))) AS normalized_name
    FROM menus
    WHERE rice IS NOT NULL
      AND TRIM(rice) <> ''
    GROUP BY
        mess_id,
        LOWER(TRIM(REGEXP_REPLACE(rice, '[[:space:]]+', ' ')))
) source
WHERE NOT EXISTS (
    SELECT 1
    FROM dishes d
    WHERE d.mess_id = source.mess_id
      AND d.normalized_name = source.normalized_name
);

INSERT INTO dishes
    (created_at, updated_at, active, category, mess_id, name, normalized_name)
SELECT
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    b'1',
    'SWEET',
    source.mess_id,
    source.name,
    source.normalized_name
FROM (
    SELECT
        mess_id,
        MIN(TRIM(REGEXP_REPLACE(sweet, '[[:space:]]+', ' '))) AS name,
        LOWER(TRIM(REGEXP_REPLACE(sweet, '[[:space:]]+', ' '))) AS normalized_name
    FROM menus
    WHERE sweet IS NOT NULL
      AND TRIM(sweet) <> ''
    GROUP BY
        mess_id,
        LOWER(TRIM(REGEXP_REPLACE(sweet, '[[:space:]]+', ' ')))
) source
WHERE NOT EXISTS (
    SELECT 1
    FROM dishes d
    WHERE d.mess_id = source.mess_id
      AND d.normalized_name = source.normalized_name
);

-- ---------------------------------------------------------------------------
-- 3. Add the new Dish reference columns as nullable during data migration.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    ADD COLUMN sabji_one_dish_id BIGINT NULL,
    ADD COLUMN sabji_two_dish_id BIGINT NULL,
    ADD COLUMN dal_dish_id BIGINT NULL,
    ADD COLUMN rice_dish_id BIGINT NULL,
    ADD COLUMN sweet_dish_id BIGINT NULL;

-- ---------------------------------------------------------------------------
-- 4. Resolve existing Menu strings to their Dish IDs.
-- ---------------------------------------------------------------------------

UPDATE menus m
JOIN dishes d
    ON d.mess_id = m.mess_id
   AND d.normalized_name =
       LOWER(TRIM(REGEXP_REPLACE(m.sabji_one, '[[:space:]]+', ' ')))
SET m.sabji_one_dish_id = d.dish_id
WHERE m.sabji_one IS NOT NULL
  AND TRIM(m.sabji_one) <> '';

UPDATE menus m
JOIN dishes d
    ON d.mess_id = m.mess_id
   AND d.normalized_name =
       LOWER(TRIM(REGEXP_REPLACE(m.sabji_two, '[[:space:]]+', ' ')))
SET m.sabji_two_dish_id = d.dish_id
WHERE m.sabji_two IS NOT NULL
  AND TRIM(m.sabji_two) <> '';

UPDATE menus m
JOIN dishes d
    ON d.mess_id = m.mess_id
   AND d.normalized_name =
       LOWER(TRIM(REGEXP_REPLACE(m.dal, '[[:space:]]+', ' ')))
SET m.dal_dish_id = d.dish_id
WHERE m.dal IS NOT NULL
  AND TRIM(m.dal) <> '';

UPDATE menus m
JOIN dishes d
    ON d.mess_id = m.mess_id
   AND d.normalized_name =
       LOWER(TRIM(REGEXP_REPLACE(m.rice, '[[:space:]]+', ' ')))
SET m.rice_dish_id = d.dish_id
WHERE m.rice IS NOT NULL
  AND TRIM(m.rice) <> '';

UPDATE menus m
JOIN dishes d
    ON d.mess_id = m.mess_id
   AND d.normalized_name =
       LOWER(TRIM(REGEXP_REPLACE(m.sweet, '[[:space:]]+', ' ')))
SET m.sweet_dish_id = d.dish_id
WHERE m.sweet IS NOT NULL
  AND TRIM(m.sweet) <> '';

-- ---------------------------------------------------------------------------
-- 5. sabji_one is mandatory in the application model.
--    Existing valid rows have now been resolved, so enforce NOT NULL.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    MODIFY COLUMN sabji_one_dish_id BIGINT NOT NULL;

-- ---------------------------------------------------------------------------
-- 6. Remove the legacy string columns.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    DROP COLUMN sabji_one,
    DROP COLUMN sabji_two,
    DROP COLUMN dal,
    DROP COLUMN rice,
    DROP COLUMN sweet;

-- ---------------------------------------------------------------------------
-- 7. Add indexes for the Dish references.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    ADD INDEX idx_menus_sabji_one_dish (sabji_one_dish_id),
    ADD INDEX idx_menus_sabji_two_dish (sabji_two_dish_id),
    ADD INDEX idx_menus_dal_dish (dal_dish_id),
    ADD INDEX idx_menus_rice_dish (rice_dish_id),
    ADD INDEX idx_menus_sweet_dish (sweet_dish_id);

-- ---------------------------------------------------------------------------
-- 8. Add Menu -> Dish foreign keys.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    ADD CONSTRAINT fk_menus_sabji_one_dish
        FOREIGN KEY (sabji_one_dish_id)
        REFERENCES dishes (dish_id),

    ADD CONSTRAINT fk_menus_sabji_two_dish
        FOREIGN KEY (sabji_two_dish_id)
        REFERENCES dishes (dish_id),

    ADD CONSTRAINT fk_menus_dal_dish
        FOREIGN KEY (dal_dish_id)
        REFERENCES dishes (dish_id),

    ADD CONSTRAINT fk_menus_rice_dish
        FOREIGN KEY (rice_dish_id)
        REFERENCES dishes (dish_id),

    ADD CONSTRAINT fk_menus_sweet_dish
        FOREIGN KEY (sweet_dish_id)
        REFERENCES dishes (dish_id)
