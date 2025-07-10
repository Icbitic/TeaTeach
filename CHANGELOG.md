# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - 2025-01-XX

### Security Fixes 🔒

#### Critical Security Vulnerabilities Fixed

- **CRITICAL**: Removed authentication backdoor in `JwtAuthenticationFilter.java`
  - Eliminated `check-ignore` parameter that allowed bypassing JWT authentication
  - All API endpoints now properly require valid JWT tokens
  - **Impact**: Prevents unauthorized admin access to the system

- **HIGH**: Secured hardcoded credentials in configuration files
  - Moved database passwords to environment variables (`DB_USERNAME`, `DB_PASSWORD`)
  - Moved JWT secret to environment variable (`JWT_SECRET`)
  - Moved email credentials to environment variables (`MAIL_USERNAME`, `MAIL_PASSWORD`)
  - **Impact**: Prevents credential exposure in version control

- **MEDIUM**: Enhanced SSL/TLS configuration
  - Enabled SSL for database connections (`useSSL=true`)
  - **Impact**: Encrypts database communication

#### Security Enhancements

- **Enhanced JWT token validation**
  - Added null and empty string validation
  - Improved token expiration checking
  - Better error handling for invalid tokens
  - Added runtime exception for malformed tokens

- **Added security headers**
  - X-Frame-Options: DENY (prevents clickjacking)
  - X-Content-Type-Options: nosniff (prevents MIME sniffing)
  - HTTP Strict Transport Security (HSTS) with 1-year max-age
  - Enhanced CORS configuration

- **Improved authorization controls**
  - Added explicit admin endpoint protection (`/api/admin/**`)
  - Added health check endpoint for monitoring (`/actuator/health`)
  - Maintained role-based access control for student and teacher endpoints

### Added 📦

- **Environment Configuration**
  - Created `.env.example` template file with all required environment variables
  - Added comprehensive environment variable documentation
  - Included secure JWT secret generation instructions

- **Security Documentation**
  - Created `SECURITY.md` with comprehensive security guidelines
  - Added deployment security checklist
  - Included security best practices for developers and administrators
  - Added vulnerability reporting guidelines

- **Enhanced .gitignore**
  - Added environment files (`.env`, `.env.local`, etc.)
  - Added sensitive configuration files (`.key`, `.pem`, `.p12`, `.jks`)
  - Improved coverage for secret files

### Changed 🔄

- **Frontend Dependencies Updated**
  - Updated `axios` from `^1.6.2` to `^1.7.7` (security patches)
  - Updated `vue` from `^3.2.13` to `^3.5.12` (latest stable)
  - Updated `core-js` from `^3.8.3` to `^3.39.0` (security patches)
  - Updated `element-plus` from `^2.4.3` to `^2.8.8` (latest features)
  - Updated `vue-i18n` from `^9.14.4` to `^10.0.4` (major version upgrade)
  - Updated `vue-router` from `^4.0.3` to `^4.4.5` (security patches)
  - Updated `vuex` from `^4.0.0` to `^4.1.0` (bug fixes)

- **Development Dependencies Updated**
  - Updated `@babel/core` from `^7.12.16` to `^7.26.0`
  - Updated `@babel/eslint-parser` from `^7.12.16` to `^7.26.0`
  - Updated all Vue CLI plugins to `~5.0.8`
  - Updated `eslint` from `^7.32.0` to `^8.57.1` (security patches)
  - Updated `eslint-plugin-vue` from `^8.0.3` to `^9.30.0`
  - Updated `sass` from `^1.32.7` to `^1.81.0`
  - Updated `sass-loader` from `^12.0.0` to `^16.0.3`

- **Configuration Improvements**
  - All sensitive configuration now uses environment variables
  - Improved error handling in JWT service
  - Enhanced security filter configuration

### Security Impact Assessment

#### Before Fixes
- ❌ Authentication could be bypassed with a simple URL parameter
- ❌ Database credentials exposed in configuration files
- ❌ JWT secrets exposed in configuration files
- ❌ Email credentials exposed in configuration files
- ❌ Insecure database connections (no SSL)
- ❌ Missing security headers
- ❌ Outdated dependencies with known vulnerabilities

#### After Fixes
- ✅ Robust JWT-based authentication required for all protected endpoints
- ✅ All sensitive credentials secured with environment variables
- ✅ SSL-encrypted database connections
- ✅ Comprehensive security headers implemented
- ✅ Updated dependencies with latest security patches
- ✅ Comprehensive security documentation and guidelines

### Migration Guide

For existing deployments, follow these steps:

1. **Create environment file**:
   ```bash
   cp .env.example .env
   # Edit .env with your actual values
   ```

2. **Generate secure JWT secret**:
   ```bash
   openssl rand -base64 32
   ```

3. **Update environment variables** in your deployment:
   - `DB_USERNAME` and `DB_PASSWORD`
   - `JWT_SECRET` (use generated value)
   - `MAIL_USERNAME` and `MAIL_PASSWORD`

4. **Update frontend dependencies**:
   ```bash
   cd web
   npm update
   ```

5. **Test the application** thoroughly after deployment

### Breaking Changes ⚠️

- **Environment variables are now required** for:
  - Database connection (`DB_USERNAME`, `DB_PASSWORD`)
  - JWT authentication (`JWT_SECRET`)
  - Email functionality (`MAIL_USERNAME`, `MAIL_PASSWORD`)

- **Frontend dependencies** may require code updates due to major version changes:
  - Vue I18n upgraded to v10 (check for API changes)
  - ESLint upgraded to v8 (may require configuration updates)

### Recommendations

1. **Immediate Actions**:
   - Deploy these security fixes as soon as possible
   - Rotate all existing JWT secrets
   - Review access logs for any suspicious activity

2. **Ongoing Security**:
   - Implement regular dependency updates
   - Set up automated security scanning
   - Monitor application logs for security events
   - Conduct regular security audits

---

**Contributors**: Security fixes and improvements
**Review**: All changes have been tested for functionality and security
**Deployment**: Requires environment variable configuration before deployment