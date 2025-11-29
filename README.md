# Smart Parking System

A comprehensive web-based parking management system built with Spring Boot that streamlines parking operations, customer management, and revenue tracking.

![Version](https://img.shields.io/badge/version-1.0-brightgreen)
![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Contributors](#contributors)

## Features

### Core Functionality
- **Real-time Parking Management**: Park and exit vehicles with automatic space allocation
- **Multi-floor Support**: 3-floor parking structure with 20 spaces per floor
- **Reservation System**: Book parking spaces in advance
- **Customer Management**: Register and manage customer profiles
- **Loyalty Program**: Earn points (1 point per ₹10 spent) with 10% discount at 100 points

### Advanced Features
- **Multiple Payment Methods**: Cash, Card, and UPI support
- **Weekend Pricing**: 1.2x pricing multiplier for Saturdays and Sundays
- **Priority Parking**: Reserved spaces for handicapped customers
- **Vehicle Type Support**: Different rates for bikes, cars, and trucks
- **Visual Parking Lot**: Interactive 3-floor visualization showing space availability
- **Reports & Analytics**: Revenue tracking and parking statistics dashboard

### Business Logic
- Dynamic pricing based on:
  - Vehicle type
  - Parking duration
  - Day of the week (weekend pricing)
  - Customer loyalty points
- Automatic space allocation with preference for handicapped spaces
- Real-time availability tracking across all floors

## 🛠️ Technology Stack

### Backend
- **Java 17+**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Data JPA
  - Spring Boot DevTools
- **Thymeleaf** - Server-side templating
- **H2 Database** - In-memory database (can be configured for persistent storage)

### Frontend
- **HTML5 / CSS3**
- **JavaScript (Vanilla)**
- **Font Awesome 6.5.2** - Icons
- **Responsive Design** - Mobile-friendly interface

### Build Tool
- **Maven** - Dependency management and build automation

## Project Structure

```
src/main/
├── java/com/parking/
│   ├── controller/
│   │   └── ParkingWebController.java      # Main REST controller
│   ├── model/
│   │   ├── Customer.java                  # Customer entity
│   │   ├── Vehicle.java                   # Vehicle entity
│   │   ├── ParkingSpace.java              # Parking space entity
│   │   ├── ParkingTicket.java             # Ticket entity
│   │   ├── Reservation.java               # Reservation entity
│   │   ├── VehicleType.java               # Enum for vehicle types
│   │   ├── SpaceStatus.java               # Enum for space status
│   │   ├── TicketStatus.java              # Enum for ticket status
│   │   ├── PaymentMethod.java             # Enum for payment methods
│   │   └── AvailabilityStats.java         # DTO for statistics
│   ├── repository/
│   │   ├── CustomerRepository.java        # Customer data access
│   │   ├── VehicleRepository.java         # Vehicle data access
│   │   ├── ParkingSpaceRepository.java    # Space data access
│   │   ├── ParkingTicketRepository.java   # Ticket data access
│   │   └── ReservationRepository.java     # Reservation data access
│   ├── service/
│   │   ├── ParkingService.java            # Core business logic
│   │   └── FileStorageService.java        # File handling service
│   └── ParkingWebApplication.java         # Main application class
├── resources/
│   ├── templates/
│   │   ├── index.html                     # Home page
│   │   ├── park.html                      # Park vehicle page
│   │   ├── park-result.html               # Parking confirmation
│   │   ├── exit.html                      # Exit vehicle page
│   │   ├── exit-payment.html              # Payment processing
│   │   ├── exit-result.html               # Exit confirmation
│   │   ├── reserve.html                   # Reservation page
│   │   ├── reserve-result.html            # Reservation confirmation
│   │   ├── customer-register.html         # Customer registration
│   │   ├── customer-success.html          # Registration success
│   │   ├── customers-list.html            # Customer management
│   │   ├── reservations-list.html         # Reservation management
│   │   ├── tickets.html                   # Ticket history
│   │   ├── reports.html                   # Analytics dashboard
│   │   ├── visualize.html                 # Visual parking lot
│   │   └── error.html                     # Error page
│   └── application.properties             # Application configuration
└── pom.xml                                 # Maven dependencies
```

## Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Git

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/subhiksha1196/smart-parking-system
cd smart-parking-system
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
```
Open your browser and navigate to: http://localhost:8080
```

## Usage

### Customer Operations

1. **Register a Customer**
   - Navigate to Register tab
   - Fill in customer details (name, phone, email, vehicle info)
   - Optionally mark as handicapped for priority parking

2. **Park a Vehicle**
   - Go to Park tab
   - Enter vehicle registration number
   - System automatically allocates available space
   - Receive parking ticket with space details

3. **Make a Reservation**
   - Go to Reserve tab
   - Enter vehicle registration and select date/time
   - Choose preferred floor
   - Receive reservation confirmation

4. **Exit & Payment**
   - Go to Exit tab
   - Enter ticket ID
   - View calculated charges (with loyalty discount if applicable)
   - Select payment method (Cash/Card/UPI)
   - Complete payment and exit

### Admin Operations

1. **View Customers**
   - Access via Admin Panel (hamburger menu)
   - View all registered customers
   - See customer details and loyalty points

2. **Manage Reservations**
   - View all active and past reservations
   - Check reservation status

3. **View Reports**
   - Revenue analytics
   - Parking statistics
   - Occupancy rates

4. **Visual Parking Lot**
   - Interactive 3-floor visualization
   - Real-time space availability
   - Color-coded status indicators

## API Endpoints

### Customer Management
- `GET /register` - Show registration form
- `POST /register` - Register new customer
- `GET /customers` - List all customers

### Parking Operations
- `GET /park` - Show park form
- `POST /park` - Park a vehicle
- `GET /exit` - Show exit form
- `POST /exit/payment` - Process payment
- `POST /exit/complete` - Complete exit

### Reservations
- `GET /reserve` - Show reservation form
- `POST /reserve` - Create reservation
- `GET /reservations` - List all reservations

### Analytics
- `GET /report` - View reports dashboard
- `GET /visualize` - View visual parking lot
- `GET /tickets` - View all tickets

## Contributors

This project was developed by:

- **Subhiksha** - [GitHub](https://github.com/subhiksha1196)
- **Sreya** - [GitHub](https://github.com/sreya889)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Contact

For any queries or suggestions, please reach out to:
- Email: subhiksha1196@gmail.com
- Project Link: [GitHub](https://github.com/subhiksha1196/smart-parking-system)

---

⭐ Star this repo if you find it helpful!