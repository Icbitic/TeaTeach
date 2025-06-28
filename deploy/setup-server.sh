#!/bin/bash

# TeaTeach Server Setup Script
# This script prepares the server for TeaTeach deployment

set -e

echo "Setting up TeaTeach deployment environment..."

# Update system packages
sudo apt update
sudo apt upgrade -y

# Install required packages
sudo apt install -y openjdk-17-jdk nginx curl wget unzip git

# Install Node.js 18.x
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Create application directories
mkdir -p /opt/teateach/backend
mkdir -p /opt/teateach/frontend
mkdir -p /opt/teateach/logs
mkdir -p /opt/teateach/config
mkdir -p /opt/teateach/source

# Set ownership
chown -R root:root /opt/teateach

# Create systemd service for backend
cp /tmp/teateach-backend.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable teateach-backend

# Configure nginx
rm -f /etc/nginx/sites-enabled/default

# Create nginx configuration
tee /etc/nginx/sites-available/teateach > /dev/null <<'EOF'
server {
    listen 80;
    server_name _;
    
    # Frontend
    location / {
        root /opt/teateach/frontend;
        try_files $uri $uri/ /index.html;
        index index.html;
        
        # Cache static assets
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
        
        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }
    
    # Health check endpoint
    location /health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
EOF

# Enable nginx site
ln -sf /etc/nginx/sites-available/teateach /etc/nginx/sites-enabled/

# Test nginx configuration
nginx -t

# Enable and start nginx
systemctl enable nginx
systemctl restart nginx

# Create log rotation configuration
tee /etc/logrotate.d/teateach > /dev/null <<'EOF'
/opt/teateach/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
    postrotate
        systemctl reload teateach-backend
    endscript
}
EOF

echo "Server setup completed successfully!"
echo "You can now deploy your application using GitHub Actions."
echo ""
echo "Useful commands:"
echo "  - Check backend status: systemctl status teateach-backend"
echo "  - View backend logs: journalctl -u teateach-backend -f"
echo "  - Check nginx status: systemctl status nginx"
echo "  - View nginx logs: tail -f /var/log/nginx/access.log"