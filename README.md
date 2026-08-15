# Smart Mess

A production-oriented Progressive Web Application for digitizing daily operations of a local mess service, with a focus on real-time meal planning, customer responses, meal collection, billing, and payment tracking.

## 🌐 Live Demo

**Application:** https://smart-mess-client.onrender.com

## 📋 Overview

Smart Mess provides a digital platform for managing the daily workflow between a mess owner and customers.

The system focuses on real-time operational visibility rather than simply digitizing records. Customers can respond to daily menus, while the mess owner can monitor responses, manage meals, track collections, generate bills, and manage payments.

## ✨ Key Features

### Owner Portal

* Dashboard with operational overview
* Menu publishing and management
* Real-time customer meal responses
* Meal collection tracking
* Customer management
* Monthly billing
* Payment tracking
* Business insights
* Meal pricing and UPI settings

### Customer Portal

* View today's menu
* Select meal preference
* View meal records
* View billing history
* View payment history
* Make UPI payment requests
* Manage profile

### Real-Time Operations

* Live dashboard updates using WebSocket
* Real-time meal response monitoring
* Continuous visibility during meal collection

## 🛠️ Tech Stack

### Frontend

* React
* TypeScript
* Vite
* Tailwind CSS
* Axios
* React Router
* STOMP / SockJS

### Backend

* Java 17
* Spring Boot
* Spring Data JPA
* Spring Security
* JWT Authentication
* MapStruct
* Maven

### Database & Deployment

* MySQL
* Aiven Cloud
* Render
* Git & GitHub

## 🔄 System Workflow

```text
Menu Publishing
       ↓
Customer Meal Response
       ↓
Real-Time Dashboard
       ↓
Meal Collection
       ↓
Meal Records
       ↓
Monthly Billing
       ↓
Payment Tracking
```

## 🏗️ Architecture

```text
React + TypeScript
        │
        ├── REST API
        │
        └── WebSocket
              ↓
      Spring Boot Backend
              ↓
        MySQL Database
```

The application uses separate Owner and Customer portals connected to a common Spring Boot backend.

## 🚀 Local Setup

### Prerequisites

* Java 17+
* Node.js
* Maven
* MySQL
* pnpm

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs by default on:

```text
http://localhost:8080
```

### Frontend

```bash
cd client
pnpm install
pnpm dev
```

Frontend runs by default on:

```text
http://localhost:5173
```

To create a production build:

```bash
pnpm build
```

## 🔐 Environment Variables

### Backend

Configure the required production/local environment variables:

```text
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION=
SPRING_PROFILES_ACTIVE=
```

### Frontend

```text
VITE_API_URL=
VITE_WS_URL=
```

Do not commit credentials, JWT secrets, or other sensitive environment values to the repository.

## 🚀 Deployment

The application is deployed using:

* **Frontend:** Render
* **Backend:** Render
* **Database:** Aiven Cloud MySQL

Production frontend:

**https://smart-mess-client.onrender.com**

## 🔮 Future Scope

* Push notifications
* Improved offline support
* PDF bill generation
* QR-based customer registration
* Additional payment integrations
* Further operational analytics
