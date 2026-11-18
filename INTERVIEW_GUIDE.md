# Interview guide (Web version)

Read each answer, then say it in your own words. Run the app and click every feature before an interview.

**1. Explain the project in 30 seconds.**
A full-stack Employee Management System. The backend is a Spring Boot REST API with JWT login and three roles. Data is stored in MySQL through Spring Data JPA. The front end is plain JavaScript that calls the API. I added validation, an activity log, and automated tests that run on GitHub Actions.

**2. How is the backend organised?**
Controller, Service, Repository layers. Controllers handle HTTP and validation, services hold business rules, repositories talk to the database. Request and response objects (DTOs) are separate from the database entities.

**3. What is dependency injection?**
Spring creates the objects (beans) and passes them into constructors. For example `EmployeeController` receives an `EmployeeService`, so I never write `new EmployeeService()`. It makes code loosely coupled and easy to test.

**4. How does login work?**
The user posts username and password to `/api/auth/login`. The server checks the password against the BCrypt hash and returns a JWT. The browser sends that token in the `Authorization` header on every request. A filter (`JwtAuthFilter`) validates the token and tells Spring Security who the user is.

**5. What is a JWT?**
A signed string with three parts: header, payload (username, role, expiry) and signature. The server signs it with a secret key. If someone changes the payload, the signature no longer matches and the token is rejected. The server stays stateless, with no session stored.

**6. How are roles enforced?**
`@PreAuthorize("hasAnyRole('ADMIN','HR')")` on controller methods. A viewer who calls them gets HTTP 403. The front end also hides the buttons, but the real protection is on the server.

**7. Why BCrypt for passwords?**
It is a slow, salted hash made for passwords. Even if the database leaks, passwords cannot be read back, and the same password gives a different hash each time.

**8. Why DTOs instead of returning entities?**
The API contract stays stable and I control exactly what is exposed. It also avoids leaking fields like password hashes and lets me add validation rules on the request.

**9. How do you validate input?**
Bean Validation annotations (`@NotBlank`, `@Pattern`, `@Email`, `@PastOrPresent`) on the request records, triggered by `@Valid`. Failures return HTTP 400 with a message per field, through `GlobalExceptionHandler`.

**10. How do you handle errors?**
Custom exceptions (`NotFoundException`, `ConflictException`, ...) are thrown in the service and turned into JSON with the right status code (404, 409, ...) by `@RestControllerAdvice`.

**11. What is JPA / Hibernate?**
JPA is the standard for mapping Java objects to database tables. Hibernate is the implementation. `Employee` is an `@Entity`, and `JpaRepository` gives me save, find and delete without writing SQL. With `ddl-auto=update` it creates the tables.

**12. How did you test it?**
Unit tests for salary calculation, password rules and JWT. Integration tests with MockMvc and an in-memory H2 database call the real API: login, access control by role, validation, duplicate email, delete. GitHub Actions runs them on every push.

**13. What would you improve?**
Pagination, attendance and leave modules, Docker and deployment, refresh tokens, and rate limiting on login.

**Be honest about the origin.** If asked who built it, say you used AI assistance to build the base and that you studied, ran, and extended it. Never claim you wrote every line yourself.
