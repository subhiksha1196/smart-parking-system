package com.parking.service;

import com.parking.model.*;
import com.parking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parking Service - Core business logic for parking management
 * Handles parking operations, reservations, payments, and reporting
 * Implements both database persistence and file-based backup
 * 
 * @author Your Name
 * @version 1.0
 */
@Service
public class ParkingService {

    // ===== DATABASE REPOSITORIES =====
    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private ParkingSpaceRepository spaceRepo;

    @Autowired
    private ParkingTicketRepository ticketRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ReservationRepository reservationRepo;

    // ===== FILE STORAGE SERVICE =====
    private FileStorageService fileStorage;

    // ===== COUNTERS AND CONFIGURATION =====
    private int ticketCounter = 1;
    private int reservationCounter = 1;
    private final boolean weekendPricingEnabled = true;
    private final double weekendMultiplier = 1.2;

    // ============================================================
    // INITIALIZATION
    // ============================================================
    
    /**
     * Initializes parking system on startup
     * Creates parking spaces if database is empty
     * Initializes file storage service
     */
    @PostConstruct
    public void initialize() {
        // Initialize file storage
        fileStorage = new FileStorageService();
        System.out.println("✅ File storage service initialized");
        
        // Check if parking spaces exist in DATABASE
        long spaceCount = spaceRepo.count();
        
        if (spaceCount == 0) {
            System.out.println("🚀 Initializing Smart Parking System...");
            
            // Try to load from backup files
            try {
                List<ParkingSpace> loadedSpaces = fileStorage.loadParkingSpaces();
                
                // CHECK IF FILES HAD DATA
                if (!loadedSpaces.isEmpty()) {
                    System.out.println("📂 Loading " + loadedSpaces.size() + " spaces from backup...");
                    loadedSpaces.forEach(spaceRepo::save);
                    
                    // Also load customers
                    List<Customer> loadedCustomers = fileStorage.loadCustomers();
                    if (!loadedCustomers.isEmpty()) {
                        loadedCustomers.forEach(customerRepo::save);
                        System.out.println("📂 Loaded " + loadedCustomers.size() + " customers from backup");
                    }
                    
                    System.out.println("✅ Successfully loaded data from backup files!");
                } else {
                    // FILES WERE EMPTY - CREATE NEW SPACES
                    System.out.println("ℹ️ Backup files empty, creating fresh parking spaces...");
                    createDefaultParkingSpaces();
                }
                
            } catch (Exception e) {
                System.out.println("ℹ️ No backup files found, creating new parking spaces...");
                createDefaultParkingSpaces();
            }
            
            // Save the initial state
            saveAllDataToFiles();
            
        } else {
            System.out.println("✅ Database contains " + spaceCount + " parking spaces");
        }
    }
    
    /**
     * Creates default parking space layout
     * 3 floors with cars, motorcycles, trucks, and handicapped spaces
     */
    private void createDefaultParkingSpaces() {
        int totalCreated = 0;
        
        for (int floor = 1; floor <= 3; floor++) {
            // Regular car spaces (8 per floor)
            for (int i = 1; i <= 8; i++) {
                String spaceId = "F" + floor + "-A-" + i;
                spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR, false));
                totalCreated++;
            }

            // Handicapped car spaces (2 per floor)
            for (int i = 9; i <= 10; i++) {
                String spaceId = "F" + floor + "-A-" + i;
                spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR, true));
                totalCreated++;
            }

            // Motorcycles (15 per floor)
            for (int i = 1; i <= 15; i++) {
                String spaceId = "F" + floor + "-B-" + i;
                spaceRepo.save(new ParkingSpace(spaceId, floor, "B", VehicleType.MOTORCYCLE, false));
                totalCreated++;
            }

            // Trucks (5 per floor)
            for (int i = 1; i <= 5; i++) {
                String spaceId = "F" + floor + "-C-" + i;
                spaceRepo.save(new ParkingSpace(spaceId, floor, "C", VehicleType.TRUCK, false));
                totalCreated++;
            }
        }
        
        System.out.println("✅ Created " + totalCreated + " parking spaces successfully!");
        System.out.println("📊 Breakdown: 30 Cars + 6 Handicapped + 45 Motorcycles + 15 Trucks");
    }

    // ============================================================
    // FILE STORAGE OPERATIONS
    // ============================================================
    
    /**
     * Saves all data to backup files
     * Called automatically after major operations
     */
    public void saveAllDataToFiles() {
        try {
            fileStorage.saveCustomers(customerRepo.findAll());
            fileStorage.saveTickets(ticketRepo.findAll());
            fileStorage.saveReservations(reservationRepo.findAll());
            fileStorage.saveParkingSpaces(spaceRepo.findAll());
            System.out.println("💾 Data backed up to files successfully");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not backup data to files: " + e.getMessage());
        }
    }

    // ============================================================
    // CUSTOMER MANAGEMENT
    // ============================================================
    
    /**
     * Registers a new customer
     * Automatically backs up to file after registration
     * 
     * @param name Customer name
     * @param contact Customer contact number
     * @param email Customer email
     * @param handicapped Whether customer needs handicapped parking
     * @return Registered customer object
     */
    public Customer registerCustomer(String name, String contact, String email, boolean handicapped) {
        Customer customer = new Customer(name, contact, email, handicapped);
        customer = customerRepo.save(customer);
        
        // Backup to file
        try {
            fileStorage.saveCustomers(customerRepo.findAll());
            System.out.println("💾 Customer backed up to file");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not backup customer");
        }
        
        return customer;
    }

    /**
     * Finds customer by contact number
     * @param contact Contact number
     * @return Optional containing customer if found
     */
    public Optional<Customer> findCustomerByContact(String contact) {
        return customerRepo.findByContact(contact);
    }

    /**
     * Retrieves all registered customers
     * @return List of all customers
     */
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    // ============================================================
    // RESERVATION SYSTEM
    // ============================================================
    
    /**
     * Creates a parking reservation for a customer
     * Automatically backs up to file
     * 
     * @param customerId Customer ID
     * @param type Vehicle type to reserve for
     * @param validityHours How many hours reservation is valid
     * @return Created reservation, or null if no space available
     */
    public Reservation createReservation(Long customerId, VehicleType type, int validityHours) {
        Optional<Customer> optCustomer = customerRepo.findById(customerId);
        if (optCustomer.isEmpty()) return null;

        Customer customer = optCustomer.get();
        List<ParkingSpace> availableSpaces = findOptimalSpace(customer, type);

        if (availableSpaces.isEmpty()) return null;

        ParkingSpace space = availableSpaces.get(0);
        space.reserve();
        spaceRepo.save(space);

        String resId = "R" + String.format("%03d", reservationCounter++);
        Reservation reservation = new Reservation(resId, customer, space, type, validityHours);
        reservation = reservationRepo.save(reservation);
        
        // Backup to files
        try {
            fileStorage.saveReservations(reservationRepo.findAll());
            fileStorage.saveParkingSpaces(spaceRepo.findAll());
            System.out.println("💾 Reservation backed up to file");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not backup reservation");
        }
        
        return reservation;
    }

    /**
     * Gets all active (not used, not expired) reservations
     * @return List of active reservations
     */
    public List<Reservation> getActiveReservations() {
        return reservationRepo.findByUsedFalse().stream()
                .filter(Reservation::isValid)
                .collect(Collectors.toList());
    }

    /**
     * Expires old reservations and releases their spaces
     */
    public void expireOldReservations() {
        List<Reservation> allReservations = reservationRepo.findByUsedFalse();
        boolean hasChanges = false;
        
        for (Reservation res : allReservations) {
            if (res.isExpired()) {
                ParkingSpace space = res.getReservedSpace();
                space.releaseSpace();
                spaceRepo.save(space);
                res.setUsed(true);
                reservationRepo.save(res);
                hasChanges = true;
            }
        }
        
        // Backup if any reservations expired
        if (hasChanges) {
            try {
                fileStorage.saveReservations(reservationRepo.findAll());
                fileStorage.saveParkingSpaces(spaceRepo.findAll());
            } catch (IOException e) {
                System.err.println("⚠️ Warning: Could not backup after expiring reservations");
            }
        }
    }

    // ============================================================
    // PARKING OPERATIONS
    // ============================================================
    
    /**
     * Parks a vehicle with optional customer and reservation
     * Automatically backs up ticket and space status to files
     * 
     * @param licensePlate Vehicle license plate
     * @param type Vehicle type
     * @param contact Contact number
     * @param customerId Optional customer ID
     * @param reservationId Optional reservation ID
     * @return Generated parking ticket, or null if no space available
     */
    public ParkingTicket parkVehicleWithCustomer(String licensePlate, VehicleType type,
                                                 String contact, Long customerId, String reservationId) {
        expireOldReservations();

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepo.findById(customerId).orElse(null);
        }

        Vehicle vehicle = new Vehicle(licensePlate, type, contact);
        vehicleRepo.save(vehicle);

        ParkingSpace space = null;

        // Check for reservation first
        if (reservationId != null && !reservationId.isEmpty()) {
            Optional<Reservation> optRes = reservationRepo.findById(reservationId);
            if (optRes.isPresent() && optRes.get().isValid()) {
                Reservation res = optRes.get();
                space = res.getReservedSpace();
                res.setUsed(true);
                reservationRepo.save(res);
            }
        }

        // Find optimal space if no reservation
        if (space == null) {
            List<ParkingSpace> availableSpaces = findOptimalSpace(customer, type);
            if (availableSpaces.isEmpty()) return null;
            space = availableSpaces.get(0);
        }

        space.occupySpace(vehicle);
        spaceRepo.save(space);

        String ticketId = "T" + String.format("%03d", ticketCounter++);
        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, space, customer);
        ticket = ticketRepo.save(ticket);
        
        // Backup to files
        try {
            fileStorage.saveTickets(ticketRepo.findAll());
            fileStorage.saveParkingSpaces(spaceRepo.findAll());
            System.out.println("💾 Parking ticket backed up to file");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not backup parking ticket");
        }

        return ticket;
    }

    /**
     * Gets all active (not paid) parking tickets
     * @return List of active tickets
     */
    public List<ParkingTicket> getActiveTickets() {
        return ticketRepo.findByStatus(TicketStatus.ACTIVE);
    }

    // ============================================================
    // EXIT AND PAYMENT
    // ============================================================
    
    /**
     * Calculates fee preview for vehicle exit
     * Does not modify any data
     * 
     * @param ticketId Parking ticket ID
     * @return Map containing fee details and success status
     */
    public Map<String, Object> calculateExitPreview(String ticketId) {
        Map<String, Object> preview = new HashMap<>();
        
        Optional<ParkingTicket> optTicket = ticketRepo.findById(ticketId);
        if (optTicket.isEmpty()) {
            preview.put("success", false);
            preview.put("message", "Ticket not found");
            return preview;
        }

        ParkingTicket ticket = optTicket.get();
        long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
        if (hours == 0) hours = 1;

        double rate = getHourlyRate(ticket.getVehicle().getType());
        double amount = hours * rate;

        boolean isWeekend = isWeekend(LocalDateTime.now());
        if (weekendPricingEnabled && isWeekend) {
            amount *= weekendMultiplier;
        }

        boolean hasDiscount = ticket.getCustomer() != null && ticket.getCustomer().hasLoyaltyDiscount();
        if (hasDiscount) {
            amount *= 0.9;
        }

        preview.put("success", true);
        preview.put("amount", amount);
        preview.put("hours", hours);
        preview.put("isWeekend", isWeekend);
        preview.put("hasDiscount", hasDiscount);

        return preview;
    }

    /**
     * Processes vehicle exit with payment
     * Updates ticket, releases space, awards loyalty points
     * Automatically backs up all changes to files
     * 
     * @param ticketId Parking ticket ID
     * @param paymentMethod Payment method (CASH/CARD/UPI)
     * @param paymentDetails Payment details (card number, UPI ID, etc.)
     * @param cashReceived Amount of cash received (for cash payments)
     * @return Map containing payment result and details
     */
    public Map<String, Object> exitVehicleWithPayment(String ticketId, PaymentMethod paymentMethod,
                                                      String paymentDetails, double cashReceived) {
        Map<String, Object> result = new HashMap<>();

        Optional<ParkingTicket> optTicket = ticketRepo.findById(ticketId);
        if (optTicket.isEmpty()) {
            result.put("success", false);
            result.put("message", "Ticket not found");
            return result;
        }

        ParkingTicket ticket = optTicket.get();
        ticket.setExitTime(LocalDateTime.now());

        long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
        if (hours == 0) hours = 1;

        double rate = getHourlyRate(ticket.getVehicle().getType());
        double amount = hours * rate;

        // Weekend pricing
        if (weekendPricingEnabled && isWeekend(ticket.getExitTime())) {
            amount *= weekendMultiplier;
        }

        // Loyalty discount
        boolean discountApplied = false;
        if (ticket.getCustomer() != null && ticket.getCustomer().hasLoyaltyDiscount()) {
            amount *= 0.9;
            discountApplied = true;
            ticket.setLoyaltyDiscountApplied(true);
        }

        ticket.setAmount(amount);
        ticket.setPaymentMethod(paymentMethod);
        ticket.setPaymentDetails(paymentDetails);

        // Process cash payment
        if (paymentMethod == PaymentMethod.CASH) {
            if (cashReceived < amount) {
                result.put("success", false);
                result.put("message", "Insufficient cash. Required: ₹" + amount);
                return result;
            }
            ticket.setCashReceived(cashReceived);
            ticket.setChangeReturned(cashReceived - amount);
            result.put("change", cashReceived - amount);
        }

        // Award loyalty points
        if (ticket.getCustomer() != null) {
            int pointsEarned = (int) (amount / 10);
            ticket.getCustomer().addLoyaltyPoints(pointsEarned);
            ticket.setLoyaltyPointsEarned(pointsEarned);
            customerRepo.save(ticket.getCustomer());
        }

        ticket.setStatus(TicketStatus.PAID);

        // Release space
        ParkingSpace space = ticket.getAllocatedSpace();
        space.releaseSpace();
        spaceRepo.save(space);

        ticketRepo.save(ticket);
        
        // Backup to files
        try {
            fileStorage.saveTickets(ticketRepo.findAll());
            fileStorage.saveCustomers(customerRepo.findAll());
            fileStorage.saveParkingSpaces(spaceRepo.findAll());
            System.out.println("💾 Exit and payment backed up to file");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not backup exit data");
        }

        result.put("success", true);
        result.put("ticket", ticket);
        result.put("amount", amount);
        result.put("discountApplied", discountApplied);

        return result;
    }

    // ============================================================
    // REPORTS AND ANALYTICS
    // ============================================================
    
    /**
     * Generates revenue report with payment breakdown
     * @return Map containing revenue statistics
     */
    public Map<String, Object> getRevenueReport() {
        List<ParkingTicket> paidTickets = ticketRepo.findByStatus(TicketStatus.PAID);

        double totalRevenue = paidTickets.stream()
                .mapToDouble(ParkingTicket::getAmount)
                .sum();

        Map<PaymentMethod, Double> paymentBreakdown = new HashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            double methodTotal = paidTickets.stream()
                    .filter(t -> t.getPaymentMethod() == method)
                    .mapToDouble(ParkingTicket::getAmount)
                    .sum();
            paymentBreakdown.put(method, methodTotal);
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("totalTickets", paidTickets.size());
        report.put("paymentBreakdown", paymentBreakdown);
        report.put("averageAmount", totalRevenue / Math.max(1, paidTickets.size()));

        return report;
    }

    /**
     * Gets parking space availability by vehicle type
     * @return Map of vehicle types to availability statistics
     */
    public Map<VehicleType, Map<String, Integer>> getAvailability() {
        Map<VehicleType, Map<String, Integer>> availability = new HashMap<>();

        for (VehicleType type : VehicleType.values()) {
            Map<String, Integer> stats = new HashMap<>();

            List<ParkingSpace> allSpaces = spaceRepo.findByCompatibleVehicleType(type);
            List<ParkingSpace> availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
                    SpaceStatus.AVAILABLE, type);
            List<ParkingSpace> occupiedSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
                    SpaceStatus.OCCUPIED, type);

            stats.put("total", allSpaces.size());
            stats.put("available", availableSpaces.size());
            stats.put("occupied", occupiedSpaces.size());

            availability.put(type, stats);
        }

        return availability;
    }

    /**
     * Gets parking spaces grouped by floor
     * @return Map of floor numbers to lists of parking spaces
     */
    public Map<Integer, List<ParkingSpace>> getSpacesByFloor() {
        List<ParkingSpace> allSpaces = spaceRepo.findAll();
        Map<Integer, List<ParkingSpace>> floorMap = new HashMap<>();

        for (ParkingSpace space : allSpaces) {
            floorMap.computeIfAbsent(space.getFloorId(), k -> new ArrayList<>()).add(space);
        }

        return floorMap;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Finds optimal parking space for a customer and vehicle type
     * Prioritizes handicapped spaces for handicapped customers
     * 
     * @param customer Customer (can be null for guests)
     * @param type Vehicle type
     * @return List of available spaces (empty if none available)
     */
    private List<ParkingSpace> findOptimalSpace(Customer customer, VehicleType type) {
        List<ParkingSpace> availableSpaces;

        if (customer != null && customer.isHandicapped()) {
            availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleTypeAndHandicappedSpace(
                    SpaceStatus.AVAILABLE, type, true);
            
            if (availableSpaces.isEmpty()) {
                availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
                        SpaceStatus.AVAILABLE, type);
            }
        } else {
            availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
                    SpaceStatus.AVAILABLE, type);
        }

        return availableSpaces;
    }

    /**
     * Gets hourly parking rate for vehicle type
     * @param type Vehicle type
     * @return Hourly rate in rupees
     */
    private double getHourlyRate(VehicleType type) {
        return switch (type) {
            case CAR -> 20.0;
            case MOTORCYCLE -> 10.0;
            case TRUCK -> 30.0;
        };
    }

    /**
     * Checks if given date/time falls on weekend
     * @param dateTime Date and time to check
     * @return true if Saturday or Sunday
     */
    private boolean isWeekend(LocalDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }
}

// package com.parking.service;

// import com.parking.model.*;
// import com.parking.repository.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import jakarta.annotation.PostConstruct; // Required for initialization logic
// import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;
// import java.util.*;

// /**
//  * Service layer implementing all core parking business logic, including
//  * space initialization, customer management, reservations, dynamic pricing, 
//  * and ticket processing.
//  */
// @Service
// public class ParkingService {

//     @Autowired
//     private VehicleRepository vehicleRepo;

//     @Autowired
//     private ParkingSpaceRepository spaceRepo;

//     @Autowired
//     private ParkingTicketRepository ticketRepo;

//     // Additional Repositories required for the enhanced features
//     @Autowired
//     private CustomerRepository customerRepo;

//     @Autowired
//     private ReservationRepository reservationRepo; 

//     private int ticketCounter = 1;
//     private int reservationCounter = 1; 
//     private final boolean weekendPricingEnabled = true;
//     private final double weekendMultiplier = 1.2;

//     /**
//      * Initializes a standard set of parking spaces with handicapped slots.
//      */
//     @PostConstruct
//     public void initialize() {
//         if (spaceRepo.count() == 0) {
//             System.out.println("Initializing enhanced parking spaces...");
//             for (int floor = 1; floor <= 3; floor++) {
//                 // Regular car spaces (10 total - 2 handicapped)
//                 for (int i = 1; i <= 8; i++) {
//                     String spaceId = "F" + floor + "-A-" + i;
//                     // Assuming a constructor ParkingSpace(id, floor, zone, type)
//                     spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR));
//                 }

//                 // Handicapped car spaces (2 per floor)
//                 for (int i = 9; i <= 10; i++) {
//                     String spaceId = "F" + floor + "-A-" + i;
//                     // Assuming a constructor for ParkingSpace(id, floor, zone, type, isHandicapped)
//                     spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR, true));
//                 }

//                 // Motorcycles
//                 for (int i = 1; i <= 15; i++) {
//                     String spaceId = "F" + floor + "-B-" + i;
//                     spaceRepo.save(new ParkingSpace(spaceId, floor, "B", VehicleType.MOTORCYCLE));
//                 }

//                 // Trucks
//                 for (int i = 1; i <= 5; i++) {
//                     String spaceId = "F" + floor + "-C-" + i;
//                     spaceRepo.save(new ParkingSpace(spaceId, floor, "C", VehicleType.TRUCK));
//                 }
//             }
//             System.out.println("✓ Parking spaces initialized with full support!");
//         }
//     }
    
//     // --- CUSTOMER MANAGEMENT ---
    
//     /**
//      * Registers a new customer into the system.
//      */
//     public Customer registerCustomer(String name, String contact, String email, boolean handicapped) {
//         // Assuming Customer has a constructor Customer(name, contact, email, handicapped)
//         Customer customer = new Customer(name, contact, email, handicapped);
//         return customerRepo.save(customer);
//     }

//     // --- RESERVATION SYSTEM ---

//     /**
//      * Creates a reservation for a specific customer and vehicle type.
//      * Prioritizes handicapped spaces if the customer is marked as such.
//      */
//     public Reservation createReservation(Long customerId, VehicleType type, int validityHours) {
//         Optional<Customer> optCustomer = customerRepo.findById(customerId);
//         if (optCustomer.isEmpty()) return null;

//         Customer customer = optCustomer.get();
//         List<ParkingSpace> availableSpaces = findOptimalAvailableSpace(customer, type);

//         if (availableSpaces.isEmpty()) return null;

//         ParkingSpace space = availableSpaces.get(0);
//         // Assuming ParkingSpace has a reserve() method that changes status to RESERVED
//         space.reserve();
//         spaceRepo.save(space);

//         String resId = "R" + String.format("%03d", reservationCounter++);
//         // Assuming a Reservation constructor: Reservation(id, customer, space, type, validityHours)
//         Reservation reservation = new Reservation(resId, customer, space, type, validityHours);
//         return reservationRepo.save(reservation);
//     }

//     /**
//      * Cleans up reservations that have expired and frees up the corresponding spaces.
//      */
//     public void expireOldReservations() {
//         // Assuming findByUsedFalse exists in ReservationRepository
//         List<Reservation> allReservations = reservationRepo.findByUsedFalse();
//         for (Reservation res : allReservations) {
//             // Assuming Reservation has an isExpired() method (checks time)
//             if (res.isExpired()) {
//                 ParkingSpace space = res.getReservedSpace();
//                 // Assuming ParkingSpace has a releaseSpace() method 
//                 space.releaseSpace();
//                 spaceRepo.save(space);
//                 res.setUsed(true); // Mark as used/invalidated
//                 reservationRepo.save(res);
//             }
//         }
//     }
    
//     // --- ENHANCED PARKING TICKET GENERATION FLOW ---

//     /**
//      * Parks a vehicle, prioritizing reservations or handicapped spaces.
//      */
//     public ParkingTicket parkVehicleWithCustomer(String licensePlate, VehicleType type,
//                                                  String contact, Long customerId,
//                                                  String reservationId) {
//         expireOldReservations();

//         Customer customer = null;
//         if (customerId != null) {
//             customer = customerRepo.findById(customerId).orElse(null);
//         }

//         // Save/update vehicle
//         Vehicle vehicle = new Vehicle(licensePlate, type, contact);
//         vehicleRepo.save(vehicle);

//         ParkingSpace space = null;

//         // 1. Check for valid reservation
//         if (reservationId != null && !reservationId.isEmpty()) {
//             Optional<Reservation> optRes = reservationRepo.findById(reservationId);
//             if (optRes.isPresent() && optRes.get().isValid()) {
//                 Reservation res = optRes.get();
//                 space = res.getReservedSpace();
//                 res.setUsed(true); 
//                 reservationRepo.save(res);
//             }
//         }

//         // 2. Find optimal available space if no reservation was used
//         if (space == null) {
//             List<ParkingSpace> availableSpaces = findOptimalAvailableSpace(customer, type);
//             if (availableSpaces.isEmpty()) return null;
//             space = availableSpaces.get(0);
//         }

//         // 3. Occupy the space
//         // Assuming ParkingSpace has an occupySpace(Vehicle) method that sets status to OCCUPIED
//         space.occupySpace(vehicle);
//         spaceRepo.save(space);

//         // 4. Create and save the ticket
//         String ticketId = "T" + String.format("%03d", ticketCounter++);
//         // Assuming a new constructor: ParkingTicket(id, vehicle, space, customer)
//         ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, space, customer);
//         ticketRepo.save(ticket);

//         return ticket;
//     }

//     // --- ENHANCED EXIT/PAYMENT FLOW ---
    
//     /**
//      * Finalizes the exit, calculates fees with discounts, processes payment, and frees space.
//      * @return A map containing success status, amount due, and change returned (if applicable).
//      */
//     public Map<String, Object> exitVehicleWithPayment(String ticketId, PaymentMethod paymentMethod,
//                                                      String paymentDetails, double cashReceived) {
//         Map<String, Object> result = new HashMap<>();

//         Optional<ParkingTicket> optTicket = ticketRepo.findById(ticketId);
//         if (optTicket.isEmpty()) {
//             result.put("success", false);
//             result.put("message", "Ticket not found");
//             return result;
//         }

//         ParkingTicket ticket = optTicket.get();
//         ticket.setExitTime(LocalDateTime.now());

//         // Calculate duration and base amount
//         long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
//         // Minimum charge of 1 hour
//         if (hours == 0) hours = 1; 

//         double rate = getHourlyRate(ticket.getVehicle().getType());
//         double amount = hours * rate;

//         // 1. Apply Weekend Pricing
//         if (weekendPricingEnabled && isWeekend(ticket.getExitTime())) {
//             amount *= weekendMultiplier;
//         }

//         // 2. Apply Loyalty Discount
//         boolean discountApplied = false;
//         if (ticket.getCustomer() != null && ticket.getCustomer().hasLoyaltyDiscount()) {
//             amount *= 0.9; // 10% discount
//             discountApplied = true;
//             ticket.setLoyaltyDiscountApplied(true);
//         }

//         ticket.setAmount(amount);

//         // 3. Process Payment
//         double changeReturned = 0.0;
//         if (paymentMethod == PaymentMethod.CASH) {
//             if (cashReceived < amount) {
//                 result.put("success", false);
//                 result.put("message", "Insufficient cash");
//                 return result;
//             }
//             changeReturned = cashReceived - amount;
//             ticket.setCashReceived(cashReceived);
//             ticket.setChangeReturned(changeReturned);
//             result.put("change", changeReturned);
//         } else if (paymentMethod == PaymentMethod.CARD) {
//             // Logic for card processing goes here (e.g., calling an external payment gateway)
//         }
        
//         // 4. Update Loyalty Points
//         if (ticket.getCustomer() != null) {
//             int pointsEarned = (int) (amount / 10);
//             ticket.getCustomer().addLoyaltyPoints(pointsEarned);
//             ticket.setLoyaltyPointsEarned(pointsEarned);
//             customerRepo.save(ticket.getCustomer());
//         }

//         // 5. Finalize Ticket and Space
//         ticket.setStatus(TicketStatus.PAID); // Assuming TicketStatus enum exists
//         ParkingSpace space = ticket.getAllocatedSpace();
//         space.releaseSpace(); // Assuming ParkingSpace has a releaseSpace() method (sets status to AVAILABLE)
        
//         spaceRepo.save(space);
//         ticketRepo.save(ticket);

//         result.put("success", true);
//         result.put("ticket", ticket);
//         result.put("amount", amount);
//         result.put("discountApplied", discountApplied);

//         return result;
//     }
    
//     // --- HELPER METHODS ---

//     private double getHourlyRate(VehicleType type) {
//         return switch (type) {
//             case CAR -> 20.0;
//             case MOTORCYCLE -> 10.0;
//             case TRUCK -> 30.0;
//             // Add other types as needed
//         };
//     }
    
//     private boolean isWeekend(LocalDateTime dateTime) {
//         int dayOfWeek = dateTime.getDayOfWeek().getValue();
//         return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
//     }

//     /**
//      * Finds the best available parking space for a customer, prioritizing handicapped if necessary.
//      */
//     private List<ParkingSpace> findOptimalAvailableSpace(Customer customer, VehicleType type) {
//         List<ParkingSpace> availableSpaces = Collections.emptyList();
        
//         // 1. Priority for handicapped customers
//         if (customer != null && customer.isHandicapped()) {
//              // Assuming findByStatusAndCompatibleVehicleTypeAndHandicappedSpace exists
//             availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleTypeAndHandicappedSpace(
//                 SpaceStatus.AVAILABLE, type, true
//             );
//             if (!availableSpaces.isEmpty()) return availableSpaces;
//         }

//         // 2. Fallback to any regular available space
//         // Assuming findByStatusAndCompatibleVehicleType exists
//         availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
//             SpaceStatus.AVAILABLE, type
//         );
        
//         return availableSpaces;
//     }
    
//     // --- REPORTING/AVAILABILITY ---
    
//     /**
//      * Provides a detailed availability breakdown by vehicle type (Total, Available, Occupied).
//      */
//     public Map<VehicleType, Map<String, Integer>> getAvailability() {
//         Map<VehicleType, Map<String, Integer>> availability = new HashMap<>();
        
//         for (VehicleType type : VehicleType.values()) {
//             Map<String, Integer> stats = new HashMap<>();
            
//             // Get all spaces for this vehicle type
//             List<ParkingSpace> allSpaces = spaceRepo.findByCompatibleVehicleType(type);
            
//             // Get available spaces
//             List<ParkingSpace> availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
//                 SpaceStatus.AVAILABLE, type
//             );
            
//             // Get occupied spaces
//             List<ParkingSpace> occupiedSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
//                 SpaceStatus.OCCUPIED, type
//             );
            
//             stats.put("total", allSpaces.size());
//             stats.put("available", availableSpaces.size());
//             stats.put("occupied", occupiedSpaces.size());
            
//             availability.put(type, stats);
//         }
        
//         return availability;
//     }

//     public List<ParkingTicket> getActiveTickets() {
//         // Assuming findByStatus exists in ParkingTicketRepository
//         return ticketRepo.findByStatus(TicketStatus.ACTIVE);
//     }
// }


// // package com.parking.service;

// // import com.parking.model.*;
// // import com.parking.repository.*;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.stereotype.Service;
// // import jakarta.annotation.PostConstruct;
// // import java.time.LocalDateTime;
// // import java.time.temporal.ChronoUnit;
// // import java.util.*;

// // @Service
// // public class ParkingService {

// //     @Autowired
// //     private VehicleRepository vehicleRepo;

// //     @Autowired
// //     private ParkingSpaceRepository spaceRepo;

// //     @Autowired
// //     private ParkingTicketRepository ticketRepo;

// //     // --- NEW AUTOWIRED REPOSITORIES ---
// //     @Autowired
// //     private CustomerRepository customerRepo;

// //     @Autowired
// //     private ReservationRepository reservationRepo;
// //     // ------------------------------------

// //     private int ticketCounter = 1;
// //     private int reservationCounter = 1; // NEW FIELD
// //     private boolean weekendPricingEnabled = true; // NEW FIELD
// //     private double weekendMultiplier = 1.2; // NEW FIELD

// //     // --- UPDATED POST CONSTRUCT METHOD ---
// //     @PostConstruct
// //     public void initialize() {
// //         if (spaceRepo.count() == 0) {
// //             System.out.println("Initializing parking spaces...");
// //             for (int floor = 1; floor <= 3; floor++) {
// //                 // Regular car spaces (10 total - 2 handicapped)
// //                 for (int i = 1; i <= 8; i++) {
// //                     String spaceId = "F" + floor + "-A-" + i;
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR));
// //                 }

// //                 // Handicapped car spaces (2 per floor)
// //                 for (int i = 9; i <= 10; i++) {
// //                     String spaceId = "F" + floor + "-A-" + i;
// //                     // Assuming a constructor for ParkingSpace(id, floor, zone, type, isHandicapped) exists
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR, true));
// //                 }

// //                 // Motorcycles
// //                 for (int i = 1; i <= 15; i++) {
// //                     String spaceId = "F" + floor + "-B-" + i;
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "B", VehicleType.MOTORCYCLE));
// //                 }

// //                 // Trucks
// //                 for (int i = 1; i <= 5; i++) {
// //                     String spaceId = "F" + floor + "-C-" + i;
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "C", VehicleType.TRUCK));
// //                 }
// //             }
// //             System.out.println("✓ Parking spaces initialized with handicapped support!");
// //         }
// //     }
// //     // ------------------------------------

// //     // --- CUSTOMER MANAGEMENT ---
// //     public Customer registerCustomer(String name, String contact, String email, boolean handicapped) {
// //         Customer customer = new Customer(name, contact, email, handicapped);
// //         return customerRepo.save(customer);
// //     }

// //     public Optional<Customer> findCustomerByContact(String contact) {
// //         return customerRepo.findByContact(contact);
// //     }

// //     public List<Customer> getAllCustomers() {
// //         return customerRepo.findAll();
// //     }
// //     // ------------------------------------

// //     // --- RESERVATION SYSTEM ---
// //     public Reservation createReservation(Long customerId, VehicleType type, int validityHours) {
// //         Optional<Customer> optCustomer = customerRepo.findById(customerId);
// //         if (optCustomer.isEmpty()) return null;

// //         Customer customer = optCustomer.get();

// //         // Find available space (prioritize handicapped if customer is handicapped)
// //         List<ParkingSpace> availableSpaces;
// //         if (customer.isHandicapped()) {
// //             // Assuming findByStatusAndCompatibleVehicleTypeAndHandicappedSpace exists in ParkingSpaceRepository
// //             availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleTypeAndHandicappedSpace(
// //                 SpaceStatus.AVAILABLE, type, true
// //             );
// //             if (availableSpaces.isEmpty()) {
// //                 availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                     SpaceStatus.AVAILABLE, type
// //                 );
// //             }
// //         } else {
// //             availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                 SpaceStatus.AVAILABLE, type
// //             );
// //         }

// //         if (availableSpaces.isEmpty()) return null;

// //         ParkingSpace space = availableSpaces.get(0);
// //         // Assuming ParkingSpace has a reserve() method that changes status to RESERVED
// //         space.reserve();
// //         spaceRepo.save(space);

// //         String resId = "R" + String.format("%03d", reservationCounter++);
// //         // Assuming a Reservation constructor: new Reservation(id, customer, space, type, validityHours)
// //         Reservation reservation = new Reservation(resId, customer, space, type, validityHours);
// //         return reservationRepo.save(reservation);
// //     }

// //     public List<Reservation> getActiveReservations() {
// //         // Assuming findByUsedFalse exists in ReservationRepository
// //         return reservationRepo.findByUsedFalse().stream()
// //             // Assuming Reservation has an isValid() method (checks against current time)
// //             .filter(Reservation::isValid)
// //             .toList();
// //     }

// //     public void expireOldReservations() {
// //         // Assuming findByUsedFalse exists in ReservationRepository
// //         List<Reservation> allReservations = reservationRepo.findByUsedFalse();
// //         for (Reservation res : allReservations) {
// //             // Assuming Reservation has an isExpired() method
// //             if (res.isExpired()) {
// //                 ParkingSpace space = res.getReservedSpace();
// //                 // Assuming ParkingSpace has a releaseSpace() method that changes status to AVAILABLE
// //                 space.releaseSpace();
// //                 spaceRepo.save(space);
// //                 res.setUsed(true); // Mark as used/invalidated
// //                 reservationRepo.save(res);
// //             }
// //         }
// //     }
// //     // ------------------------------------

// //     // --- ENHANCED PARK VEHICLE ---
// //     // Replaces the simple parkVehicle method
// //     public ParkingTicket parkVehicleWithCustomer(String licensePlate, VehicleType type,
// //                                                  String contact, Long customerId,
// //                                                  String reservationId) {
// //         // Clean up expired reservations first
// //         expireOldReservations();

// //         Customer customer = null;
// //         if (customerId != null) {
// //             customer = customerRepo.findById(customerId).orElse(null);
// //         }

// //         Vehicle vehicle = new Vehicle(licensePlate, type, contact);
// //         vehicleRepo.save(vehicle);

// //         ParkingSpace space = null;

// //         // 1. Check if using a valid reservation
// //         if (reservationId != null && !reservationId.isEmpty()) {
// //             Optional<Reservation> optRes = reservationRepo.findById(reservationId);
// //             if (optRes.isPresent() && optRes.get().isValid()) {
// //                 Reservation res = optRes.get();
// //                 space = res.getReservedSpace();
// //                 res.setUsed(true); // Mark reservation as used
// //                 reservationRepo.save(res);
// //             }
// //         }

// //         // 2. If no reservation, find an available space
// //         if (space == null) {
// //             List<ParkingSpace> availableSpaces;

// //             if (customer != null && customer.isHandicapped()) {
// //                 // Priority for handicapped customers
// //                 availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleTypeAndHandicappedSpace(
// //                     SpaceStatus.AVAILABLE, type, true
// //                 );
// //                 if (availableSpaces.isEmpty()) {
// //                     // Fallback to regular space
// //                     availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                         SpaceStatus.AVAILABLE, type
// //                     );
// //                 }
// //             } else {
// //                 // Regular space search
// //                 availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                     SpaceStatus.AVAILABLE, type
// //                 );
// //             }

// //             if (availableSpaces.isEmpty()) return null;
// //             space = availableSpaces.get(0);
// //         }

// //         // 3. Occupy the space
// //         // Assuming ParkingSpace has an occupySpace(Vehicle) method
// //         space.occupySpace(vehicle);
// //         spaceRepo.save(space);

// //         // 4. Create and save the ticket
// //         String ticketId = "T" + String.format("%03d", ticketCounter++);
// //         // Uses the new constructor: ParkingTicket(id, vehicle, space, customer)
// //         ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, space, customer);
// //         ticketRepo.save(ticket);

// //         return ticket;
// //     }
// //     // ------------------------------------

// //     // --- ENHANCED EXIT WITH PAYMENT ---
// //     // Replaces the simple exitVehicle method
// //     public Map<String, Object> exitVehicleWithPayment(String ticketId, PaymentMethod paymentMethod,
// //                                                       String paymentDetails, double cashReceived) {
// //         Map<String, Object> result = new HashMap<>();

// //         Optional<ParkingTicket> optTicket = ticketRepo.findById(ticketId);
// //         if (optTicket.isEmpty()) {
// //             result.put("success", false);
// //             result.put("message", "Ticket not found");
// //             return result;
// //         }

// //         ParkingTicket ticket = optTicket.get();
// //         ticket.setExitTime(LocalDateTime.now());

// //         // Calculate parking duration
// //         long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
// //         if (hours == 0) hours = 1;

// //         // Base rate
// //         double rate = switch (ticket.getVehicle().getType()) {
// //             case CAR -> 20.0;
// //             case MOTORCYCLE -> 10.0;
// //             case TRUCK -> 30.0;
// //         };

// //         double amount = hours * rate;

// //         // 1. Weekend pricing
// //         if (weekendPricingEnabled && isWeekend(ticket.getExitTime())) {
// //             amount *= weekendMultiplier;
// //         }

// //         // 2. Loyalty discount
// //         boolean discountApplied = false;
// //         if (ticket.getCustomer() != null && ticket.getCustomer().hasLoyaltyDiscount()) {
// //             amount *= 0.9; // 10% discount
// //             discountApplied = true;
// //             ticket.setLoyaltyDiscountApplied(true);
// //         }

// //         ticket.setAmount(amount);

// //         // 3. Process payment
// //         ticket.setPaymentMethod(paymentMethod);
// //         ticket.setPaymentDetails(paymentDetails);

// //         if (paymentMethod == PaymentMethod.CASH) {
// //             if (cashReceived < amount) {
// //                 result.put("success", false);
// //                 result.put("message", "Insufficient cash");
// //                 return result;
// //             }
// //             ticket.setCashReceived(cashReceived);
// //             ticket.setChangeReturned(cashReceived - amount);
// //             result.put("change", cashReceived - amount);
// //         }

// //         // 4. Award loyalty points
// //         if (ticket.getCustomer() != null) {
// //             int pointsEarned = (int) (amount / 10);
// //             ticket.getCustomer().addLoyaltyPoints(pointsEarned);
// //             ticket.setLoyaltyPointsEarned(pointsEarned);
// //             customerRepo.save(ticket.getCustomer());
// //         }

// //         ticket.setStatus(TicketStatus.PAID);

// //         // 5. Release space
// //         ParkingSpace space = ticket.getAllocatedSpace();
// //         // Assuming ParkingSpace has a releaseSpace() method
// //         space.releaseSpace();
// //         spaceRepo.save(space);

// //         ticketRepo.save(ticket);

// //         result.put("success", true);
// //         result.put("ticket", ticket);
// //         result.put("amount", amount);
// //         result.put("discountApplied", discountApplied);

// //         return result;
// //     }
// //     // ------------------------------------

// //     // --- UTILITY METHOD ---
// //     private boolean isWeekend(LocalDateTime dateTime) {
// //         int dayOfWeek = dateTime.getDayOfWeek().getValue();
// //         return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
// //     }
// //     // ------------------------------------

// //     // --- REPORTING ---
// //     public Map<String, Object> getRevenueReport() {
// //         // Assuming findByStatus exists in ParkingTicketRepository
// //         List<ParkingTicket> paidTickets = ticketRepo.findByStatus(TicketStatus.PAID);

// //         double totalRevenue = paidTickets.stream()
// //             .mapToDouble(ParkingTicket::getAmount)
// //             .sum();

// //         Map<PaymentMethod, Double> paymentBreakdown = new HashMap<>();
// //         for (PaymentMethod method : PaymentMethod.values()) {
// //             double methodTotal = paidTickets.stream()
// //                 .filter(t -> t.getPaymentMethod() == method)
// //                 .mapToDouble(ParkingTicket::getAmount)
// //                 .sum();
// //             paymentBreakdown.put(method, methodTotal);
// //         }

// //         Map<String, Object> report = new HashMap<>();
// //         report.put("totalRevenue", totalRevenue);
// //         report.put("totalTickets", paidTickets.size());
// //         report.put("paymentBreakdown", paymentBreakdown);
// //         report.put("averageAmount", totalRevenue / Math.max(1, paidTickets.size()));

// //         return report;
// //     }
// //     // ------------------------------------

// //     // --- EXISTING HELPER METHODS ---
// //     // Note: The original parkVehicle and exitVehicle were removed/replaced by the enhanced versions.
    
// //     // public Map<VehicleType, Long> getAvailability() {
// //     //     Map<VehicleType, Long> availability = new HashMap<>();
// //     //     for (VehicleType type : VehicleType.values()) {
// //     //         // Assuming findByStatusAndCompatibleVehicleType exists in ParkingSpaceRepository
// //     //         long count = spaceRepo.findByStatusAndCompatibleVehicleType(
// //     //             SpaceStatus.AVAILABLE, type
// //     //         ).size();
// //     //         availability.put(type, count);
// //     //     }
// //     //     return availability;
// //     // }

// //     // public Map<VehicleType, Map<String, Integer>> getAvailability() {
// //     //     Map<VehicleType, Map<String, Integer>> availability = new HashMap<>();
        
// //     //     for (VehicleType type : VehicleType.values()) {
// //     //         Map<String, Integer> stats = new HashMap<>();
            
// //     //         // Get all spaces for this vehicle type
// //     //         List<ParkingSpace> allSpaces = spaceRepo.findByCompatibleVehicleType(type);
            
// //     //         // Get available spaces
// //     //         List<ParkingSpace> availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //     //             SpaceStatus.AVAILABLE, type
// //     //         );
            
// //     //         // Get occupied spaces
// //     //         List<ParkingSpace> occupiedSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //     //             SpaceStatus.OCCUPIED, type
// //     //         );
            
// //     //         stats.put("total", allSpaces.size());
// //     //         stats.put("available", availableSpaces.size());
// //     //         stats.put("occupied", occupiedSpaces.size());
            
// //     //         availability.put(type, stats);
// //     //     }
        
// //     //     return availability;
// //     // }

// //     public Map<VehicleType, Map<String, Integer>> getAvailability() {
// //         Map<VehicleType, Map<String, Integer>> availability = new HashMap<>();
        
// //         for (VehicleType type : VehicleType.values()) {
// //             Map<String, Integer> stats = new HashMap<>();
            
// //             // Get all spaces for this vehicle type
// //             List<ParkingSpace> allSpaces = spaceRepo.findByCompatibleVehicleType(type);
            
// //             // Get available spaces
// //             List<ParkingSpace> availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                 SpaceStatus.AVAILABLE, type
// //             );
            
// //             // Get occupied spaces
// //             List<ParkingSpace> occupiedSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                 SpaceStatus.OCCUPIED, type
// //             );
            
// //             stats.put("total", allSpaces.size());
// //             stats.put("available", availableSpaces.size());
// //             stats.put("occupied", occupiedSpaces.size());
            
// //             availability.put(type, stats);
// //         }
        
// //         return availability;
// //     }
// //     public List<ParkingSpace> getAllSpaces() {
// //         return spaceRepo.findAll();
// //     }

// //     public List<ParkingTicket> getActiveTickets() {
// //         // Assuming findByStatus exists in ParkingTicketRepository
// //         return ticketRepo.findByStatus(TicketStatus.ACTIVE);
// //     }
// // }

// // package com.parking.service;

// // import com.parking.model.*;
// // import com.parking.repository.*;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.stereotype.Service;
// // import jakarta.annotation.PostConstruct;
// // import java.time.LocalDateTime;
// // import java.time.temporal.ChronoUnit;
// // import java.util.*;

// // @Service
// // public class ParkingService {
    
// //     @Autowired
// //     private VehicleRepository vehicleRepo;
    
// //     @Autowired
// //     private ParkingSpaceRepository spaceRepo;
    
// //     @Autowired
// //     private ParkingTicketRepository ticketRepo;
    
// //     private int ticketCounter = 1;
    
// //     @PostConstruct
// //     public void initialize() {
// //         if (spaceRepo.count() == 0) {
// //             System.out.println("Initializing parking spaces...");
// //             for (int floor = 1; floor <= 3; floor++) {
// //                 for (int i = 1; i <= 10; i++) {
// //                     String spaceId = "F" + floor + "-A-" + i;
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "A", VehicleType.CAR));
// //                 }
                
// //                 for (int i = 1; i <= 15; i++) {
// //                     String spaceId = "F" + floor + "-B-" + i;
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "B", VehicleType.MOTORCYCLE));
// //                 }
                
// //                 for (int i = 1; i <= 5; i++) {
// //                     String spaceId = "F" + floor + "-C-" + i;
// //                     spaceRepo.save(new ParkingSpace(spaceId, floor, "C", VehicleType.TRUCK));
// //                 }
// //             }
// //             System.out.println("✓ Parking spaces initialized!");
// //         }
// //     }
    
// //     public ParkingTicket parkVehicle(String licensePlate, VehicleType type, String contact) {
// //         Vehicle vehicle = new Vehicle(licensePlate, type, contact);
// //         vehicleRepo.save(vehicle);
        
// //         List<ParkingSpace> availableSpaces = spaceRepo.findByStatusAndCompatibleVehicleType(
// //             SpaceStatus.AVAILABLE, type
// //         );
        
// //         if (availableSpaces.isEmpty()) {
// //             return null;
// //         }
        
// //         ParkingSpace space = availableSpaces.get(0);
// //         space.occupySpace(vehicle);
// //         spaceRepo.save(space);
        
// //         String ticketId = "T" + String.format("%03d", ticketCounter++);
// //         ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, space);
// //         ticketRepo.save(ticket);
        
// //         return ticket;
// //     }
    
// //     public boolean exitVehicle(String ticketId) {
// //         Optional<ParkingTicket> optTicket = ticketRepo.findById(ticketId);
// //         if (optTicket.isEmpty()) {
// //             return false;
// //         }
        
// //         ParkingTicket ticket = optTicket.get();
// //         ticket.setExitTime(LocalDateTime.now());
        
// //         long hours = ChronoUnit.HOURS.between(ticket.getEntryTime(), LocalDateTime.now());
// //         if (hours == 0) hours = 1;
        
// //         double rate = switch (ticket.getVehicle().getType()) {
// //             case CAR -> 20.0;
// //             case MOTORCYCLE -> 10.0;
// //             case TRUCK -> 30.0;
// //         };
        
// //         ticket.setAmount(hours * rate);
// //         ticket.setStatus(TicketStatus.PAID);
        
// //         ParkingSpace space = ticket.getAllocatedSpace();
// //         space.releaseSpace();
// //         spaceRepo.save(space);
        
// //         ticketRepo.save(ticket);
// //         return true;
// //     }
    
// //     public Map<VehicleType, Long> getAvailability() {
// //         Map<VehicleType, Long> availability = new HashMap<>();
// //         for (VehicleType type : VehicleType.values()) {
// //             long count = spaceRepo.findByStatusAndCompatibleVehicleType(
// //                 SpaceStatus.AVAILABLE, type
// //             ).size();
// //             availability.put(type, count);
// //         }
// //         return availability;
// //     }
    
// //     public List<ParkingSpace> getAllSpaces() {
// //         return spaceRepo.findAll();
// //     }
    
// //     public List<ParkingTicket> getActiveTickets() {
// //         return ticketRepo.findByStatus(TicketStatus.ACTIVE);
// //     }
// // }