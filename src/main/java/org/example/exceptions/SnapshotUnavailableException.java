package org.example.exceptions;

public class SnapshotUnavailableException extends RuntimeException {

    public SnapshotUnavailableException(String message) {
        super(message);
    }

    public SnapshotUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

