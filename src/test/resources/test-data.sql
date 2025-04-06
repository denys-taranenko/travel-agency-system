INSERT INTO users (id, username, password, phone_number, role, balance, account_status, avatar_path, email, provider) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Admin', '$2a$10$xJwL5vxJv5v5v5v5v5v5vu', '0979711223', 'ADMIN', 0, FALSE, 'default-avatar.png', 'admin@example.com', 'TravelAgency'),
('550e8400-e29b-41d4-a716-446655440001', 'Manager', '$2a$10$xJwL5vxJv5v5v5v5v5v5vu', '0979722334', 'MANAGER', 0, FALSE, 'default-avatar.png', 'manager@example.com', 'TravelAgency'),
('550e8400-e29b-41d4-a716-446655440002', 'User', '$2a$10$xJwL5vxJv5v5v5v5v5v5vu', '0979722335', 'USER', 0, FALSE, 'default-avatar.png', 'user@example.com', 'TravelAgency');


INSERT INTO vouchers (id, title, description, price, tour_type, transfer_type, hotel_type, status, arrival_date, eviction_date, hot_status) VALUES
('660e8400-e29b-41d4-a716-446655440000', 'Thermal Spa Retreat', 'Relax in natural hot springs', 1000, 'HEALTH', 'TRAIN', 'THREE_STARS', 'AVAILABLE', '2025-05-01', '2025-05-11', TRUE);
