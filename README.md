# HotelBooking

A Spring Boot REST API for managing hotel rooms, customer accounts, reservations, and payment records. Customers can browse rooms, check availability, and book a stay; administrators can manage rooms and update booking statuses.

This repository contains the backend only. Booking emails link to a separate frontend at `http://localhost:3000`, which is not included. The Stripe integration is in progress; see [current limitations](#current-limitations) before using the payment endpoints.

## Contents

- [Features](#features)
- [Technology stack](#technology-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Authentication and roles](#authentication-and-roles)
- [API reference](#api-reference)
- [Example workflow](#example-workflow)
- [Booking rules and data model](#booking-rules-and-data-model)
- [Responses and errors](#responses-and-errors)


## Features

- Register and log in with email and password, using BCrypt password hashing and JWT authentication.
- View and update the signed-in customer's account and retrieve their booking history.
- Browse rooms, search room information, and filter availability by dates and room type.
- Create, update, and delete rooms as an administrator, including local image uploads.
- Create reservations with date checks, an availability check, a calculated total, and a generated booking reference.
- Update booking and payment statuses as an administrator.
- Send booking confirmation and payment result emails through SMTP and store notification records.
- Create Stripe PaymentIntents and record client-reported payment results, subject to the limitations below.

SMS and WhatsApp methods are placeholders. Although several payment gateways appear in the enums, only Stripe has service and controller code.

## Technology stack

| Component | Version / purpose |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.4.10 |
| Spring Web | REST controllers and multipart requests |
| Spring Security | Stateless authentication and method-level authorization |
| Spring Data JPA / Hibernate | Relational persistence |
| MySQL | Application database |
| Spring Mail | SMTP email delivery |
| Jakarta Validation | Request and entity constraints |
| JJWT | 0.12.6; JWT signing and parsing |
| Stripe Java SDK | 28.4.0 |
| ModelMapper | 3.2.4; entity/DTO mapping |
| Lombok | 1.18.32 |
| Maven Wrapper | Downloads Maven 3.9.11 |
| Testing | Spring Boot Test and Spring Security Test |

Versions above describe this repository's `pom.xml` and wrapper configuration.

## Project structure

```text
HotelBooking/
|-- .mvn/wrapper/                 # Maven distribution configuration
|-- mvnw / mvnw.cmd               # Maven launchers for Unix / Windows
|-- pom.xml                      # Dependencies and build configuration
|-- src/main/java/com/example/HotelBooking/
|   |-- HotelBookingApplication.java
|   |-- config/                  # ModelMapper configuration
|   |-- controllers/             # Authentication, user, room, booking APIs
|   |-- dtos/                    # Request and response objects
|   |-- entities/                # JPA persistence models
|   |-- enums/                   # Roles, room types, and statuses
|   |-- exceptions/              # Application and security error handlers
|   |-- payments/stripe/         # Payment API, service, and request DTO
|   |-- repositories/            # Spring Data repositories and queries
|   |-- security/                # JWT filter, user details, and CORS
|   `-- services/                # Service interfaces and implementations
|-- src/main/resources/
|   `-- application.properties   # Local configuration; ignored by Git
|-- src/test/java/com/example/HotelBooking/
|   `-- HotelBookingApplicationTests.java
`-- product-image/               # Created on upload; ignored by Git
```

Requests pass through the security filter and controllers to services, which use repositories for database access. DTOs carry API data, while JPA entities represent stored records.

## Getting started

### 1. Prerequisites

Install JDK 21 and MySQL, and ensure `JAVA_HOME` points to your JDK. Use an SMTP account or a local SMTP test server to exercise notifications. Stripe test credentials are needed only when exercising Stripe calls, but the configured key property must exist for application startup.

The Maven Wrapper is included, so a separate Maven installation is optional. The first wrapper/build invocation needs internet access to download Maven and dependencies.

Open a terminal in the repository root and check Java:

```shell
java -version
```

### 2. Create the database

Run the following in MySQL using an account with permission to create databases and users. Replace the example password before running it.

```sql
CREATE DATABASE IF NOT EXISTS hotel_booking
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'hotel_app'@'localhost' IDENTIFIED BY 'replace-with-a-local-password';
GRANT ALL PRIVILEGES ON hotel_booking.* TO 'hotel_app'@'localhost';
```

If the user already exists, use that account's credentials instead of running `CREATE USER` again. Hibernate's `ddl-auto=update` setting creates or updates tables when the application starts; the database itself must already exist. No migration scripts or seed data are included.

### 3. Configure the application

Create `src/main/resources/application.properties`, or adapt your existing local file, using the template in [Configuration](#configuration). Replace all placeholders with your local settings. This filename is excluded by `.gitignore`.

### 4. Start the server

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

The default API address is `http://localhost:8080`. To check the running API, request `GET http://localhost:8080/api/rooms/types`; its expected body is:

```json
["SINGLE", "DOUBLE", "SUIT", "TRIPLE"]
```

There is no frontend homepage, Swagger UI, or Actuator health endpoint configured in this repository.

## Configuration

Use this local configuration template. Values in angle brackets are placeholders, not working credentials.

```properties
spring.application.name=HotelBooking
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking
spring.datasource.username=hotel_app
spring.datasource.password=<your-database-password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update

secretJwtString=<replace-with-a-random-secret-of-at-least-32-UTF-8-bytes>

spring.mail.host=<your-smtp-host>
spring.mail.port=587
spring.mail.username=<your-smtp-username>
spring.mail.password=<your-smtp-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

spring.servlet.multipart.max-file-size=2GB
spring.servlet.multipart.max-request-size=2GB

stripe.api.public.keys=<your-stripe-test-publishable-key>
stripe.api.secret.keys=<your-stripe-test-secret-key>
```

Adapt the SMTP port, authentication, and TLS settings to your provider or local test server. The JWT key is read directly as UTF-8 bytes; it is not Base64-decoded. Keep a stable signing key between restarts if existing tokens should remain valid.

**Stripe configuration issue:** `PaymentService` currently reads `stripe.api.public.keys` and supplies that value as the server API key. Before testing payments, change its `@Value` annotation to `${stripe.api.secret.keys}`. A publishable key cannot perform the server operation used here. The README documents this required code correction; it does not apply it.

For environment-based secrets, replace local property values with placeholders such as `spring.datasource.password=${DB_PASSWORD}` and `secretJwtString=${JWT_SECRET}`, then define those variables in the launch environment. A `.env` file is not automatically loaded by this project.

Room images are saved under `product-image/` relative to the process working directory. The saved `imageUrl` is an absolute filesystem path; an HTTP image-serving route is not implemented. Upload requests require a writable directory and an `image/*` content type. The local configuration allows up to 2 GB per file and per request; adjust these limits to your application's needs.

## Authentication and roles

Register a user, log in, and copy the `token` from the login response. Include it on protected requests:

```http
Authorization: Bearer <your-token>
```

| Role | Access |
| --- | --- |
| `CUSTOMER` | Own account, own booking history, booking creation, and authenticated payment endpoints |
| `ADMIN` | Customer capabilities plus room management, all users, all bookings, and booking status updates |

Registration defaults to `CUSTOMER` when `role` is omitted. There is no seeded administrator. For local development, the current registration endpoint accepts `"role": "ADMIN"`; register a separate local administrator and log in to obtain its token. Because this field is accepted on a public endpoint, role assignment must be restricted before public deployment.

JWT subjects are user email addresses. After changing an account email, log in again using the new email. The login response reports `expirationTime: "6 months"`, but the current token duration calculation is **25,920,000 milliseconds (7 hours 12 minutes)**. Use the JWT's actual expiry rather than that response label.

## API reference

Paths below are relative to `http://localhost:8080`. JSON endpoints use `Content-Type: application/json`. Room creation and update use multipart form fields. Dates use `YYYY-MM-DD`; enum values are case-sensitive.

### Authentication and users

| Method | Path | Access | Input / result |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Required: `firstName`, `lastName`, `email`, `phoneNumber`, `password`; optional: `role` |
| POST | `/api/auth/login` | Public | `email`, `password`; returns token and account metadata |
| GET | `/api/users/all` | Admin | List users |
| GET | `/api/users/account` | Authenticated | Current account |
| PUT | `/api/users/update` | Authenticated | Optional `firstName`, `lastName`, `email`, `phoneNumber` |
| DELETE | `/api/users/delete` | Authenticated | Delete current account |
| GET | `/api/users/bookings` | Authenticated | Current user's booking history |

The account update service contains password handling, but `UserDTO.password` has `@JsonIgnore`, so JSON password updates are not supported by the current DTO mapping.

### Rooms

| Method | Path | Access | Input / result |
| --- | --- | --- | --- |
| GET | `/api/rooms/all` | Public | List rooms |
| GET | `/api/rooms/{id}` | Public | Room by database ID |
| GET | `/api/rooms/types` | Public | Bare array of room types |
| GET | `/api/rooms/search` | Public | Required query parameter: `input` |
| GET | `/api/rooms/available` | Public | Required: `checkInDate`, `checkOutDate`; optional: `roomType` |
| POST | `/api/rooms/add` | Admin | Required multipart fields: `roomNumber`, `type`, `pricePerNight`, `capacity`, `description`, `imageFile` |
| PUT | `/api/rooms/update` | Admin | Required multipart field: `id`; other creation fields are optional |
| DELETE | `/api/rooms/delete/{id}` | Admin | Delete room by ID |

Room search checks room number, type, price, capacity, and description. Room numbers must be unique and at least 1; capacity must be at least 1, and the entity's minimum nightly price is 0.1.

### Bookings

| Method | Path | Access | Input / result |
| --- | --- | --- | --- |
| POST | `/api/bookings` | Customer or Admin | `roomId`, `checkInDate`, `checkOutDate` |
| GET | `/api/bookings/all` | Admin | List bookings; nested user and room fields are omitted |
| GET | `/api/bookings/{reference}` | See note below | Find a booking by reference |
| PUT | `/api/bookings/update` | Admin | Required: `id`; optional: `bookingStatus`, `paymentStatus` |

The security configuration intends to permit booking routes before method checks, but its matcher is written as `api/bookings/**` without a leading slash. The reference lookup has no method-level restriction; verify and correct that matcher before relying on anonymous lookup behavior. Supplying a valid token avoids depending on anonymous access during local testing.

### Payments

| Method | Path | Access | Input / result |
| --- | --- | --- | --- |
| POST | `/api/payments/pay` | Authenticated | `bookingReference`, `amount`; returns a raw PaymentIntent client-secret string |
| PUT | `/api/payments/pay` | Authenticated | `bookingReference`, `amount`, `transactionId`, `success`, optional `failureReason`; empty success body |

The intent service uses USD and multiplies the submitted amount by 100 to obtain cents. The update endpoint stores a payment record, updates the booking payment status, and sends email. It does not verify the submitted result with Stripe; see [current limitations](#current-limitations).

## Example workflow

The following examples use PowerShell, from a second terminal while the application is running. Replace the example email with an inbox you control to receive booking notifications.

### 1. Register and log in

```powershell
$baseUrl = 'http://localhost:8080'
$registration = @{
    firstName = 'Alex'
    lastName = 'Morgan'
    email = 'alex@example.com'
    phoneNumber = '+2348000000000'
    password = 'replace-with-a-local-password'
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/register" `
    -ContentType 'application/json' -Body $registration

$credentials = @{
    email = 'alex@example.com'
    password = 'replace-with-a-local-password'
} | ConvertTo-Json

$login = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" `
    -ContentType 'application/json' -Body $credentials
$headers = @{ Authorization = "Bearer $($login.token)" }
```

### 2. Add a room with an administrator token

Register and log in as your local administrator as described above, then replace the token and image path below. This single-line example uses `curl.exe` on Windows; use `curl` on macOS/Linux.

```shell
curl.exe -X POST "http://localhost:8080/api/rooms/add" -H "Authorization: Bearer <admin-token>" -F "roomNumber=101" -F "type=DOUBLE" -F "pricePerNight=150.00" -F "capacity=2" -F "description=Double room with a city view" -F "imageFile=@C:/path/to/room.jpg;type=image/jpeg"
```

The add response contains a success message. Fetch `/api/rooms/all` to obtain the new room's database ID; `roomNumber` and `id` are different fields.

### 3. Find availability and create a booking

```powershell
$checkIn = (Get-Date).AddDays(7).ToString('yyyy-MM-dd')
$checkOut = (Get-Date).AddDays(10).ToString('yyyy-MM-dd')
$available = Invoke-RestMethod -Uri "$baseUrl/api/rooms/available?checkInDate=$checkIn&checkOutDate=$checkOut&roomType=DOUBLE"
$available.rooms

# Continue only when the response contains an available room.
$room = $available.rooms | Select-Object -First 1
if ($null -eq $room) { throw 'No matching rooms are available.' }

$bookingBody = @{
    roomId = $room.id
    checkInDate = $checkIn
    checkOutDate = $checkOut
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$baseUrl/api/bookings" `
    -Headers $headers -ContentType 'application/json' -Body $bookingBody

$history = Invoke-RestMethod -Uri "$baseUrl/api/users/bookings" -Headers $headers
$history.bookings
```

The create-booking response currently echoes the incoming DTO, so it does not automatically include the generated ID, reference, or calculated total. Retrieve these from booking history or the confirmation email. At 150.00 per night, a three-night stay totals 450.00.

### 4. Update a booking as an administrator

Send `PUT /api/bookings/update` with an administrator token and the booking's database ID:

```json
{
  "id": 1,
  "bookingStatus": "CHECKED_IN"
}
```

### 5. Exercise the payment API in development

After correcting the Stripe key injection, send `POST /api/payments/pay` with a valid token and values from the saved booking:

```json
{
  "bookingReference": "<reference-from-booking-history>",
  "amount": 450.00
}
```

The response is a client-secret string for a separate frontend to use in its payment flow. No payment frontend is supplied. The existing `PUT /api/payments/pay` accepts a result shaped like this:

```json
{
  "bookingReference": "<reference-from-booking-history>",
  "amount": 450.00,
  "transactionId": "<stripe-payment-intent-id>",
  "success": true
}
```

This update only records the reported result; it does not itself charge a card or prove a payment succeeded.

## Booking rules and data model

- Check-in must be today or later, and check-out must be after check-in.
- Total price is `pricePerNight × number of nights`.
- New bookings start with booking status `BOOKED` and payment status `PENDING`.
- Availability excludes overlapping bookings with status `BOOKED` or `CHECKED_IN`.
- Overlap comparisons include both endpoints: a new check-in on an existing booking's check-out date is currently treated as a conflict.
- Booking references are 10 characters drawn from uppercase letters and digits 1–9, checked against stored references.
- The booking belongs to the authenticated user; creation does not accept a customer ID to book for someone else.
- The administrator update service changes statuses only; it does not reschedule dates or change rooms.

| Entity | Stored information / relationships |
| --- | --- |
| `User` | Unique email, hashed password, profile, role, active flag |
| `Room` | Unique room number, type, nightly price, capacity, description, image path |
| `Booking` | References one user and one room; dates, total, reference, statuses |
| `BookingReference` | Generated reference values with a unique constraint |
| `PaymentEntity` | User relationship, booking reference string, amount, transaction, gateway, status |
| `Notification` | Recipient, subject, body, booking reference, type, creation time |

Use the exact enum spellings currently defined in the code:

| Enum | Values |
| --- | --- |
| `UserRole` | `CUSTOMER`, `ADMIN` |
| `RoomType` | `SINGLE`, `DOUBLE`, `SUIT`, `TRIPLE` |
| `BookingStatus` | `BOOKED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCLLED` |
| `PaymentStatus` | `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`, `REVERSED` |
| `PaymentGateway` | `PAYPAL`, `STRIPE`, `PAYSTACK`, `FLUTTERWAVE` |
| `NotificationType` | `EMAIL`, `SMS`, `WHATSAPP` |

`SUIT` and `CANCLLED` are the implemented spellings. Enum values such as `REFUNDED` do not imply that gateway refund operations are implemented.

## Responses and errors

Most endpoints return a shared response envelope. Null fields are omitted, and the relevant payload appears under `user`, `users`, `room`, `rooms`, `booking`, or `bookings`. For example, an empty room list has this shape:

```json
{
  "status": 200,
  "message": "Success",
  "rooms": [],
  "timestamp": "2026-09-09T12:00:00"
}
```

The timestamp above is illustrative. Room types and payment endpoints use the different response shapes documented in the API tables. Successful controller responses generally use HTTP 200, including registration and creation operations.

| HTTP status | Current handling |
| --- | --- |
| 400 | Explicit invalid-credential, required-value, and booking-date/state exceptions |
| 401 / 403 | Authentication or authorization failures handled by Spring Security and custom handlers |
| 404 | Explicit not-found exceptions, including an unknown login email |
| 500 | Exceptions caught by the generic application handler |

There is no dedicated handler for every validation, duplicate-key, or malformed-input error. Some invalid requests may therefore return 500. JWT parsing occurs in the authentication filter, so token errors may not follow the controller response envelope.

