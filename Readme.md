

## Folder Structure

The workspace contains the following structure:

- `src`: the folder to maintain source code
- `lib`: the folder to maintain dependencies

The compiled output files will be generated in the `bin` folder by default.

> To customize the folder structure, open `.vscode/settings.json` and update the related settings there.

# Car Rental Service Project

## Overview

This Car Rental Service application is built using Java and SQL. It manages customer registrations, vehicle rentals, and database interactions, featuring user authentication and administrative functions.

## Dependencies

### Required Libraries:
- **JDBC Driver** (for SQL connectivity)
- **Java Date and Time Libraries** (for rental management)

These are found in the `lib` folder.

## Project Structure

```
CarRentalService/
|
├── lib
│   ├── jdbc-driver.jar
│   └── additional-library.jar
├── src
│   ├── CarRentalService.java
│   ├── Customer.java
│   ├── Vehicle.java
│   ├── Booking.java
│   └── Admin.java
└── README.md
```

## Installation and Setup

### 1. **Database Setup**

1. **Create the database:**
   ```sql
   CREATE DATABASE car_rental;
   USE car_rental;
   ```

2. **Create tables:**
   ```sql
   CREATE TABLE customer (customer_gmail VARCHAR(100) PRIMARY KEY, name VARCHAR(250), phone CHAR(15), address VARCHAR(1000),password CHAR(8));
   CREATE TABLE vehicle (vehicle_id INT PRIMARY KEY, model VARCHAR(40), brand VARCHAR(40), availability TINYINT(1),rpd FLOAT,serviceDate DATE);
   CREATE TABLE booking (customer_gmail VARCHAR(255), bookID INT, vehicle_id INT, start_date DATE, end_date DATE);
   CREATE TABLE admins (adminID INT,username VARCHAR(100),password CHAR(8)); 
   CREATE TABLE complaints (complaintID INT,customer_gmail VARCHAR(255),vehicle_id INT,complaint TEXT,date DATE);  
   CREATE TABLE offers (offerDetails VARCHAR(255),discountRate DECIMAL(5,2),applicableDays VARCHAR(50)); 

   
   ```

3. **Configure database connection:**
   ```java
   String url = "jdbc:mysql://localhost:3306/car_rental";
   String user = "root";
   String password = "password";
   ```

## Running the Application

Run `CarRentalService.java` as the entry point. Users can register or log in, with customers managing rentals and admins handling system tasks.

### Example Credentials

**Customer:**
- Username: user123
- Password: userpass

**Admin:**
- Username: admin123
- Password: adminpass

## Running the Project in an IDE

1. Open the project in an IDE .
2. Add dependencies (e.g., JDBC driver).
3. Run `CarRentalService.java`.

