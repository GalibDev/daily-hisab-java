CREATE TABLE IF NOT EXISTS users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(80) NOT NULL,
 email VARCHAR(190) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS sessions (
 token_hash VARCHAR(64) PRIMARY KEY,
 user_id BIGINT NOT NULL,
 expires_at TIMESTAMP NOT NULL,
 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS categories (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 name VARCHAR(60) NOT NULL,
 type VARCHAR(7) NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
 UNIQUE (user_id, name, type),
 UNIQUE (id, user_id),
 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS transactions (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 category_id BIGINT NOT NULL,
 amount DECIMAL(14,2) NOT NULL CHECK (amount > 0),
 occurred_on DATE NOT NULL,
 note VARCHAR(250) NOT NULL DEFAULT '',
 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
 FOREIGN KEY (category_id, user_id) REFERENCES categories(id, user_id),
 INDEX idx_transactions_user_date (user_id, occurred_on)
);
