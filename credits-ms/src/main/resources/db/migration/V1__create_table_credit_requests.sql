CREATE TABLE credit_requests (
                                 id              UUID            PRIMARY KEY,
                                 cpf             VARCHAR(11)     NOT NULL,
                                 name            VARCHAR(255)    NOT NULL,
                                 income          NUMERIC(15, 2)  NOT NULL,
                                 value_request   NUMERIC(15, 2)  NOT NULL,
                                 term_months     INTEGER         NOT NULL,
                                 status          VARCHAR(20)     NOT NULL    DEFAULT 'PENDING',
                                 correlation_id  UUID            NOT NULL,
                                 created_at      TIMESTAMP       NOT NULL,

                                 CONSTRAINT ck_income_positive
                                     CHECK (income > 0),

                                 CONSTRAINT ck_value_request_positive
                                     CHECK (value_request > 0),

                                 CONSTRAINT ck_term_months_range
                                     CHECK (term_months BETWEEN 6 AND 360)
);

CREATE INDEX idx_credit_requests_cpf
    ON credit_requests (cpf);

CREATE INDEX idx_credit_requests_correlation_id
    ON credit_requests (correlation_id);

CREATE INDEX idx_credit_requests_status
    ON credit_requests (status);