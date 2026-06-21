CREATE TABLE outbox_events (
                               id              UUID            PRIMARY KEY,
                               aggregate_id    UUID            NOT NULL,
                               event_type      VARCHAR(100)    NOT NULL,
                               payload         TEXT            NOT NULL,
                               correlation_id  UUID            NOT NULL,
                               published       BOOLEAN         NOT NULL    DEFAULT FALSE,
                               created_at      TIMESTAMP       NOT NULL,
                               published_at    TIMESTAMP,

                               CONSTRAINT ck_published_at_required
                                   CHECK (
                                       published = FALSE OR published_at IS NOT NULL
                                       )
);

CREATE INDEX idx_outbox_events_published
    ON outbox_events (published)
    WHERE published = FALSE;

CREATE INDEX idx_outbox_events_aggregate_id
    ON outbox_events (aggregate_id);

CREATE INDEX idx_outbox_events_correlation_id
    ON outbox_events (correlation_id);