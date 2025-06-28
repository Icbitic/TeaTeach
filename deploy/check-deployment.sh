#!/bin/bash

# TeaTeach Deployment Status Check Script
# This script checks if the deployment is working correctly

SERVER_IP="36.212.128.238"
BACKEND_PORT="8080"

echo "🔍 Checking TeaTeach Deployment Status..."
echo "========================================"

# Check if server is reachable
echo "📡 Testing server connectivity..."
if ping -c 1 $SERVER_IP &> /dev/null; then
    echo "✅ Server is reachable"
else
    echo "❌ Server is not reachable"
    exit 1
fi

# Check frontend (Nginx)
echo "🌐 Testing frontend..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP/)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ Frontend is accessible (HTTP $FRONTEND_STATUS)"
else
    echo "❌ Frontend is not accessible (HTTP $FRONTEND_STATUS)"
fi

# Check backend health
echo "🔧 Testing backend health..."
BACKEND_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP/health)
if [ "$BACKEND_HEALTH" = "200" ]; then
    echo "✅ Backend health check passed (HTTP $BACKEND_HEALTH)"
else
    echo "❌ Backend health check failed (HTTP $BACKEND_HEALTH)"
fi

# Check API endpoint
echo "🔌 Testing API endpoint..."
API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP/api/)
if [ "$API_STATUS" = "200" ] || [ "$API_STATUS" = "404" ]; then
    echo "✅ API endpoint is accessible (HTTP $API_STATUS)"
else
    echo "❌ API endpoint is not accessible (HTTP $API_STATUS)"
fi

# Check direct backend access
echo "🎯 Testing direct backend access..."
DIRECT_BACKEND=$(curl -s -o /dev/null -w "%{http_code}" http://$SERVER_IP:$BACKEND_PORT/actuator/health)
if [ "$DIRECT_BACKEND" = "200" ]; then
    echo "✅ Direct backend access works (HTTP $DIRECT_BACKEND)"
else
    echo "❌ Direct backend access failed (HTTP $DIRECT_BACKEND)"
fi

echo ""
echo "📊 Deployment Summary:"
echo "Frontend URL: http://$SERVER_IP"
echo "API URL: http://$SERVER_IP/api"
echo "Health Check: http://$SERVER_IP/health"
echo "Backend Direct: http://$SERVER_IP:$BACKEND_PORT"

echo ""
echo "🔍 For detailed logs, SSH to the server and run:"
echo "  sudo systemctl status teateach-backend"
echo "  sudo journalctl -u teateach-backend -f"
echo "  sudo systemctl status nginx"
echo "  sudo tail -f /var/log/nginx/access.log"