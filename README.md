StockManger - 
A modern web-based solution for digitizing and managing the distribution of consumable products in university warehouses, developed as part of an academic project at Abdelhamid Ibn Badis University of Mostaganem.
📋 Table of Contents

About
Features
System Architecture
Technologies Used


🎯 About
StockManger is a comprehensive inventory management system designed to replace traditional paper-based procedures with a centralized electronic platform. The system addresses common challenges in university warehouse management including:

Frequent counting errors
Delayed stock updates
Lack of product movement traceability
Difficulty identifying high-consuming departments
Inability to generate reliable consumption reports

✨ Features
Core Functionality

Digital Request Submission: Online submission of consumable product requests by departments
Real-time Stock Management: Automatic stock level updates after each validated transaction
Digital Validation: Electronic approval workflow for release orders
Inventory Tracking: Complete traceability of product movements
Alert System: Configurable thresholds for restocking notifications
Reporting & Analytics: Periodic consumption reports by service, product, or time period

User-Specific Features
Administrator

User account management (create, modify, deactivate)
System activity logs and usage statistics
Anomaly detection

Requester (Department Head)

Submit new product requests
Real-time request status tracking
Access to department request history

Warehouse Manager (Magasinier)

View and process submitted requests
Product catalog management (add, modify, delete)
Stock availability verification
Preparation and generation of release orders
PDF document generation

General Secretary (SG)

Final approval/rejection of requests
Stock overview consultation
Access to periodic reports and analytics

🏗️ System Architecture
The system follows a layered architecture with clear separation of concerns:
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (Web Interface - NetBeans/JSP)        │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│         Business Logic Layer            │
│        (Java Application Logic)         │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│          Data Access Layer              │
│            (MySQL Database)             │
└─────────────────────────────────────────┘
🛠️ Technologies Used

Backend: Java (Object-Oriented Programming)
Frontend: HTML5, CSS, JavaScript, JSP
Database: MySQL
IDE: Apache NetBeans
Modeling: UML (Unified Modeling Language)
Design Tool: Eclipse Papyrus
Methodology: RUP (Rational Unified Process)
Version Control: Git

📦 Installation
Prerequisites

JDK 8 or higher
Apache NetBeans IDE
MySQL Server 5.7+
Git

Setup Steps

Clone the repository

bash   git clone git@github.com:moatamen12/Store-management.git
   cd Store-management

Database Configuration

bash   # Create the database
   mysql -u root -p
   CREATE DATABASE stockmanger;
   USE stockmanger;
   SOURCE database/schema.sql;

Configure Database Connection
Update the database connection settings in your configuration file:

properties   db.url=jdbc:mysql://localhost:3306/stockmanger
   db.username=your_username
   db.password=your_password

Open Project in NetBeans

Launch NetBeans IDE
File → Open Project
Navigate to the cloned repository
Select the project


Build and Run

Right-click on the project
Select "Clean and Build"
Click "Run" to start the application
