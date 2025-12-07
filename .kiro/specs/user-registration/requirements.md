# Requirements Document

## Introduction

本功能为展览购票系统用户端（frontend-a）实现用户注册功能。目前登录页面只有登录功能，新用户无法注册账号使用系统。

## Glossary

- **Registration_System**: 用户注册系统，处理新用户账号创建
- **User**: C端用户，使用移动端的消费者
- **Login_Page**: 登录页面，包含登录和注册功能

## Requirements

### Requirement 1

**User Story:** As a new User, I want to register an account, so that I can use the exhibition ticketing system.

#### Acceptance Criteria

1. WHEN a User is on the login page THEN the Registration_System SHALL provide a way to switch to registration mode
2. WHEN in registration mode THEN the Registration_System SHALL display username, password, and confirm password fields
3. WHEN a User submits valid registration data THEN the Registration_System SHALL create a new account and auto-login
4. WHEN registration is successful THEN the Registration_System SHALL navigate to the home page

### Requirement 2

**User Story:** As a User, I want form validation during registration, so that I can correct errors before submission.

#### Acceptance Criteria

1. IF the username is empty THEN the Registration_System SHALL display a validation error
2. IF the password is less than 6 characters THEN the Registration_System SHALL display a validation error
3. IF the confirm password does not match password THEN the Registration_System SHALL display a validation error
4. WHEN validation fails THEN the Registration_System SHALL prevent form submission

### Requirement 3

**User Story:** As a User, I want to switch between login and registration modes, so that I can access the appropriate form.

#### Acceptance Criteria

1. WHEN on the login form THEN the Registration_System SHALL display a link to switch to registration
2. WHEN on the registration form THEN the Registration_System SHALL display a link to switch to login
3. WHEN switching modes THEN the Registration_System SHALL clear the form fields
