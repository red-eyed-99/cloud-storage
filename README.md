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
 - Docker
 - Kubernetes
## Front-end
A ready-made [frontend](https://github.com/zhukovsd/cloud-storage-frontend/) was used with some modifications.
## How to run a project locally
### Docker
1. Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. Clone repository
    ```
    git clone https://github.com/red-eyed-99/cloud-storage.git
    ```
3. Go to **docker** folder from project root and run **compose.yaml**
   ```
      cd docker
      docker compose up -d
   ```
4. Now the web application should be accessible at http://localhost:8080 and swagger-ui at http://localhost:8081/swagger-ui/index.html

> [!NOTE]
> To run the back-end via IDE, simply stop the back-end container in docker and run it.

### Kubernetes
1. Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/).
2. Run kubernetes cluster via Docker Desktop.
3. Apply file to run nginx ingress controller.
   ```
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
   ```
4. Open /k8s/app folder from root project and apply manifest files.
   ```
   cd /k8s/app
   kubectl apply -R -f . 
   ```
5. Now the web application should be accessible at http://localhost:80 and swagger-ui at http://localhost:80/swagger-ui/index.html

#### Kubernetes Dashboard
1. Apply the manifest to launch the required resources for the dashboard.
   ```
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.2.0/aio/deploy/recommended.yaml
   ```
2. Run a proxy in a separate terminal to gain access to dashboard.
   ```
   kubectl proxy
   ```
3. The dashboard is now available on http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
4. Apply manifests to create an administrator role.
   ```
   cd ../dashboard
   kubectl apply -f .
   ```
5. Get an authentication token.
- Temporary
  ```
  kubectl create token admin-user
  ```
- Permanent
   ```
   kubectl get secret admin-user -o go-template='{{.data.token | base64decode}}'
   ```
