# Spring Cloud AWS 3 POC

A comprehensive Spring Boot 3 project demonstrating integration with multiple AWS services using Spring Cloud AWS 3.3.0.

## 🚀 **Features**

This project provides complete integration with:
- **Amazon SQS** - Message queuing and processing
- **Amazon SNS** - Pub/sub messaging and notifications  
- **Amazon S3** - Object storage and file management
- **AWS Secrets Manager** - Secure secret storage and retrieval

## 🏗️ **Architecture**

Built with clean architecture principles:
- **Controllers** - REST API endpoints for each AWS service
- **Services** - Business logic and AWS SDK integration
- **DTOs** - Type-safe request/response models
- **Configuration** - AWS client setup and Spring configuration

## 📋 **Available Endpoints**

### **SQS (Simple Queue Service)**
```bash
POST /api/sqs/send          # Send message to queue
GET  /api/sqs/health        # SQS health check
```

### **SNS (Simple Notification Service)**
```bash
POST /api/sns/publish                    # Publish message to topic
GET  /api/sns/topic/{topicArn}/exists    # Check topic existence
GET  /api/sns/health                     # SNS health check
```

### **S3 (Simple Storage Service)**
```bash
POST   /api/s3/upload                           # Upload object
POST   /api/s3/upload-file                      # Upload file (multipart)
GET    /api/s3/download/{bucket}/{key}          # Download object
DELETE /api/s3/{bucket}/{key}                   # Delete object
GET    /api/s3/list/{bucket}                    # List objects
GET    /api/s3/bucket/{bucket}/exists           # Check bucket existence
GET    /api/s3/health                           # S3 health check
```

### **Secrets Manager**
```bash
POST   /api/secrets/create              # Create secret
GET    /api/secrets/{name}              # Get secret value
PUT    /api/secrets/update              # Update secret
DELETE /api/secrets/{name}              # Delete secret
GET    /api/secrets/{name}/exists       # Check secret existence
GET    /api/secrets/health              # Secrets Manager health check
```

## 🔧 **Technology Stack**

- **Spring Boot 3.4.6** - Latest Spring Boot version
- **Spring Cloud AWS 3.3.0** - Latest AWS integration
- **Java 17** - Modern Java LTS version
- **AWS SDK v2** - Latest AWS SDK
- **Maven** - Dependency management
- **Swagger/OpenAPI** - API documentation
- **JUnit 5** - Testing framework

## 🚀 **Quick Start**

### **Prerequisites**
- Java 17+
- Maven 3.6+
- AWS Account with appropriate permissions

### **Setup AWS Credentials**

Choose one of the following methods:

**Option 1: Environment Variables**
```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

**Option 2: AWS CLI**
```bash
aws configure
```

**Option 3: IAM Roles** (when running on EC2)
- Attach appropriate IAM role to your EC2 instance

### **Run the Application**

```bash
# Clone and navigate to project
cd spring-cloud-aws-3-poc

# Run the application
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/spring-cloud-aws-3-poc-1.0.0.jar
```

### **Access the Application**

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## 📝 **Usage Examples**

### **Send SQS Message**
```bash
curl -X POST http://localhost:8080/api/sqs/send \
  -H "Content-Type: application/json" \
  -d '{
    "queueName": "my-queue",
    "messageBody": "Hello from Spring Cloud AWS!",
    "delaySeconds": 0
  }'
```

### **Publish SNS Message**
```bash
curl -X POST http://localhost:8080/api/sns/publish \
  -H "Content-Type: application/json" \
  -d '{
    "topicArn": "arn:aws:sns:us-east-1:123456789012:my-topic",
    "message": "Hello from SNS!",
    "subject": "Test Message"
  }'
```

### **Upload to S3**
```bash
curl -X POST http://localhost:8080/api/s3/upload \
  -H "Content-Type: application/json" \
  -d '{
    "bucketName": "my-bucket",
    "objectKey": "test-file.txt",
    "content": "SGVsbG8gUzMh",
    "contentType": "text/plain"
  }'
```

### **Create Secret**
```bash
curl -X POST http://localhost:8080/api/secrets/create \
  -H "Content-Type: application/json" \
  -d '{
    "secretName": "my-secret",
    "secretValue": "super-secret-value",
    "description": "Test secret"
  }'
```

## 🧪 **Testing**

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=SqsControllerTest

# Build without tests
./mvnw clean package -DskipTests
```

## 📦 **Project Structure**

```
src/main/java/com/javaninja/
├── SpringCloudAwsApplication.java     # Main application
├── controller/                        # REST controllers
│   ├── SqsController.java
│   ├── SnsController.java
│   ├── S3Controller.java
│   └── SecretsManagerController.java
├── service/                           # Business logic
│   ├── SqsService.java
│   ├── SnsService.java
│   ├── S3Service.java
│   └── SecretsManagerService.java
├── model/dto/                         # Data transfer objects
│   ├── SqsMessageRequest.java
│   ├── SnsMessageRequest.java
│   ├── S3ObjectRequest.java
│   └── SecretsManagerRequest.java
└── config/                            # Configuration
    └── AwsConfig.java
```

## 🔒 **Security**

- No authentication required for development/testing
- Uses AWS IAM for service-to-service authentication
- Secrets are handled securely through AWS Secrets Manager
- Input validation on all endpoints

## 🌟 **Features**

- **Comprehensive AWS Integration** - 4 major AWS services
- **Production Ready** - Error handling, logging, validation
- **Well Documented** - Swagger UI and comprehensive README
- **Testable** - Unit and integration tests included
- **Extensible** - Easy to add more AWS services
- **Modern Stack** - Latest Spring Boot and AWS SDK versions

## 📚 **Additional Resources**

- [Spring Cloud AWS Documentation](https://docs.awspring.io/)
- [AWS SDK for Java Documentation](https://docs.aws.amazon.com/sdk-for-java/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)

## 🤝 **Contributing**

This is a proof of concept project demonstrating AWS integration patterns. Feel free to extend it with additional AWS services or features!

## 📄 **License**

This project is for educational and demonstration purposes.

