CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGSERIAL,
    description VARCHAR(255) NOT NULL,
    requestor_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_request PRIMARY KEY (id),
    CONSTRAINT fk_request_requestor FOREIGN KEY (requestor_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ch_request_created_not_after_current CHECK (created <= CURRENT_TIMESTAMP)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    is_available BOOLEAN,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,
    CONSTRAINT pk_item PRIMARY KEY (id),
    CONSTRAINT fk_item_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE SET NULL ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (id),
    CONSTRAINT fk_booking_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_booking_booker FOREIGN KEY (booker_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ch_booking_start_date_after_current CHECK (start_date > CURRENT_TIMESTAMP),
    CONSTRAINT ch_booking_end_date_after_current CHECK (end_date > CURRENT_TIMESTAMP),
    CONSTRAINT ch_booking_end_date_after_start_date CHECK (end_date > start_date)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL,
    text VARCHAR(2000) NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_comment PRIMARY KEY (id),
    CONSTRAINT fk_comment_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);