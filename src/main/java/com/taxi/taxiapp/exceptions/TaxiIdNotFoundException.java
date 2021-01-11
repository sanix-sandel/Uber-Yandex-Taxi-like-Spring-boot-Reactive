package com.taxi.taxiapp.exceptions;

public class TaxiIdNotFoundException extends RuntimeException{

    public TaxiIdNotFoundException(String message) {
        super(message);
    }

    public TaxiIdNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
