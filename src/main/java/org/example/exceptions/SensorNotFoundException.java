package org.example.exceptions;

public class SensorNotFoundException extends RuntimeException {

    public SensorNotFoundException(String idOrName) {
        super("Capteur introuvable : " + idOrName);
    }
}

