package edu.drc.exception;

public class BookingConfilctsException extends Exception {
    public BookingConfilctsException(){
        super("> Error: the booking conflicts with existing bookings!");
    }
}
