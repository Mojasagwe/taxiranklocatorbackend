-- Starter taxi ranks (previously insert_rank1..5.sql). Safe to re-run: existing codes are skipped.

INSERT INTO taxi_ranks (name, code, description, address, city, province, latitude, longitude, contact_phone, contact_email, operating_hours, capacity, is_active, created_at, updated_at) VALUES ('Central Taxi Rank', 'CTR001', 'Main taxi rank in the central business district', '123 Main Street', 'Johannesburg', 'Gauteng', -26.2041, 28.0473, '+27123456789', 'central@taxirank.com', '05:00-22:00', 50, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO taxi_ranks (name, code, description, address, city, province, latitude, longitude, contact_phone, contact_email, operating_hours, capacity, is_active, created_at, updated_at) VALUES ('Sandton City Rank', 'SCR001', 'Premium taxi rank serving the Sandton business area', '25 Maude Street', 'Sandton', 'Gauteng', -26.1067, 28.0567, '+27123456790', 'sandton@taxirank.com', '05:30-21:30', 35, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO taxi_ranks (name, code, description, address, city, province, latitude, longitude, contact_phone, contact_email, operating_hours, capacity, is_active, created_at, updated_at) VALUES ('Pretoria Central Rank', 'PCR001', 'Main taxi rank in Pretoria CBD', '15 Church Square', 'Pretoria', 'Gauteng', -25.7461, 28.1881, '+27123456791', 'pretoria@taxirank.com', '04:30-22:00', 45, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO taxi_ranks (name, code, description, address, city, province, latitude, longitude, contact_phone, contact_email, operating_hours, capacity, is_active, created_at, updated_at) VALUES ('Cape Town Station', 'CTS001', 'Cape Town central station taxi rank', '10 Adderley Street', 'Cape Town', 'Western Cape', -33.9249, 18.4241, '+27123456792', 'capetown@taxirank.com', '04:00-23:00', 60, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO taxi_ranks (name, code, description, address, city, province, latitude, longitude, contact_phone, contact_email, operating_hours, capacity, is_active, created_at, updated_at) VALUES ('Durban Beach Rank', 'DBR001', 'Beachfront taxi rank serving tourists and locals', '25 Marine Parade', 'Durban', 'KwaZulu-Natal', -29.8587, 31.0218, '+27123456793', 'durban@taxirank.com', '05:00-22:00', 40, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

