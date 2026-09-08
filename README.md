# ☁️ StorageContainer

A full-stack cloud storage application inspired by modern cloud-storage platforms such as Google Drive.

StorageContainer allows users to securely upload, organize, manage, share, and download files and folders through a clean web-based interface. The application provides authentication, role-based access control, folder management, file management, sharing permissions, and cloud-based file storage.

---

## 🚀 Features

### 🔐 Authentication & Authorization

* User registration and login
* JWT-based authentication
* Password encryption using BCrypt
* Google OAuth2 login
* Secure authentication using Spring Security
* Role-based authorization
* Authenticated user information endpoint
* Protected frontend routes

### 📁 Folder Management

* Create folders
* Create nested folders
* View root folders
* Navigate through nested folders
* View folder contents
* Rename folders
* Delete folders
* Track folder ownership
* Parent-child folder relationship

### 📄 File Management

* Upload files
* Store files in cloud storage
* View files inside folders
* Download files
* Rename files
* Delete files
* Store file metadata
* Track file ownership
* Support files stored inside nested folders

### 🤝 File & Folder Sharing

Users can share files and folders with other registered users.

Supported permissions:

* 👁️ `VIEWER`
* ✏️ `EDITOR`

Sharing functionality includes:

* Share files with users by email
* Share folders with users by email
* View items shared with the logged-in user
* Permission-based access
* Owner-based authorization

### 🛡️ Access Control

StorageContainer follows permission-based access control.

| User     | VIEWER              | EDITOR              | OWNER |
| -------- | ------------------- | ------------------- | ----- |
| View     | ✅                   | ✅                   | ✅     |
| Download | ✅                   | ✅                   | ✅     |
| Rename   | ❌                   | ✅                   | ✅     |
| Delete   | ❌                   | ✅                   | ✅     |
| Share    | Based on permission | Based on permission | ✅     |

---

# 🏗️ Architecture

StorageContainer follows a **full-stack client-server architecture**.

```text
                    ┌──────────────────────┐
                    │      React UI        │
                    │      Frontend        │
                    └──────────┬───────────┘
                               │
                               │ REST API
                               ▼
                    ┌──────────────────────┐
                    │    Spring Boot       │
                    │      Backend         │
                    └──────────┬───────────┘
                               │
               ┌───────────────┼────────────────┐
               │               │                │
               ▼               ▼                ▼
       ┌──────────────┐ ┌──────────────┐ ┌───────────────┐
       │ PostgreSQL   │ │ Supabase     │ │ Spring        │
       │ Database     │ │ Storage      │ │ Security      │
       └──────────────┘ └──────────────┘ └───────────────┘
                                                │
                                                ▼
                                         ┌───────────────┐
                                         │ JWT / Google  │
                                         │ OAuth2        │
                                         └───────────────┘
```

---

# 🛠️ Technology Stack

## Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* REST APIs
* JWT
* OAuth2 / Google Authentication
* Lombok
* Maven

## Frontend

* React
* JavaScript
* Axios
* CSS
* Vite
* React Router

## Database

* PostgreSQL

## Cloud Storage

* Supabase Storage

## Development Tools

* IntelliJ IDEA
* Git
* GitHub
* GitHub Desktop
* Postman
* npm

---

# 📂 Project Structure

## Backend

```text
StorageContainer/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── cloudBasedStorage/
│   │   │           ├── controller/
│   │   │           │
│   │   │           ├── service/
│   │   │           │
│   │   │           ├── repository/
│   │   │           │
│   │   │           ├── model/
│   │   │           │
│   │   │           ├── dto/
│   │   │           │
│   │   │           ├── security/
│   │   │           │
│   │   │           └── config/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

## Frontend

```text
frontend/
│
├── src/
│   ├── components/
│   │
│   ├── pages/
│   │
│   ├── services/
│   │
│   ├── css/
│   │
│   ├── App.jsx
│   └── main.jsx
│
├── package.json
└── vite.config.js
```

---

# 🗄️ Database Design

The application uses PostgreSQL to store user information, file metadata, folder information, sharing details, and activity information.

Main tables include:

```text
users
folders
files
file_versions
shares
link_shares
stars
activities
```

### Users

Stores registered user information.

Important fields include:

```text
user_id
user_email
user_password
user_role
```

User roles include:

```text
EDITOR
VIEWER
PUBLIC_USER
```

---

### Folders

Stores folder information and supports nested folder structures.

```text
id
folder_name
parent_folder
created_by
created_at
updated_at
```

The `parent_folder` relationship allows folders to be nested inside other folders.

Example:

```text
My Drive
│
├── Documents
│   ├── Java
│   ├── Spring Boot
│   └── Projects
│
├── Photos
│   ├── 2025
│   └── 2026
│
└── Resume
```

---

### Files

Stores file metadata.

```text
id
file_name
file_path
file_size
file_type
created_by
folder
```

The actual file content is stored in cloud storage while PostgreSQL stores the associated metadata.

---

### Shares

Stores file/folder sharing information.

```text
id
email
item_id
item_type
permission
shared_by
```

Supported item types:

```text
FILE
FOLDER
```

Supported permissions:

```text
VIEWER
EDITOR
```

---

# 🔑 Authentication Flow

StorageContainer uses JWT authentication for API security.

### Login

```text
User
 │
 ▼
Login
 │
 ▼
Spring Security
 │
 ▼
Validate credentials
 │
 ▼
Generate JWT
 │
 ▼
Return JWT to frontend
 │
 ▼
Frontend stores authentication information
 │
 ▼
JWT sent with protected API requests
```

Requests to protected APIs contain:

```http
Authorization: Bearer <JWT_TOKEN>
```

The backend validates the token before allowing access to protected resources.

---

# 🔵 Google OAuth2 Login

StorageContainer also supports Google authentication.

```text
User
 │
 ▼
Login with Google
 │
 ▼
Google OAuth2
 │
 ▼
OAuth2 Success Handler
 │
 ▼
Find/Create User
 │
 ▼
Generate JWT
 │
 ▼
Frontend
```

This allows users to authenticate without manually entering a password.

---

# 📡 API Endpoints

## Authentication

### Register

```http
POST /api/auth/register
```

Creates a new user account.

### Login

```http
POST /api/auth/login
```

Authenticates the user and generates a JWT.

### Current User

```http
GET /api/auth/me
```

Returns information about the currently authenticated user.

---

# 📁 Folder APIs

Example folder operations:

```http
GET /api/folders
```

Get folders available to the user.

```http
GET /api/folders/root
```

Get root folders.

```http
GET /api/folders/{folderId}/children
```

Get child folders.

```http
POST /api/folders
```

Create a folder.

```http
PUT /api/folders/{folderId}
```

Rename/update a folder.

```http
DELETE /api/folders/{folderId}
```

Delete a folder.

---

# 📄 File APIs

Typical file operations include:

```http
POST /api/files/upload
```

Upload a file.

```http
GET /api/files
```

Retrieve files.

```http
GET /api/files/{fileId}
```

Retrieve a specific file.

```http
DELETE /api/files/{fileId}
```

Delete a file.

Download functionality is provided through the file/storage service.

---

# 🤝 Sharing APIs

### Share an item

```http
POST /api/shares
```

Shares a file or folder with another user.

### Get items shared with me

```http
GET /api/shares/shared-with-me
```

Returns files and folders shared with the logged-in user.

### Get all shares

```http
GET /api/shares
```

Returns sharing information.

Example request:

```json
{
  "email": "user@example.com",
  "itemId": 10,
  "itemType": "FILE",
  "permission": "VIEWER"
}
```

---

# ☁️ Supabase Storage

StorageContainer uses Supabase Storage to store uploaded files.

The application separates:

```text
File metadata
       ↓
PostgreSQL
```

from:

```text
Actual file
       ↓
Supabase Storage
```

Example:

```text
PostgreSQL

file_id       → 101
file_name     → resume.pdf
file_size     → 245KB
file_type     → application/pdf
file_path     → 252/abc123/resume.pdf
```

while the actual file is stored in the configured Supabase bucket.

---

# 🔒 Security

Security is implemented using:

* Spring Security
* JWT authentication
* BCrypt password hashing
* OAuth2
* Authentication filters
* Role/permission-based authorization
* Ownership checks
* Protected REST endpoints

Sensitive configuration values should **never be committed to GitHub**.

For example:

```properties
spring.datasource.url=...
spring.datasource.username=...
spring.datasource.password=...

jwt.secret=...

spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...

supabase.url=...
supabase.key=...
```

Use environment variables or a local configuration file for secrets.

---

# ⚙️ Installation & Setup

## 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/StorageContainer.git
```

Move into the project:

```bash
cd StorageContainer
```

---

# 2. Configure PostgreSQL

Create a PostgreSQL database.

Example:

```sql
CREATE DATABASE storagecontainer;
```

Configure the database connection using environment variables or your local `application.properties`.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/storagecontainer
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# 3. Configure Supabase

Create a Supabase project and storage bucket.

Example configuration:

```properties
supabase.url=YOUR_SUPABASE_URL
supabase.key=YOUR_SUPABASE_KEY
supabase.bucket=YOUR_BUCKET_NAME
```

Do not commit secret keys to GitHub.

---

# 4. Configure JWT

Add a secure JWT secret through environment variables or local configuration.

Example:

```properties
jwt.secret=YOUR_SECRET_KEY
```

---

# 5. Configure Google OAuth2

Create OAuth credentials in Google Cloud Console.

Configure:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

Configure the appropriate redirect URI for your application.

---

# 6. Start the Backend

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Or run the main Spring Boot application from IntelliJ IDEA.

The backend runs on:

```text
http://localhost:8080
```

---

# 7. Start the Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will normally be available at:

```text
http://localhost:5173
```

---

# 🖥️ Application Workflow

```text
                    ┌─────────────┐
                    │    Login    │
                    └──────┬──────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │ Authentication  │
                  └────────┬────────┘
                           │
                           ▼
                     ┌───────────┐
                     │ Dashboard │
                     └─────┬─────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
          Folders        Files       Shared
              │            │            │
              ▼            ▼            ▼
           Create       Upload       Receive
           Rename       Download     Shared Items
           Delete       Rename
                        Delete
```

---

# 📸 Main Application Screens

The application provides a cloud-storage style interface with:

* Login/Register page
* Google login
* Dashboard
* Folder navigation
* New Folder modal
* File upload
* File/folder three-dot menu
* Rename modal
* Delete confirmation modal
* Share modal
* Shared With Me page
* Viewer/Editor permissions

---

# 🧩 Key Backend Components

Important backend layers:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

For file operations:

```text
FileController
      ↓
FileService
      ↓
StoredFileRepository
      ↓
SupabaseStorageService
      ↓
Supabase Storage
```

For sharing:

```text
ShareController
      ↓
ShareService
      ↓
ShareRepository
      ↓
PostgreSQL
```

---

# 🧠 Design Principles

The project follows several important backend development practices:

### Separation of Concerns

Controllers handle HTTP requests.

Services contain business logic.

Repositories handle database operations.

Security components handle authentication and authorization.

---

### DTO-Based API Communication

DTOs are used to transfer data between the frontend and backend without exposing unnecessary entity details.

---

### Ownership Validation

Operations such as delete and rename verify that the user has the required permissions before modifying resources.

---

### Cloud Storage Separation

The application stores:

```text
Metadata → PostgreSQL
Files    → Supabase Storage
```

This keeps database records lightweight while allowing files to be stored in cloud storage.

---

# 🔮 Future Enhancements

Possible future improvements include:

* Public shareable URLs
* Link-based file sharing
* File version history
* Star/favorite files
* Activity history
* Search files and folders
* File preview
* Image preview
* Drag-and-drop upload
* Storage usage dashboard
* Pagination
* Advanced exception handling
* Global error handling
* Unit tests
* Integration tests
* Docker deployment
* CI/CD pipeline
* Production deployment
* File version management
* Email notifications for shared files
* Trash/recycle bin

---

# 🧪 Testing

API testing can be performed using Postman.

Important scenarios include:

```text
✓ Register user
✓ Login user
✓ JWT validation
✓ Create folder
✓ Create nested folder
✓ Upload file
✓ Download file
✓ Rename file
✓ Rename folder
✓ Delete file
✓ Delete folder
✓ Share file
✓ Share folder
✓ Viewer permission
✓ Editor permission
✓ Owner permission
✓ Unauthorized access
```

---

# 🛡️ Important Security Notes

Never commit the following to GitHub:

```text
application.properties
.env
JWT secrets
Database passwords
Supabase service keys
Google OAuth client secrets
API keys
```

Add sensitive files to `.gitignore`.

Example:

```gitignore
# Environment
.env
.env.*

# Spring configuration
application-local.properties
application-prod.properties

# IDE
.idea/
*.iml

# Build
target/
node_modules/

# Logs
*.log
```

If a secret has already been pushed to GitHub, **rotating/revoking the exposed secret is important even after deleting the file from the repository history**.

---

# 📌 Project Status

## Current Status

🟢 Core cloud-storage functionality implemented.

### Completed

* [x] User registration
* [x] User login
* [x] JWT authentication
* [x] Google OAuth2
* [x] Folder creation
* [x] Nested folders
* [x] Folder navigation
* [x] File upload
* [x] Cloud file storage
* [x] File listing
* [x] File download
* [x] File deletion
* [x] Folder deletion
* [x] File rename
* [x] Folder rename
* [x] File/folder sharing
* [x] Viewer permission
* [x] Editor permission
* [x] Shared With Me
* [x] Premium cloud-storage style UI

### Planned

* [ ] Public URL sharing
* [ ] File versioning
* [ ] Search
* [ ] Star/favorite
* [ ] Activity tracking
* [ ] Advanced exception handling
* [ ] Automated testing
* [ ] Production deployment

---

# 👩‍💻 Author

**Gowthami C P**

Java Backend Developer

### Technical Interests

* Java
* Spring Boot
* REST APIs
* Spring Security
* PostgreSQL
* React
* Microservices
* Cloud Storage

---

# ⭐ Project Highlights

StorageContainer demonstrates practical experience with:

```text
Java
  ↓
Spring Boot
  ↓
REST API
  ↓
Spring Security
  ↓
JWT + OAuth2
  ↓
PostgreSQL
  ↓
Supabase Cloud Storage
  ↓
React
```

The project focuses on building a real-world cloud storage system rather than a basic CRUD application, including authentication, authorization, file storage, folder hierarchy, ownership, sharing, and permission management.

---

## 📄 License

This project is developed for educational and portfolio purposes.
