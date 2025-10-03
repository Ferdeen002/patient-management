# LocalStack Deployment Guide

## Overview
This guide explains how to deploy the Patient Management microservices to LocalStack for local testing.

## Prerequisites
- LocalStack running in Docker (Pro version recommended for ECS support)
- AWS CLI configured
- Docker installed
- Maven installed

## Deployment Steps

### 1. Generate CDK Template
```bash
cd infrastructure
mvn compile exec:java -Dexec.mainClass="com.pm.stack.LocalStack"
```

This generates the CloudFormation template at `infrastructure/cdk.out/localstack.template.json`

### 2. Deploy to LocalStack
```bash
cd infrastructure
bash localstack-deploy.sh
```

The script will:
- Delete any existing stack
- Clean up orphaned log groups
- Deploy the new stack
- Wait for ECS tasks to start
- Automatically register the API Gateway with the load balancer
- Display the load balancer DNS

### 3. Load Balancer URL
After deployment, you'll receive a load balancer URL like:
```
http://lb-b9c8bb43.elb.localhost.localstack.cloud:4566
```

**Note:** The load balancer DNS changes with each deployment. Update your API request files accordingly.

## API Endpoints

All requests go through the load balancer on port 4566:

### Authentication
- **Login:** `POST http://<LB_DNS>:4566/auth/login`
- **Validate:** `GET http://<LB_DNS>:4566/auth/validate`

### Patient Service
- **Get Patients:** `GET http://<LB_DNS>:4566/api/patients`
- **Create Patient:** `POST http://<LB_DNS>:4566/api/patients`
- **Update Patient:** `PUT http://<LB_DNS>:4566/api/patients/{id}`
- **Delete Patient:** `DELETE http://<LB_DNS>:4566/api/patients/{id}`

## Testing

### 1. Login Request
```bash
curl -X POST http://lb-b9c8bb43.elb.localhost.localstack.cloud:4566/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"testuser@test.com","password":"password123"}'
```

Response:
```json
{"token":"eyJhbGciOiJIUzI1NiJ9..."}
```

### 2. Validate Token
```bash
curl http://lb-b9c8bb43.elb.localhost.localstack.cloud:4566/auth/validate \
  -H "Authorization: Bearer <your-token>"
```

## Services Deployed

The following services are deployed to LocalStack ECS:

1. **API Gateway** (Port 4004) - Entry point for all requests
2. **Auth Service** (Port 4005) - Authentication and JWT validation
3. **Patient Service** (Port 4000) - Patient management
4. **Billing Service** (Port 4001, gRPC 9001) - Billing operations
5. **Analytics Service** (Port 4002) - Analytics processing

## Infrastructure Components

- **VPC** with 2 Availability Zones
- **ECS Cluster** with Fargate tasks
- **Application Load Balancer** for API Gateway
- **RDS PostgreSQL** instances for Auth and Patient services
- **MSK Kafka Cluster** for event streaming
- **CloudWatch Log Groups** for service logs

## Troubleshooting

### Load Balancer Returns Empty Response
Run the target registration manually:
```bash
# Get API Gateway container IP
API_GATEWAY_IP=$(docker inspect <container-name> --format '{{.NetworkSettings.IPAddress}}')

# Register target
aws --endpoint-url=http://localhost:4566 elbv2 register-targets \
  --target-group-arn <target-group-arn> \
  --targets Id=$API_GATEWAY_IP,Port=4004
```

### Check Service Logs
```bash
# List ECS containers
docker ps | grep ls-ecs-PatientManagementCluster

# View logs
docker logs <container-name>
```

### Verify Services
```bash
# Check ECS services
aws --endpoint-url=http://localhost:4566 ecs list-services \
  --cluster PatientManagementCluster98E10F8D-71029507

# Check load balancers
aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers
```

## Important Notes

1. **Load Balancer DNS changes** with each deployment - update your API request files
2. **Port 4566** is required in the URL (LocalStack's edge port)
3. **Target registration** is automated in the deployment script
4. **Database connections** use `host.docker.internal` for inter-service communication
5. **Kafka** is configured but may have limited functionality in LocalStack

## Clean Up

To remove the deployment:
```bash
aws --endpoint-url=http://localhost:4566 cloudformation delete-stack \
  --stack-name patient-management
```
