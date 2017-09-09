package edu.drc.exception;

public class BookingNotExistException extends Exception{
    public BookingNotExistException(){
        super("> Error: the booking bing cancelled does not exist!");
    }
}
