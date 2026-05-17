-- Reference schema (Hibernate ddl-auto=update applies changes at runtime).
-- For production PostgreSQL you may switch to Flyway/Liquibase.

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(320) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notes (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     VARCHAR(10000) NOT NULL,
    owner_id    BIGINT NOT NULL REFERENCES users(id),
    archived    BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS note_shares (
    id                   BIGSERIAL PRIMARY KEY,
    note_id              BIGINT NOT NULL REFERENCES notes(id),
    shared_with_user_id  BIGINT NOT NULL REFERENCES users(id),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (note_id, shared_with_user_id)
);

CREATE INDEX IF NOT EXISTS idx_notes_owner ON notes(owner_id);
CREATE INDEX IF NOT EXISTS idx_notes_deleted_at ON notes(deleted_at);
CREATE INDEX IF NOT EXISTS idx_note_shares_user ON note_shares(shared_with_user_id);
