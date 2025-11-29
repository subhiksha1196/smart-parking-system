package com.parking.service;

import com.parking.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based storage service for persisting parking system data
 * Implements file I/O operations for customers, tickets, and reservations
 * 
 * @author Your Name
 * @version 1.0
 */
public class FileStorageService {
    
    private static final String DATA_DIR = "data/";
    private static final String CUSTOMERS_FILE = DATA_DIR + "customers.json";
    private static final String TICKETS_FILE = DATA_DIR + "tickets.json";
    private static final String RESERVATIONS_FILE = DATA_DIR + "reservations.json";
    private static final String SPACES_FILE = DATA_DIR + "parking_spaces.json";
    
    private final Gson gson;
    
    /**
     * Constructor initializes Gson with LocalDateTime adapter
     * Creates data directory if it doesn't exist
     */
    public FileStorageService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        
        createDataDirectory();
    }
    
    /**
     * Creates data directory if it doesn't exist
     */
    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Data directory created: " + DATA_DIR);
        }
    }
    
    // ===== CUSTOMER OPERATIONS =====
    
    /**
     * Saves all customers to file
     * @param customers List of customers to save
     * @throws IOException if file write fails
     */
    public void saveCustomers(List<Customer> customers) throws IOException {
        writeToFile(CUSTOMERS_FILE, customers);
        System.out.println("Saved " + customers.size() + " customers to file");
    }
    
    /**
     * Loads all customers from file
     * @return List of customers, empty list if file doesn't exist
     * @throws IOException if file read fails
     */
    public List<Customer> loadCustomers() throws IOException {
        Type listType = new TypeToken<ArrayList<Customer>>(){}.getType();
        List<Customer> customers = readFromFile(CUSTOMERS_FILE, listType);
        System.out.println("Loaded " + customers.size() + " customers from file");
        return customers;
    }
    
    // ===== TICKET OPERATIONS =====
    
    /**
     * Saves all parking tickets to file
     * @param tickets List of tickets to save
     * @throws IOException if file write fails
     */
    public void saveTickets(List<ParkingTicket> tickets) throws IOException {
        writeToFile(TICKETS_FILE, tickets);
        System.out.println("Saved " + tickets.size() + " tickets to file");
    }
    
    /**
     * Loads all parking tickets from file
     * @return List of tickets, empty list if file doesn't exist
     * @throws IOException if file read fails
     */
    public List<ParkingTicket> loadTickets() throws IOException {
        Type listType = new TypeToken<ArrayList<ParkingTicket>>(){}.getType();
        List<ParkingTicket> tickets = readFromFile(TICKETS_FILE, listType);
        System.out.println("Loaded " + tickets.size() + " tickets from file");
        return tickets;
    }
    
    // ===== RESERVATION OPERATIONS =====
    
    /**
     * Saves all reservations to file
     * @param reservations List of reservations to save
     * @throws IOException if file write fails
     */
    public void saveReservations(List<Reservation> reservations) throws IOException {
        writeToFile(RESERVATIONS_FILE, reservations);
        System.out.println("Saved " + reservations.size() + " reservations to file");
    }
    
    /**
     * Loads all reservations from file
     * @return List of reservations, empty list if file doesn't exist
     * @throws IOException if file read fails
     */
    public List<Reservation> loadReservations() throws IOException {
        Type listType = new TypeToken<ArrayList<Reservation>>(){}.getType();
        List<Reservation> reservations = readFromFile(RESERVATIONS_FILE, listType);
        System.out.println("Loaded " + reservations.size() + " reservations from file");
        return reservations;
    }
    
    // ===== PARKING SPACE OPERATIONS =====
    
    /**
     * Saves all parking spaces to file
     * @param spaces List of parking spaces to save
     * @throws IOException if file write fails
     */
    public void saveParkingSpaces(List<ParkingSpace> spaces) throws IOException {
        writeToFile(SPACES_FILE, spaces);
        System.out.println("Saved " + spaces.size() + " parking spaces to file");
    }
    
    /**
     * Loads all parking spaces from file
     * @return List of parking spaces, empty list if file doesn't exist
     * @throws IOException if file read fails
     */
    public List<ParkingSpace> loadParkingSpaces() throws IOException {
        Type listType = new TypeToken<ArrayList<ParkingSpace>>(){}.getType();
        List<ParkingSpace> spaces = readFromFile(SPACES_FILE, listType);
        System.out.println("Loaded " + spaces.size() + " parking spaces from file");
        return spaces;
    }
    
    // ===== GENERIC FILE OPERATIONS =====
    
    /**
     * Generic method to write any object to JSON file
     * @param filename Name of the file
     * @param data Data to write
     * @throws IOException if file write fails
     */
    private void writeToFile(String filename, Object data) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
        }
    }
    
    /**
     * Generic method to read object from JSON file
     * @param filename Name of the file
     * @param type Type of object to deserialize
     * @return Deserialized object, or empty list if file doesn't exist
     * @throws IOException if file read fails
     */
    private <T> T readFromFile(String filename, Type type) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + filename + ", returning empty list");
            return gson.fromJson("[]", type);
        }
        
        try (FileReader reader = new FileReader(filename)) {
            return gson.fromJson(reader, type);
        }
    }
    
    /**
     * Clears all data files (for testing purposes)
     */
    public void clearAllData() {
        deleteFile(CUSTOMERS_FILE);
        deleteFile(TICKETS_FILE);
        deleteFile(RESERVATIONS_FILE);
        deleteFile(SPACES_FILE);
        System.out.println("All data files cleared");
    }
    
    /**
     * Deletes a specific file
     * @param filename Name of file to delete
     */
    private void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }
}

/**
 * Adapter class for serializing/deserializing LocalDateTime with Gson
 */
class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>, 
                                       com.google.gson.JsonDeserializer<LocalDateTime> {
    
    @Override
    public com.google.gson.JsonElement serialize(LocalDateTime src, Type typeOfSrc, 
                                                  com.google.gson.JsonSerializationContext context) {
        return new com.google.gson.JsonPrimitive(src.toString());
    }
    
    @Override
    public LocalDateTime deserialize(com.google.gson.JsonElement json, Type typeOfT, 
                                      com.google.gson.JsonDeserializationContext context) {
        return LocalDateTime.parse(json.getAsString());
    }
}