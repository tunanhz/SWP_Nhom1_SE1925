# Healthcare System - SWP_Nhom1_SE1925

A comprehensive clinic management system designed to streamline operations, including patient management, appointment scheduling, medical examinations, lab services, prescription management, staff management, inventory, billing, and internal notifications. The system offers a modern, user-friendly interface with detailed role-based access control.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Technologies Used](#technologies-used)
- [User Roles and Permissions](#user-roles-and-permissions)
- [Workflow](#workflow)
- [Installation and Deployment](#installation-and-deployment)
- [API Endpoints](#api-endpoints)
- [Database Structure](#database-structure)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [Contact](#contact)

## Overview
This system supports end-to-end clinic operations, enabling efficient management of patients, appointments, medical services, prescriptions, staff, inventory, and finances. It features a closed-loop workflow, role-based access, real-time notifications via WebSocket, and a responsive web interface.

## Features
- **Patient Management**: Register, schedule appointments, view medical history, and manage payments.
- **Appointment Scheduling**: Book, confirm, and manage appointments.
- **Medical Services**: Conduct examinations, order lab tests, and record results.
- **Prescription Management**: Create, verify, and dispense prescriptions.
- **Inventory Management**: Track and update medicine stock.
- **Staff Management**: Manage accounts, roles, and departments.
- **Billing and Reporting**: Generate invoices and financial reports.
- **Real-Time Notifications**: Internal announcements via WebSocket.

## System Architecture
The system follows a multi-tier architecture:
```
[Patient/Receptionist/Doctor/Pharmacist] ↔ [Web UI]
        ↓
[Servlet API] ↔ [Database] ↔ [WebSocket]
```
- **Frontend**: Responsive web interface for all user roles.
- **Backend**: RESTful API handling business logic.
- **Database**: Centralized storage for all data.
- **WebSocket**: Real-time internal notifications.

## Technologies Used
### Backend
- Java 17, Jakarta EE (Servlet API)
- Maven for dependency management
- JDBC with SQL Server
- WebSocket for real-time notifications
- Libraries: Jackson, Gson, Lombok, JUnit, javax.mail, org.json

### Frontend
- HTML5, CSS3, JavaScript (ES6)
- Frameworks: Bootstrap, SweetAlert2, FontAwesome, Flaticon
- Libraries: FullCalendar, SwiperSlider, ApexCharts

### Others
- RESTful API with AJAX
- Multi-layer structure: Controller, DAO, DTO, Model

## User Roles and Permissions
| Role             | Key Functions                                                                 |
|------------------|-------------------------------------------------------------------------------|
| **Admin System** | Manage accounts, roles, system settings, and staff.                           |
| **Admin Business** | Handle invoices, revenue, and financial reports.                              |
| **Doctor**       | Conduct examinations, order services, view appointments, and prescribe meds.  |
| **Pharmacist**   | Manage prescriptions, verify and dispense meds, and track inventory.          |
| **Receptionist** | Manage appointments, confirm patients, and assign to doctors.                 |
| **Patient**      | Register, book appointments, view medical history, and pay invoices.          |

## Workflow
1. **Patient**: Registers, books an appointment.
2. **Receptionist**: Confirms appointment, manages waitlist.
3. **Doctor**: Examines patient, orders lab tests, prescribes meds.
4. **Pharmacist**: Verifies prescriptions, dispenses meds, updates inventory.
5. **Admin**: Manages accounts, roles, and reports.
6. **Notifications**: Sent via WebSocket to relevant roles.

## Installation and Deployment
### Prerequisites
- JDK 17+
- Maven 3.6+
- SQL Server
- Tomcat or Jakarta EE-compatible server

### Steps
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   ```
2. **Configure Database**:
   - Create a database named `HealthCareSystem` in SQL Server.
   - Import SQL scripts from `src/main/resources/sql/`.
   - Update DB connection details in `dao/DBContext.java` if needed.
3. **Build the Project**:
   ```bash
   cd SWP_Nhom1_SE1925
   mvn clean package
   ```
4. **Deploy**:
   - Deploy the generated WAR file to Tomcat or a compatible server.
5. **Access**:
   - Open `http://localhost:8080/SWP_Nhom1_SE1925` in a browser.

## API Endpoints
| Endpoint                          | Method | Description                              | Example Request                                      |
|-----------------------------------|--------|------------------------------------------|-----------------------------------------------------|
| `/api/login`                      | POST   | Authenticate user                        | `{ "username": "user", "password": "pass" }`        |
| `/api/doctor/examination`         | POST   | Record examination details               | `{ "patientId": 1, "symptoms": "...", "diagnosis": "..." }` |
| `/api/doctor/service-order`       | POST   | Create service order                     | `{ "medicineRecordId": 1, "services": [1,2], "assignedDoctors": [3,4] }` |
| `/api/prescription?patientId=1`   | GET    | Retrieve prescriptions                   | -                                                   |
| `/invoice?patient_id=1`           | GET    | Retrieve invoices                        | -                                                   |
| `/ws/announcements?staffId=1`     | WS     | WebSocket for internal notifications     | -                                                   |

## Database Structure
Key tables include:
- `AccountStaff`, `AccountPharmacist`, `AccountPatient`: User accounts and roles.
- `Doctor`, `Receptionist`, `Pharmacist`, `Patient`: User details.
- `Appointment`: Appointment scheduling.
- `Waitlist`: Patient waitlist.
- `ExamResult`, `MedicineRecords`: Examination and prescription records.
- `ServiceOrder`, `ServiceOrderItem`: Medical service orders.
- `Prescription`, `Invoice`: Prescriptions and billing.
- `Announcement`: Internal notifications.

## Troubleshooting
- **Login Issues**: Verify credentials and account status.
- **Data Not Saving**: Check database connection and permissions.
- **API Errors**: Inspect request/response in browser dev tools or server logs.
- **Session Errors**: Ensure correct role is logged in.

## Contributing
- Submit issues or pull requests on GitHub.
- Follow the project's coding standards and guidelines.

## Contact
- **Group Name**: Nhom1_SE1925
- **Email**: [Insert group email]
- **GitHub**: [Insert repository link]

---

### Changes Made
1. **Improved Structure**: Organized sections clearly with concise descriptions.
2. **Removed Redundancy**: Eliminated repetitive details (e.g., detailed module instructions moved to documentation or code comments).
3. **Enhanced Readability**: Used markdown formatting for better clarity (e.g., tables, code blocks, lists).
4. **Standardized Terminology**: Unified terms like "Healthcare System" and "SWP_Nhom1_SE1925".
5. **Simplified Setup Instructions**: Condensed installation steps for clarity.
6. **Professional Tone**: Adopted a formal yet approachable tone suitable for a project README.
7. **Omitted Unnecessary Details**: Removed excessive technical notes (e.g., directory structure, validation details) that belong in internal documentation.
8. **Aligned with GitHub Standards**: Included standard sections like Contributing and Contact.

If you need further refinements or additional sections, let me know!
