-- Seed Data for Asset Management System
USE asset_db;

-- Clear any existing data
DELETE FROM Transaction;
DELETE FROM Asset;
DELETE FROM Employee;
DELETE FROM Admin;

-- Insert Admins
INSERT INTO Admin (username, password, email) VALUES
('admin', 'admin123', 'admin@assets.com'),
('superadmin', 'super123', 'superadmin@assets.com');

-- Insert Employees
INSERT INTO Employee (name, email, department, password) VALUES
('Alice Smith', 'alice@assets.com', 'IT', 'alice123'),
('Bob Jones', 'bob@assets.com', 'HR', 'bob123'),
('Charlie Brown', 'charlie@assets.com', 'Finance', 'charlie123');

-- Insert Assets (Rich dataset with various categories and statuses)
INSERT INTO Asset (name, category, serial_number, status, price, purchase_date) VALUES
('Dell XPS 15', 'Laptop', 'SN-DEL-001', 'Available', 1500.00, '2026-01-15'),
('MacBook Pro 16', 'Laptop', 'SN-MAC-002', 'Available', 2400.00, '2026-02-10'),
('iPad Pro 12.9', 'Tablet', 'SN-IPA-003', 'Available', 899.99, '2026-03-01'),
('Dell UltraSharp 27', 'Monitor', 'SN-MON-004', 'Available', 450.00, '2026-04-05'),
('iPhone 15 Pro', 'Phone', 'SN-PHN-005', 'Available', 999.99, '2026-05-20'),
('ThinkPad T14', 'Laptop', 'SN-THI-006', 'Maintenance', 1200.00, '2026-01-20'),
('Logitech MX Master 3S', 'Accessory', 'SN-LOG-007', 'Available', 99.99, '2026-02-15'),
('Keychron K2 Keyboard', 'Accessory', 'SN-KEY-008', 'Available', 79.99, '2026-03-10'),
('Bose QuietComfort 45', 'Audio', 'SN-BOS-009', 'Available', 329.99, '2026-04-12'),
('Samsung 32\" Curved Monitor', 'Monitor', 'SN-SAM-010', 'Available', 299.99, '2026-05-01'),
('Sony WH-1000XM5', 'Audio', 'SN-SON-011', 'Available', 399.99, '2026-05-18'),
('Samsung Galaxy S24 Ultra', 'Phone', 'SN-SGL-012', 'Available', 1299.99, '2026-06-01'),
('Lenovo ThinkCentre M70q', 'Desktop', 'SN-LEN-013', 'Available', 699.99, '2026-03-25'),
('HP LaserJet Pro', 'Printer', 'SN-HPL-014', 'Available', 249.99, '2026-01-10'),
('Apple Watch Series 9', 'Accessory', 'SN-APW-015', 'Available', 399.99, '2026-04-20'),
('Seagate Backup Plus 5TB', 'Storage', 'SN-SEA-016', 'Available', 119.99, '2026-02-22');
