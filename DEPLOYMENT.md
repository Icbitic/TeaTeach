# TeaTeach Deployment Guide

This guide explains how to deploy the TeaTeach application using GitHub Actions to automatically build and deploy both the Spring Boot backend and Vue.js frontend.

## Architecture

- **Backend**: Spring Boot application running on port 8080
- **Frontend**: Vue.js SPA served by Nginx on port 80
- **Database**: MySQL (needs to be set up separately on the server)
- **Reverse Proxy**: Nginx handles frontend serving and API proxying

## Server Requirements

- **Server IP**: 36.212.128.238
- **User**: root
- **OS**: Ubuntu/Debian-based Linux
- Java 17 or higher
- Node.js 18 or higher
- Nginx
- MySQL 8.0
- Sudo access for the deployment user

## Server Setup

### 1. Initial Server Configuration

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install required packages
sudo apt install -y openjdk-17-jdk nginx mysql-server curl wget unzip

# Create deployment user (if not exists)
sudo useradd -m -s /bin/bash neu
sudo usermod -aG sudo neu
```

### 2. MySQL Setup

```bash
# Secure MySQL installation
sudo mysql_secure_installation

# Create database and user
sudo mysql -u root -p
```

```sql
CREATE DATABASE teateach;
CREATE USER 'teateach'@'localhost' IDENTIFIED BY 'teateach123';
GRANT ALL PRIVILEGES ON teateach.* TO 'teateach'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. Firewall Configuration

```bash
# Allow HTTP and SSH
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 8080
sudo ufw enable
```

## GitHub Actions Deployment

The deployment is fully automated through GitHub Actions. When you push to the `main` branch:

1. **Test Phase**: Runs unit tests with a temporary MySQL instance
2. **Deploy Phase**:
   - Sets up server environment (first time only)
   - Clones/updates repository on the server
   - Builds Spring Boot JAR file on the server
   - Builds Vue.js production bundle on the server
   - Starts/restarts services

### Workflow Features

- ✅ Automated testing before deployment
- ✅ Remote building (faster than uploading artifacts)
- ✅ Git-based deployment with automatic updates
- ✅ Zero-downtime deployment with systemd
- ✅ Health checks after deployment
- ✅ Nginx configuration for SPA routing
- ✅ Log rotation and monitoring
- ✅ Proper security configurations

## Server File Structure

```
/opt/teateach/
├── backend/
│   └── TeaTeach-0.0.1-SNAPSHOT.jar
├── frontend/
│   ├── index.html
│   ├── static/
│   └── ...
├── source/
│   ├── .git/
│   ├── src/
│   ├── web/
│   ├── pom.xml
│   └── ... (full repository)
├── logs/
│   ├── application.log
│   └── backend.log
└── uploads/
    └── submissions/
```

## Service Management

### Backend Service (systemd)

```bash
# Check status
sudo systemctl status teateach-backend

# Start/stop/restart
sudo systemctl start teateach-backend
sudo systemctl stop teateach-backend
sudo systemctl restart teateach-backend

# View logs
sudo journalctl -u teateach-backend -f
```

### Nginx Service

```bash
# Check status
sudo systemctl status nginx

# Restart
sudo systemctl restart nginx

# Test configuration
sudo nginx -t

# View logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## URLs and Endpoints

After successful deployment:

- **Frontend**: http://36.212.128.238
- **Backend API**: http://36.212.128.238/api
- **Health Check**: http://36.212.128.238/health
- **Direct Backend**: http://36.212.128.238:8080

## Troubleshooting

### Common Issues

1. **Backend won't start**:
   ```bash
   sudo journalctl -u teateach-backend -n 50
   ```

2. **Database connection issues**:
   - Check MySQL service: `sudo systemctl status mysql`
   - Verify database credentials in `/opt/teateach/backend/application-prod.yml`

3. **Frontend not loading**:
   - Check Nginx configuration: `sudo nginx -t`
   - Verify files exist: `ls -la /opt/teateach/frontend/`

4. **API calls failing**:
   - Check Nginx proxy configuration
   - Verify backend is running on port 8080

### Log Locations

- **Backend Application**: `/opt/teateach/logs/application.log`
- **Backend Service**: `sudo journalctl -u teateach-backend`
- **Nginx Access**: `/var/log/nginx/access.log`
- **Nginx Error**: `/var/log/nginx/error.log`

## Security Considerations

- Change default passwords in production
- Use environment variables for sensitive configuration
- Consider setting up SSL/TLS certificates
- Regularly update system packages
- Monitor logs for suspicious activity

## Manual Deployment (if needed)

If you need to deploy manually:

```bash
# SSH to the server
ssh root@36.212.128.238

# Navigate to source directory
cd /opt/teateach/source

# Update repository
git pull origin main

# Build backend
./mvnw clean package -DskipTests
cp target/*.jar /opt/teateach/backend/

# Build frontend
cd web
npm ci
npm run build
rm -rf /opt/teateach/frontend/*
cp -r dist/* /opt/teateach/frontend/

# Restart services
systemctl restart teateach-backend nginx
```

## Environment Variables

For production, consider setting these environment variables:

```bash
# In /etc/systemd/system/teateach-backend.service
Environment=DB_PASSWORD=your_secure_password
Environment=JWT_SECRET=your_jwt_secret_key
Environment=SPRING_PROFILES_ACTIVE=prod
```

Then reload systemd: `sudo systemctl daemon-reload`