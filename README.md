# Cloud Storage
This web-application is a multi-user file cloud. Users can use it to upload and store files.
## Application functionality
 - The ability to create a personal user account and authenticate users using sessions
 - Uploading and downloading files and folders
 - Creating empty directories
 - The ability to view, rename, move, delete and search files and folders
## Back-end technology stack
 - Java 21
 - Spring Boot 4.0.0 (Web, JPA, Security, Test)
 - PostgreSQL 18.1
 - Liquibase
 - Redis
 - Minio
 - Lombok, Mapstruct
 - Testcontainers
## Front-end
A ready-made [frontend](https://github.com/zhukovsd/cloud-storage-frontend/) was used with some modifications.
## How to run a project locally
1. Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. Clone repository
    ```
    git clone https://github.com/red-eyed-99/cloud-storage.git
    ```
3. Go to **docker** folder and run **compose.yaml**
   ```
      cd docker
      docker compose up -d
   ```
4. Now the web application should be accessible at http://localhost:8080 and swagger-ui at http://localhost:8081/swagger-ui/index.html

> To run the back-end via IDE, simply stop the back-end container in docker and run it.
