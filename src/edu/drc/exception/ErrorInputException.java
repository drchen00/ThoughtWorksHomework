package edu.drc.exception;

public class ErrorInputException extends Exception{
    public ErrorInputException(){
        super("> Error: the booking is invalid!");
    }
}
