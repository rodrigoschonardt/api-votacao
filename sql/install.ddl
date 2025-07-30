CREATE TABLE topics (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOt NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    cpf VARCHAR(14) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (cpf)
);

CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    vote_option INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (user_id, session_id)
);

CREATE INDEX idx_voting_sessions_topic_id ON sessions(topic_id);
CREATE INDEX idx_votes_user_id ON votes(user_id);
CREATE INDEX idx_votes_session_id ON votes(session_id);
