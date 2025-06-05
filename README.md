# Spring Cloud AWS 3 POC

A comprehensive Spring Boot 3 application demonstrating Spring Cloud AWS 3.0 integration with all supported AWS services.

## Features

- **Spring Boot 3.4.6** with **Java 17**
- **Spring Cloud AWS 3.3.0** integration
- **AWS Services**: SQS, SNS, S3, DynamoDB, SES, Secrets Manager, Parameter Store, CloudWatch
- **Security**: Basic authentication with role-based access control
- **API Documentation**: Swagger/OpenAPI integration
- **Testing**: JUnit 5 with TestContainers support
- **Production Ready**: Actuator endpoints for monitoring

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- AWS Account (for production) or LocalStack (for local testing)

### Running the Application

1. **Clone/Download the project**
2. **Open in IntelliJ IDEA**:
   - File → Open → Select the project folder
   - IntelliJ will automatically detect it as a Maven project
   - Wait for dependencies to download

3. **Configure AWS Credentials** (choose one):
   
   **Option A: Environment Variables**
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   export AWS_DEFAULT_REGION=us-east-1
   ```
   
   **Option B: AWS CLI**
   ```bash
   aws configure
   ```
   
   **Option C: IAM Roles** (when running on EC2)

4. **Run the Application**:
   - In IntelliJ: Right-click on `SpringCloudAwsApplication.java` → Run
   - Or use Maven: `mvn spring-boot:run`

5. **Access the Application**:
   - Application: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Authentication
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: `ADMIN` (required for AWS endpoints)

## API Endpoints

### General Endpoints
- `GET /api/health` - Application health check
- `GET /api/info` - Application information
- `POST /api/test` - Test endpoint

### SQS Endpoints
- `POST /api/sqs/send` - Send message to SQS queue
- `GET /api/sqs/health` - SQS service health check

### Future Endpoints
The project structure is ready for additional AWS service endpoints:
- SNS (Simple Notification Service)
- S3 (Simple Storage Service)
- DynamoDB (NoSQL Database)
- SES (Simple Email Service)
- Secrets Manager
- Parameter Store
- CloudWatch

## Project Structure

```
src/
├── main/
│   ├── java/com/example/springcloudaws/
│   │   ├── SpringCloudAwsApplication.java     # Main application
│   │   ├── config/
│   │   │   └── SecurityConfig.java            # Security configuration
│   │   ├── controller/
│   │   │   ├── GeneralController.java         # General endpoints
│   │   │   └── SqsController.java             # SQS endpoints
│   │   ├── service/
│   │   │   └── SqsService.java                # SQS business logic
│   │   └── model/dto/
│   │       └── SqsDto.java                    # Data transfer objects
│   └── resources/
│       └── application.yml                    # Configuration
└── test/
    └── java/com/example/springcloudaws/
        └── SpringCloudAwsApplicationTest.java # Basic test
```

## Configuration

### AWS Services Configuration
Edit `application.yml` to configure your AWS resources:

```yaml
aws:
  services:
    sqs:
      queues:
        user-events: your-queue-name
        # Add more queues as needed
    s3:
      buckets:
        documents: your-bucket-name
        # Add more buckets as needed
```

### Security Configuration
- Basic authentication is enabled by default
- Admin user: `admin/admin123`
- Regular user: `user/user123`
- Modify `SecurityConfig.java` to customize security settings

## Development

### Adding New AWS Services

1. **Add Dependencies**: Update `pom.xml` with required Spring Cloud AWS starters
2. **Create DTOs**: Add data transfer objects in `model/dto/`
3. **Create Service**: Add business logic in `service/`
4. **Create Controller**: Add REST endpoints in `controller/`
5. **Update Configuration**: Add service-specific configuration in `application.yml`

### Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=SpringCloudAwsApplicationTest
```

### Building

```bash
# Build JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

## Deployment

### Local Development
1. Use LocalStack for AWS service emulation
2. Set `spring.cloud.aws.endpoint=http://localhost:4566`

### AWS Deployment
1. Deploy to EC2 with IAM roles
2. Use AWS ECS/EKS for containerized deployment
3. Configure Application Load Balancer for high availability

## Monitoring

- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Application Info**: `/actuator/info`

## Security Considerations

- Use IAM roles instead of access keys in production
- Enable HTTPS in production
- Configure proper CORS settings
- Use AWS Secrets Manager for sensitive configuration
- Enable CloudWatch logging and monitoring

## Troubleshooting

### Common Issues

1. **AWS Credentials Not Found**
   - Ensure AWS credentials are properly configured
   - Check environment variables or AWS CLI configuration

2. **Access Denied**
   - Verify IAM permissions for AWS services
   - Check security group settings

3. **Connection Timeout**
   - Verify AWS region configuration
   - Check network connectivity

### Logs
Check application logs for detailed error information:
```bash
tail -f logs/spring-cloud-aws-poc.log
```

## Support

For issues and questions:
1. Check the application logs
2. Verify AWS service configuration
3. Review Spring Cloud AWS documentation
4. Check AWS service status

## License

This project is licensed under the MIT License.

