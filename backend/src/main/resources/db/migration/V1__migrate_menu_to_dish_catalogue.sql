-- V1: Introduce tenant-scoped Dish catalogue and migrate Menu structure.
--
-- Existing menu data is intentionally discarded because it is not required.
-- From this migration onward, menu dish values are stored through Dish IDs.

-- ---------------------------------------------------------------------------
-- 1. Create the Dish catalogue
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
-- 2. Remove existing Menu records.
--    Existing menu data is intentionally not preserved.
-- ---------------------------------------------------------------------------

DELETE FROM menus;

-- ---------------------------------------------------------------------------
-- 3. Add Dish foreign-key columns to menus.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    ADD COLUMN sabji_one_dish_id BIGINT NOT NULL,
    ADD COLUMN sabji_two_dish_id BIGINT NULL,
    ADD COLUMN dal_dish_id BIGINT NULL,
    ADD COLUMN rice_dish_id BIGINT NULL,
    ADD COLUMN sweet_dish_id BIGINT NULL;

-- ---------------------------------------------------------------------------
-- 4. Remove the old string-based dish columns.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    DROP COLUMN sabji_one,
    DROP COLUMN sabji_two,
    DROP COLUMN dal,
    DROP COLUMN rice,
    DROP COLUMN sweet;

-- ---------------------------------------------------------------------------
-- 5. Add indexes for the Dish foreign keys.
-- ---------------------------------------------------------------------------

ALTER TABLE menus
    ADD INDEX idx_menus_sabji_one_dish (sabji_one_dish_id),
    ADD INDEX idx_menus_sabji_two_dish (sabji_two_dish_id),
    ADD INDEX idx_menus_dal_dish (dal_dish_id),
    ADD INDEX idx_menus_rice_dish (rice_dish_id),
    ADD INDEX idx_menus_sweet_dish (sweet_dish_id);

-- ---------------------------------------------------------------------------
-- 6. Add foreign-key relationships from Menu to Dish.
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
        REFERENCES dishes (dish_id);
