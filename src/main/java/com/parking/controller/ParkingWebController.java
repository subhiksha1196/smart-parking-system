package com.parking.controller;

import com.parking.model.*;
import com.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Web controller for Parking Management System
 * Handles all HTTP requests and coordinates with ParkingService
 * 
 * @author Your Name
 * @version 1.0
 */
@Controller
@RequestMapping("/")
public class ParkingWebController {

    @Autowired
    private ParkingService parkingService;

    // ============================================================
    // HOME PAGE
    // ============================================================
    
    /**
     * Displays home page with parking availability
     * @param model Spring Model for view attributes
     * @return index view name
     */
    @GetMapping({"/", "/index"})
    public String home(Model model) {
        Map<VehicleType, Map<String, Integer>> availability = parkingService.getAvailability();
        model.addAttribute("availability", availability);
        return "index";
    }

    // ============================================================
    // CUSTOMER MANAGEMENT
    // ============================================================
    
    /**
     * Displays customer registration form
     * @return customer-register view name
     */
    @GetMapping("/register")
    public String registerForm() {
        return "customer-register";
    }

    /**
     * Processes customer registration
     * Automatically saves to file after registration
     * @param name Customer name
     * @param email Customer email
     * @param phone Customer phone number
     * @param handicapped Whether customer is handicapped
     * @param model Spring Model for view attributes
     * @return customer-success view on success, error view on failure
     */
    @PostMapping("/register")
    public String registerCustomer(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam(value = "handicapped", defaultValue = "false") boolean handicapped,
            Model model) {
        
        try {
            // Register customer (automatically saved to file in ParkingService)
            Customer newCustomer = parkingService.registerCustomer(name, phone, email, handicapped);
            
            model.addAttribute("customerName", newCustomer.getName());
            model.addAttribute("customerId", newCustomer.getId());
            model.addAttribute("email", newCustomer.getEmail());
            model.addAttribute("phone", newCustomer.getContact());
            model.addAttribute("isHandicapped", newCustomer.isHandicapped());
            model.addAttribute("loyaltyPoints", newCustomer.getLoyaltyPoints());
            
            return "customer-success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Lists all registered customers
     * @param model Spring Model for view attributes
     * @return customers-list view name
     */
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Customer> customers = parkingService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customers-list";
    }

    // ============================================================
    // PARKING OPERATIONS
    // ============================================================
    
    /**
     * Displays parking form with customer list
     * @param model Spring Model for view attributes
     * @return park view name
     */
    @GetMapping("/park")
    public String parkForm(Model model) {
        model.addAttribute("customers", parkingService.getAllCustomers());
        return "park";
    }

    /**
     * Generates parking ticket for a vehicle
     * Automatically saves ticket and space status to file
     * @param licensePlate Vehicle license plate
     * @param vehicleType Type of vehicle (CAR/MOTORCYCLE/TRUCK)
     * @param reservationId Optional reservation ID
     * @param contact Contact number (for guests)
     * @param customerId Customer ID (optional)
     * @param model Spring Model for view attributes
     * @return park-result view on success, error view on failure
     */
    @PostMapping("/park-result")
    public String generateParkingTicket(
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("vehicleType") VehicleType vehicleType,
            @RequestParam(value = "reservationId", required = false) String reservationId,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam(value = "customerId", required = false) Long customerId,
            Model model) {
        
        // Park vehicle (automatically saved to file in ParkingService)
        ParkingTicket ticket = parkingService.parkVehicleWithCustomer(
                licensePlate, vehicleType, contact, customerId, reservationId);

        if (ticket == null) {
            model.addAttribute("errorMessage", "Parking failed. No available space for " + vehicleType + ".");
            return "error";
        }

        model.addAttribute("success", true);
        model.addAttribute("ticketId", ticket.getTicketId());
        model.addAttribute("licensePlate", ticket.getVehicle().getLicensePlate());
        model.addAttribute("spaceId", ticket.getAllocatedSpace().getSpaceId());
        model.addAttribute("entryTime", ticket.getEntryTime());
        model.addAttribute("customerName", ticket.getCustomer() != null ? ticket.getCustomer().getName() : "Guest");
        
        return "park-result";
    }

    /**
     * Lists all active parking tickets
     * @param model Spring Model for view attributes
     * @return tickets view name
     */
    @GetMapping("/tickets")
    public String activeTickets(Model model) {
        List<ParkingTicket> tickets = parkingService.getActiveTickets();
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    // ============================================================
    // EXIT AND PAYMENT
    // ============================================================
    
    /**
     * Displays exit form
     * @return exit view name
     */
    @GetMapping("/exit")
    public String exitForm() {
        return "exit";
    }

    /**
     * Processes exit request and calculates fees
     * @param ticketId Parking ticket ID
     * @param model Spring Model for view attributes
     * @return exit-payment view with fee details, error view on failure
     */
    @PostMapping("/exit-payment")
    public String processExitRequest(
            @RequestParam("ticketId") String ticketId,
            Model model) {
        
        // Calculate preview of fees
        Map<String, Object> preview = parkingService.calculateExitPreview(ticketId);
        
        if (!(boolean) preview.getOrDefault("success", false)) {
            model.addAttribute("errorMessage", preview.get("message"));
            return "error";
        }
        
        model.addAttribute("ticketId", ticketId);
        model.addAttribute("amount", preview.get("amount"));
        model.addAttribute("hours", preview.get("hours"));
        model.addAttribute("isWeekend", preview.get("isWeekend"));
        model.addAttribute("hasDiscount", preview.get("hasDiscount"));
        
        return "exit-payment";
    }

    /**
     * Finalizes payment and processes vehicle exit
     * Automatically saves updated data to files
     * @param ticketId Parking ticket ID
     * @param paymentMethod Payment method (CASH/CARD/UPI)
     * @param paymentDetails Payment details (card number, UPI ID, etc.)
     * @param cashReceived Amount of cash received
     * @param model Spring Model for view attributes
     * @return exit-result view with receipt, error view on failure
     */
    @PostMapping("/exit-result")
    public String finalizePayment(
            @RequestParam("ticketId") String ticketId,
            @RequestParam("paymentMethod") PaymentMethod paymentMethod,
            @RequestParam(value = "paymentDetails", required = false) String paymentDetails,
            @RequestParam(value = "cashReceived", defaultValue = "0.0") double cashReceived,
            Model model) {
        
        // Process exit and payment (automatically saved to file in ParkingService)
        Map<String, Object> result = parkingService.exitVehicleWithPayment(
                ticketId, paymentMethod, paymentDetails, cashReceived);

        if (!(boolean) result.getOrDefault("success", false)) {
            model.addAttribute("errorMessage", result.get("message"));
            return "error";
        }

        ParkingTicket ticket = (ParkingTicket) result.get("ticket");
        
        model.addAttribute("success", true);
        model.addAttribute("ticketId", ticketId);
        model.addAttribute("amount", result.get("amount"));
        model.addAttribute("paymentMethod", paymentMethod.name());
        model.addAttribute("changeReturned", result.getOrDefault("change", 0.0));
        model.addAttribute("discountApplied", result.get("discountApplied"));
        model.addAttribute("loyaltyPointsEarned", ticket.getLoyaltyPointsEarned());
        model.addAttribute("customerName", ticket.getCustomer() != null ? ticket.getCustomer().getName() : "Guest");
        
        return "exit-result";
    }

    // ============================================================
    // RESERVATIONS
    // ============================================================
    
    /**
     * Displays reservation form with customer list
     * @param model Spring Model for view attributes
     * @return reserve view name
     */
    @GetMapping("/reserve")
    public String reserveForm(Model model) {
        List<Customer> customers = parkingService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "reserve";
    }

    /**
     * Creates a new parking reservation
     * Automatically saves reservation to file
     * @param customerId Customer ID
     * @param vehicleType Type of vehicle to reserve for
     * @param validityHours How many hours the reservation is valid
     * @param model Spring Model for view attributes
     * @return reserve-result view with reservation details, error view on failure
     */
    @PostMapping("/reserve-result")
    public String createReservation(
            @RequestParam("customerId") Long customerId,
            @RequestParam("vehicleType") VehicleType vehicleType,
            @RequestParam("validityHours") int validityHours,
            Model model) {
        
        // Create reservation (automatically saved to file in ParkingService)
        Reservation reservation = parkingService.createReservation(customerId, vehicleType, validityHours);

        if (reservation == null) {
            model.addAttribute("errorMessage", "Reservation failed. No space available for " + vehicleType + ".");
            return "error";
        }

        model.addAttribute("success", true);
        model.addAttribute("customerName", reservation.getCustomer().getName());
        model.addAttribute("reservationId", reservation.getReservationId());
        model.addAttribute("spaceId", reservation.getReservedSpace().getSpaceId());
        model.addAttribute("validUntil", reservation.getExpiresAt());
        model.addAttribute("hoursRemaining", reservation.getHoursRemaining());
        
        return "reserve-result";
    }

    /**
     * Lists all active reservations
     * Expires old reservations before displaying
     * @param model Spring Model for view attributes
     * @return reservations-list view name
     */
    @GetMapping("/reservations")
    public String listReservations(Model model) {
        parkingService.expireOldReservations();
        List<Reservation> reservations = parkingService.getActiveReservations();
        model.addAttribute("reservations", reservations);
        return "reservations-list";
    }

    // ============================================================
    // REPORTS AND VISUALIZATION
    // ============================================================
    
    /**
     * Displays reports and analytics dashboard
     * @param model Spring Model for view attributes
     * @return reports view name
     */
    @GetMapping("/report")
    public String reports(Model model) {
        Map<String, Object> revenueReport = parkingService.getRevenueReport();
        Map<VehicleType, Map<String, Integer>> availability = parkingService.getAvailability();
        
        model.addAttribute("revenueReport", revenueReport);
        model.addAttribute("availability", availability);
        
        return "reports";
    }

    /**
     * Visualizes parking space layout by floor
     * @param model Spring Model for view attributes
     * @return visualize view name
     */
    @GetMapping("/visualize")
    public String visualize(Model model) {
        Map<Integer, List<ParkingSpace>> floorMap = parkingService.getSpacesByFloor();
        model.addAttribute("floorMap", floorMap);
        return "visualize";
    }

    // ============================================================
    // MANUAL SAVE ENDPOINT (Optional - for testing)
    // ============================================================
    
    /**
     * Manually triggers data save to files
     * Useful for testing file storage functionality
     * @return redirect to home page
     */
    @GetMapping("/save-data")
    public String saveData() {
        parkingService.saveAllDataToFiles();
        return "redirect:/?saved=true";
    }

    // ============================================================
    // ERROR PAGE
    // ============================================================
    
    /**
     * Displays error page
     * @return error view name
     */
    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }
}
