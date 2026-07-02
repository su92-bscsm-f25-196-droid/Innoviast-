# Innoviast LMS & Education ERP System

A modern, production-ready **Flutter-based Learning Management System (LMS)** and **Education ERP** application designed for schools, colleges, and universities. This application provides a complete digital education management solution with dedicated dashboards for **Admin, HOD, Teacher, and Student**, featuring secure authentication, course management, attendance, assignments, quizzes, examinations, fee management, payroll, analytics, live classes, and AI-powered assistance.

---

#  Features

##  Authentication

* Secure Login & Logout
* Role-Based Authentication (RBAC)
* Remember Me
* Session Persistence
* Auto Login
* Forgot Password
* Password Reset
* Email Verification
* Secure Local Storage

---

# User Roles

The application supports four user roles:

* ‍ Admin
* ‍HOD (Head of Department)
*  Teacher
* ‍ Student

Each role has a dedicated dashboard and specific permissions.

---

#‍ Admin Dashboard

The Admin has complete control over the entire system.

### User Management

* Create Student Accounts
* Create Teacher Accounts
* Create HOD Accounts
* Edit Users
* Delete Users
* Activate/Deactivate Users
* Reset Password
* Search Users
* View All Users

### Department Management

* Add Departments
* Edit Departments
* Delete Departments

### Course Management

* Add Courses
* Update Courses
* Delete Courses
* Assign Teachers
* Assign Departments
* Course Categories
* Course Thumbnails

### Student Enrollment

* Enroll Students
* Remove Students
* Transfer Students
* Approve Enrollment Requests
* Reject Enrollment Requests
* Enrollment History

### Academic Management

* Attendance
* Assignments
* Quizzes
* Exams
* Timetable
* Results
* Grades
* Academic Calendar
* Holiday Calendar

### Reports & Analytics

* Total Students
* Total Teachers
* Total HODs
* Total Courses
* Active Classes
* Pending Assignments
* Attendance Reports
* Analytics Dashboard
* Performance Charts

---

#  Fee Management

Admin can manage the complete fee system.

### Features

* Monthly Fees
* Semester Fees
* Admission Fees
* Examination Fees
* Library Fees
* Hostel Fees
* Scholarships
* Discounts
* Fine Management
* Fee Challans
* Fee Receipts
* Fee History
* Pending Dues
* Fee Reports
* PDF & Excel Export
* Fee Notifications

Students can

* View Fee Details
* Download Challans
* Download Receipts
* View Payment History
* View Pending Dues

---

#  Payroll Management

Admin can manage salaries of Teachers and HODs.

### Features

* Monthly Salary
* Bonuses
* Allowances
* Deductions
* Salary Slips
* Payroll Reports
* Payment History
* PDF Export

Teachers and HODs can

* View Salary
* Download Salary Slip
* View Payment History

---

#  HOD Dashboard

Department-level management.

### Features

* Manage Department
* View Teachers
* View Students
* Approve Courses
* Monitor Attendance
* Department Timetable
* Reports
* Department Analytics

---

# Teacher Dashboard

Teachers can manage only assigned courses.

### Features

* Assigned Courses
* Student Enrollment
* Attendance
* Assignment Management
* Quiz Management
* Upload Notes
* Upload PDFs
* Upload PPT
* Upload Word Files
* Upload Videos
* Upload Images
* Upload ZIP Files
* Upload Study Material

---

# Assignment Module

Teachers can

* Create Assignments
* Edit Assignments
* Delete Assignments
* Set Deadlines
* Assign Marks
* Provide Feedback

Supported Files

* PDF
* DOC
* DOCX
* PPT
* PPTX
* Images
* ZIP
* RAR
* Videos
* Audio
* Google Drive Links

Students can

* View Assignments
* Submit Assignments
* Upload Files
* View Feedback
* Track Submission Status

---

# Quiz Module

Teachers can create

* MCQs
* True/False
* Short Questions
* Long Questions
* Multiple Correct Answers

Features

* Quiz Timer
* Negative Marking
* Auto Grading
* Manual Grading
* Instant Results
* Quiz Analytics

---

# Examination System

### Admin

* Schedule Exams
* Publish Results
* Generate Date Sheets
* Generate Result Cards
* Calculate GPA
* Calculate CGPA

### Teacher

* Upload Marks
* Submit Grades
* View Performance

### Student

* View Date Sheet
* View Results
* Download Result Card
* View GPA & CGPA

---

#  Student Dashboard

Students have limited permissions.

### Features

* Dashboard
* View Courses
* Self Enrollment (Optional)
* Attendance
* Timetable
* Assignments
* Quiz
* Results
* Notifications
* Download Notes
* Profile Management

Students can

* Edit Profile
* Change Password
* Upload Profile Picture

---

# Timetable & Live Classes

Students and Teachers can view

* Today's Classes
* Weekly Schedule
* Monthly Schedule
* Upcoming Classes
* Live Classes
* Completed Classes

Teachers can

* Schedule Live Classes
* Start Live Classes
* End Live Classes
* Share Meeting Links
* Upload Recordings

---

# 💬 Chat & Messaging

* One-to-One Chat
* Group Chat
* Department Chat
* Course Chat
* Teacher ↔ Student Chat
* Admin ↔ Teacher Chat
* File Sharing
* Voice Messages
* Image Sharing
* Read Receipts
* Online Status
* Push Notifications

---

# 🤖 AI Assistant

Integrated AI assistant for education.

Features

* Answer Student Questions
* Explain Topics
* Generate Notes
* Generate Assignments
* Generate Quiz Questions
* Solve Programming Problems
* Solve Mathematics
* Translate Text
* Study Planner
* Learning Recommendations

---

# 📊 Analytics Dashboard

Beautiful charts and reports.

Includes

* Student Analytics
* Teacher Analytics
* Attendance Analytics
* Assignment Statistics
* Quiz Performance
* Fee Collection
* Payroll Reports
* Department Performance
* Course Analytics
* Revenue Dashboard

Charts

* Line Charts
* Pie Charts
* Bar Charts
* Area Charts

---

# 📂 File Management

Supports

* PDF
* DOC
* DOCX
* PPT
* PPTX
* Excel
* Images
* Videos
* Audio
* ZIP
* RAR
* Text Files

---

# 🔔 Notifications

Real-time notifications for

* Assignments
* Quizzes
* Live Classes
* Announcements
* Fee Due Dates
* Enrollment Approval
* Results
* Attendance Alerts

---

# 📆 Calendar

* Academic Calendar
* Assignment Deadlines
* Quiz Schedule
* Examination Schedule
* Events
* Holidays
* Google Calendar Sync
* Outlook Calendar Sync

---

# 🔒 Security

* Role-Based Access Control (RBAC)
* Secure Authentication
* Encrypted Storage
* Input Validation
* Session Management
* Secure Firebase Rules

---

# ⚡ Performance

* Offline Support
* Auto Sync
* Lazy Loading
* Pagination
* Caching
* Optimized Firebase Queries
* Smooth Animations
* High Performance

---

# 🎨 UI/UX

* Material Design 3
* Modern Dashboard
* Responsive Layout
* Dark Mode
* Light Mode
* Glassmorphism UI
* Beautiful Charts
* Reusable Components
* Smooth Animations

---

# 🛠 Tech Stack

| Technology               | Description              |
| ------------------------ | ------------------------ |
| Flutter                  | Cross-platform framework |
| Dart                     | Programming Language     |
| Firebase Authentication  | User Authentication      |
| Cloud Firestore          | NoSQL Database           |
| Firebase Storage         | File Storage             |
| Firebase Cloud Messaging | Push Notifications       |
| Firebase Analytics       | App Analytics            |
| Firebase Crashlytics     | Crash Reporting          |
| REST API                 | External Integrations    |
| Riverpod / Bloc          | State Management         |
| Material 3               | Modern UI Design         |

---

# 📁 Project Structure

```text
lib/
│── core/
│── config/
│── models/
│── services/
│── repositories/
│── features/
│   ├── authentication/
│   ├── admin/
│   ├── hod/
│   ├── teacher/
│   ├── student/
│   ├── courses/
│   ├── attendance/
│   ├── assignments/
│   ├── quizzes/
│   ├── exams/
│   ├── fees/
│   ├── payroll/
│   ├── messaging/
│   ├── notifications/
│   ├── analytics/
│   └── ai_assistant/
│── shared/
│── widgets/
│── utils/
└── main.dart
```

# License

This project is developed as part of the **Innoviast Summer Internship Program 2026** and serves as a modern **University Learning Management System (LMS) & Education ERP** solution built with Flutter.

---

# Developer

**Muhammad Amir Zubair**

**Mobile App Developer** 
