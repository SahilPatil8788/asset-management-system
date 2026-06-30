-- Database Schema for Asset Management System
CREATE DATABASE IF NOT EXISTS asset_db;
USE asset_db;

-- Drop tables in reverse order of dependencies to avoid foreign key violations
DROP TABLE IF EXISTS Transaction;
DROP TABLE IF EXISTS Asset;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Admin;

-- Admin Table
CREATE TABLE Admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Employee Table
CREATE TABLE Employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Asset Table
CREATE TABLE Asset (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'Available',
    price DOUBLE NOT NULL,
    purchase_date DATE NOT NULL,
    CONSTRAINT chk_status CHECK (status IN ('Available', 'Borrowed', 'Maintenance')),
    CONSTRAINT chk_price CHECK (price >= 0)
);

-- Transaction Table
CREATE TABLE Transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    employee_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) DEFAULT 'Borrowed',
    CONSTRAINT chk_trans_status CHECK (status IN ('Borrowed', 'Returned')),
    FOREIGN KEY (asset_id) REFERENCES Asset(id) ON DELETE RESTRICT,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);
