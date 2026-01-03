### Adding JWT Feature  

First you need a users table to store the data.

```
CREATE TABLE users (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  email NVARCHAR(255) UNIQUE NOT NULL,
  username NVARCHAR(100) UNIQUE NOT NULL,
  password_hash NVARCHAR(255) NOT NULL,
  created_at DATETIME2 DEFAULT SYSDATETIME(),
  updated_at DATETIME2 DEFAULT SYSDATETIME()
);
```
When we are updating the row we need to update the updated_at row. For this we will be using triggers.

```
CREATE TRIGGER trg_users_updated_at
ON users
AFTER UPDATE
AS
BEGIN
  SET NOCOUNT ON;

  UPDATE users
  SET updated_at = SYSDATETIME()
  FROM inserted
  WHERE users.id = inserted.id;
END;
```

Adding Refresh token Table

```
CREATE TABLE refresh_tokens (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash NVARCHAR(255) NOT NULL,
  expires_at DATETIME2 NOT NULL,
  revoked BIT DEFAULT 0,
  CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id) REFERENCES users(id)
);

```
revoked - this is a flag which stores 0 or 1. when logout, or password change, revoke access happens we make it 1.

You can find all the folders related to authenthication in auth package. For now to keep it simple, I have only register, login and refresh token feature.
To enable this JWT we need to add few dependencies
```
<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-api</artifactId>
			<version>0.13.0</version>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-impl</artifactId>
			<version>0.13.0</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-jackson</artifactId>
			<version>0.13.0</version>
			<scope>runtime</scope>
		</dependency>
```

We defined 3 things to enable JWT.
 - Security Config
 - JWT Util
 - JWT Filter

## Why we need SecurityConfig

SecurityConfig defines who can access what in your application. It tells Spring Security which endpoints are public (like /auth/login) and which ones require authentication. It also disables features you don’t need for JWT (like sessions, form login, and HTTP basic auth) and enables stateless behavior. Most importantly, it enforces rules like “every request except /auth/** must be authenticated,” and it relies on the SecurityContext (set by the JWT filter) to decide whether a request is allowed or rejected with a 403.

## Why we need a JWT filter

Spring Security does not understand JWT by default, so the JWT filter exists to bridge that gap. The JwtFilter runs once per request, extracts the JWT from the Authorization header, validates it, and if valid, creates an Authentication object and stores it in the SecurityContext. This is how Spring Security learns who the user is for that request. Without this filter, Spring would always think the user is unauthenticated, even if they sent a valid JWT.

## Why we need JwtUtil

JwtUtil is responsible for all JWT-related cryptographic work. It creates tokens (signing them with a secret key), validates tokens, checks expiration, and extracts claims like the user’s email. Keeping this logic in a dedicated utility class ensures clean separation of concerns: JwtUtil handles token logic, the filter handles request processing, and the security config handles access rules. This separation makes the system easier to maintain, test, and secure.

Now this is done. I have a use case, where I need to set a unique Id for each request.
We will create a Filter for it and add it in the Security Config