CREATE TABLE IF NOT EXISTS frame_preference(
       user_id BIGINT PRIMARY KEY,
       color INTEGER,
       enabled BOOLEAN NOT NULL
)