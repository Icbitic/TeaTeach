# Security Guidelines for TeaTeach

## Overview

This document outlines the security measures implemented in TeaTeach and provides guidelines for secure deployment and usage.

## Security Fixes Applied

### 1. Removed Security Backdoor
- **Issue**: A backdoor authentication mechanism was present in `JwtAuthenticationFilter.java` that allowed bypassing JWT authentication with a `check-ignore` parameter.
- **Fix**: Completely removed the backdoor mechanism to prevent unauthorized access.
- **Impact**: All API endpoints now properly require valid JWT tokens for authentication.

### 2. Secured Database Credentials
- **Issue**: Database passwords were hardcoded in configuration files.
- **Fix**: Replaced hardcoded credentials with environment variables.
- **Configuration**: Use `DB_USERNAME` and `DB_PASSWORD` environment variables.

### 3. Secured JWT Secret
- **Issue**: JWT secret key was hardcoded in configuration files.
- **Fix**: Moved JWT secret to environment variable `JWT_SECRET`.
- **Requirement**: Use a strong, randomly generated secret key (minimum 256 bits).

### 4. Secured Email Credentials
- **Issue**: Email credentials were exposed in configuration files.
- **Fix**: Moved to environment variables `MAIL_USERNAME` and `MAIL_PASSWORD`.

### 5. Enhanced SSL/TLS Configuration
- **Issue**: Database connections were configured with `useSSL=false`.
- **Fix**: Enabled SSL for database connections in development environment.
- **Note**: Ensure your MySQL server supports SSL connections.

### 6. Improved JWT Token Validation
- **Enhancement**: Added better token validation with expiration checks.
- **Enhancement**: Added null and empty string validation.
- **Enhancement**: Improved error handling for invalid tokens.

### 7. Enhanced Security Headers
- **Added**: X-Frame-Options: DENY
- **Added**: X-Content-Type-Options: nosniff
- **Added**: HTTP Strict Transport Security (HSTS)
- **Added**: Proper CORS configuration

## Environment Configuration

### Required Environment Variables

Create a `.env` file (use `.env.example` as template) with the following variables:

```bash
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_db_password

# JWT
JWT_SECRET=your_256_bit_secret_key
JWT_EXPIRATION=86400000

# Email
MAIL_USERNAME=your_email@domain.com
MAIL_PASSWORD=your_app_password
```

### Generating Secure JWT Secret

Generate a secure JWT secret using one of these methods:

```bash
# Using OpenSSL
openssl rand -base64 32

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"

# Using Python
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

## Deployment Security Checklist

### Before Deployment

- [ ] Set all required environment variables
- [ ] Use strong, unique passwords for all accounts
- [ ] Enable SSL/TLS for database connections
- [ ] Configure firewall rules to restrict database access
- [ ] Use HTTPS for all web traffic
- [ ] Review and update dependency versions
- [ ] Enable application logging and monitoring

### Production Environment

- [ ] Use a dedicated database user with minimal privileges
- [ ] Enable database SSL/TLS encryption
- [ ] Configure reverse proxy (nginx/Apache) with security headers
- [ ] Set up regular security updates
- [ ] Implement rate limiting
- [ ] Configure log rotation and monitoring
- [ ] Use secrets management service (AWS Secrets Manager, Azure Key Vault, etc.)

## Security Best Practices

### For Developers

1. **Never commit sensitive data** to version control
2. **Use environment variables** for all configuration
3. **Validate all user inputs** on both client and server side
4. **Keep dependencies updated** regularly
5. **Follow principle of least privilege** for database access
6. **Use HTTPS** in all environments
7. **Implement proper error handling** without exposing sensitive information

### For System Administrators

1. **Regular security updates** for OS and applications
2. **Network segmentation** to isolate database servers
3. **Regular backups** with encryption
4. **Monitor logs** for suspicious activities
5. **Implement intrusion detection** systems
6. **Use Web Application Firewall** (WAF)
7. **Regular security audits** and penetration testing

## Reporting Security Issues

If you discover a security vulnerability, please:

1. **Do not** create a public GitHub issue
2. Contact the maintainers privately
3. Provide detailed information about the vulnerability
4. Allow time for the issue to be addressed before public disclosure

## Security Updates

This document will be updated as new security measures are implemented. Always refer to the latest version for current security guidelines.

---

**Last Updated**: January 2025
**Version**: 1.0