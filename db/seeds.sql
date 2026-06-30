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

-- Insert Assets
INSERT INTO Asset (name, category, serial_number, status, price, purchase_date) VALUES
('Dell XPS 15', 'Laptop', 'SN-DEL-001', 'Available', 1500.00, '2026-01-15'),
('MacBook Pro 16', 'Laptop', 'SN-MAC-002', 'Available', 2400.00, '2026-02-10'),
('iPad Pro 12.9', 'Tablet', 'SN-IPA-003', 'Available', 899.99, '2026-03-01'),
('Dell UltraSharp 27', 'Monitor', 'SN-MON-004', 'Available', 450.00, '2026-04-05'),
('iPhone 15 Pro', 'Phone', 'SN-PHN-005', 'Available', 999.99, '2026-05-20'),
('ThinkPad T14', 'Laptop', 'SN-THI-006', 'Maintenance', 1200.00, '2026-01-20');
