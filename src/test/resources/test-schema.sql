DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS vouchers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    phone_number VARCHAR(20) UNIQUE,
    role VARCHAR(50),
    balance NUMERIC(15, 2),
    account_status BOOLEAN,
    avatar_path VARCHAR(255),
    email VARCHAR(255),
    provider VARCHAR(255)
);

CREATE TABLE vouchers (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
    tour_type VARCHAR(50) NOT NULL,
    transfer_type VARCHAR(50) NOT NULL,
    hotel_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    arrival_date DATE NOT NULL,
    eviction_date DATE NOT NULL,
    hot_status BOOLEAN,
    deleted_at DATE
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID,
    last_customer UUID,
    voucher_id UUID NOT NULL,
    order_price NUMERIC(15, 2) NOT NULL,
    voucher_arrival_date DATE,
    voucher_eviction_date DATE,
    order_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    cancel_reason TEXT,
    call_back BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(255),
    expiry_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
