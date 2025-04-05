# Travel Agency — Spring Boot Project

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Security-JWT%2FOAuth2-blue)](https://spring.io/projects/spring-security)
[![I18n](https://img.shields.io/badge/I18n-English%2FUkrainian-yellowgreen)](https://www.thymeleaf.org/)

A full-featured travel booking platform with secure authentication, payment processing, and role-based administration.

The class diagram of the Domain model is shown in the figure below:

![diagram.png](TravelAgency.png)

## ✨ Key Features

### 🔐 Advanced Authentication

- **OAuth2 Login** via Google and GitHub
- JWT token-based authentication
- Password recovery system (Forgot Password flow)
- Role-based access control:
    - `USER`: Browse and book tours
    - `MANAGER`: Mark tours as "hot", update booking statuses, order management
    - `ADMIN`: Full CRUD operations, user management
- Fully secured with **HTTPS** (TLS 1.3)

### 💳 Payment Integration

- Secure payment processing for bookings
- Order status tracking:
    - `REGISTERED` → `APPROVED` → `PAID`
    - `CANCELED` status flow

### 🌍 Multilingual Support

- Full internationalization (i18n):
    - English (default)
    - Ukrainian
- Dynamic language switching

### 🏝️ Tour Management

- Advanced filtering:
- "Hot deals" promoted by managers
- Detailed tour view with:
- Descriptions
- Hotel ratings
- Transfer types
- Date ranges

## 🛠️ Technology Stack

| Layer                | Technologies                                                    |
|----------------------|-----------------------------------------------------------------|
| **Core Backend**     | Spring Boot 3, Spring MVC, Spring Data JPA, Hibernate           |
| **Security**         | JWT Authentication, OAuth2 (Google/GitHub), Password Reset Flow |
| **Database**         | PostgreSQL with Soft Delete implementation                      |
| **Frontend**         | Thymeleaf, Bootstrap 5, jQuery (AJAX for dynamic content)       |
| **Architecture**     | DTO Pattern (with MapStruct), Layered Architecture              |
| **Payment**          | Stripe API integration for secure payments                      |
| **Order Management** | Full order lifecycle (REGISTERED → PAID → APPROVED/CANCELED)    |
| **DevOps**           | Maven, Embedded Tomcat                                          |
| **Quality**          | Global Exception Handler, AOP Logging, Input Validation         |

## 📸 Screenshots Gallery

<div align="center">
  <img src="screenshots/about_us.png" width="30%" alt="About Us" title="About Us">
  <img src="screenshots/login.png" width="30%" alt="Login" title="Login">
  <img src="screenshots/forgot_password.png" width="30%" alt="Forgot Password" title="Forgot Password">
  <img src="screenshots/register.png" width="30%" alt="Register" title="Register">
  <img src="screenshots/oauth2_google.png" width="30%" alt="OAuth2 Google" title="OAuth2 Google">
  <img src="screenshots/oauth2_git.png" width="30%" alt="OAuth2 GitHub" title="OAuth2 GitHub">
</div>

<details>
<summary>🔍 Show more screenshots</summary>
<div align="center">
<img src="screenshots/vouchers.png" width="30%" alt="Voucher Listing Page"> 
<img src="screenshots/payment.png" width="30%" alt="Stripe Payment Flow"> 
<img src="screenshots/user_info.png" width="30%" alt="User Profile"> 
<img src="screenshots/user_edit.png" width="30%" alt="User Settings"> 
<img src="screenshots/admin_panel.png" width="30%" alt="Admin Dashboard"> 
<img src="screenshots/manager_request.png" width="30%" alt="Manager Request"> 
<img src="screenshots/order_history.png" width="30%" alt="Order Management">
<img src="screenshots/soft_delete.png" width="30%" alt="Archive Vouchers">
<img src="screenshots/user_management.png" width="30%" alt="User Management">
</div>
</details>
