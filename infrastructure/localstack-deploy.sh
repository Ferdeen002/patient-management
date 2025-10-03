#!/bin/bash
set -e # Stops the script if any command fails

aws --endpoint-url=http://localhost:4566 cloudformation delete-stack \
    --stack-name patient-management

aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name patient-management \
    --template-file "./cdk.out/localstack.template.json"

echo "✅ Stack deployed successfully!"

echo "🔗 Retrieving load balancer information..."
LB_INFO=$(aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].[DNSName,LoadBalancerArn]" --output text)
LB_DNS=$(echo $LB_INFO | awk '{print $1}')
LB_ARN=$(echo $LB_INFO | awk '{print $2}')

echo "Load Balancer DNS: $LB_DNS"

echo "🔗 Getting listener and target group..."
LISTENER_INFO=$(aws --endpoint-url=http://localhost:4566 elbv2 describe-listeners \
    --load-balancer-arn $LB_ARN --query "Listeners[0].DefaultActions[0].TargetGroupArn" --output text)

echo "⏳ Waiting for ECS tasks to start..."
sleep 5

echo "🔍 Finding API Gateway container..."
API_GATEWAY_CONTAINER=$(docker ps --filter "name=ls-ecs-PatientManagementCluster" --format "{{.Names}}" | grep -E "api-gateway|APIGateway" | head -1)

if [ -z "$API_GATEWAY_CONTAINER" ]; then
    # Fallback: find by port 4004
    API_GATEWAY_CONTAINER=$(docker ps --format "{{.Names}}\t{{.Ports}}" | grep "4004->4004" | head -1 | awk '{print $1}')
fi

if [ -n "$API_GATEWAY_CONTAINER" ]; then
    echo "Found API Gateway container: $API_GATEWAY_CONTAINER"
    
    API_GATEWAY_IP=$(docker inspect $API_GATEWAY_CONTAINER --format '{{.NetworkSettings.IPAddress}}')
    echo "API Gateway IP: $API_GATEWAY_IP"
    
    echo "📝 Registering API Gateway as load balancer target..."
    aws --endpoint-url=http://localhost:4566 elbv2 register-targets \
        --target-group-arn $LISTENER_INFO \
        --targets Id=$API_GATEWAY_IP,Port=4004 2>/dev/null || echo "Target already registered or registration failed"
    
    echo "✅ Target registration complete!"
else
    echo "⚠️  Warning: Could not find API Gateway container. You may need to register targets manually."
fi

echo ""
echo "================================================"
echo "🎉 Deployment Complete!"
echo "================================================"
echo "Load Balancer DNS: $LB_DNS"
echo "API Gateway URL: http://$LB_DNS:4566/auth/login"
echo ""
echo "Test with:"
echo "curl -X POST http://$LB_DNS:4566/auth/login \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"email\":\"testuser@test.com\",\"password\":\"password123\"}'"
echo "================================================"