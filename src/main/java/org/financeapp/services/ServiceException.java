package org.financeapp.services;

public class ServiceException extends Exception {

    // Manejamos una clase customizada de errores, en la que incluimos dos métodos, uno para reportar solo un mensaje de error y otro para devolver el mensaje de error y una posible causa.

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
