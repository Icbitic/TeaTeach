#!/bin/bash

# TeaTeach Server Setup Script
# This script prepares the server for TeaTeach deployment

set -e

echo "Setting up TeaTeach deployment environment..."

# Update system packages
sudo apt update
sudo apt upgrade -y

# Install required packages
sudo apt install -y openjdk-17-jdk nginx curl wget unzip

# Create application directories
sudo mkdir -p /opt/teateach/backend
sudo mkdir -p /opt/teateach/frontend
sudo mkdir -p /opt/teateach/logs
sudo mkdir -p /opt/teateach/config

# Set proper ownership
sudo chown -R neu:neu /opt/teateach

# Create systemd service for backend
sudo cp /tmp/teateach-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable teateach-backend

# Configure nginx
sudo rm -f /etc/nginx/sites-enabled/default

# Create nginx configuration
sudo tee /etc/nginx/sites-available/teateach > /dev/null <<'EOF'
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
sudo ln -sf /etc/nginx/sites-available/teateach /etc/nginx/sites-enabled/

# Test nginx configuration
sudo nginx -t

# Enable and start nginx
sudo systemctl enable nginx
sudo systemctl restart nginx

# Create log rotation configuration
sudo tee /etc/logrotate.d/teateach > /dev/null <<'EOF'
/opt/teateach/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 neu neu
    postrotate
        systemctl reload teateach-backend
    endscript
}
EOF

echo "Server setup completed successfully!"
echo "You can now deploy your application using GitHub Actions."
echo ""
echo "Useful commands:"
echo "  - Check backend status: sudo systemctl status teateach-backend"
echo "  - View backend logs: sudo journalctl -u teateach-backend -f"
echo "  - Check nginx status: sudo systemctl status nginx"
echo "  - View nginx logs: sudo tail -f /var/log/nginx/access.log"