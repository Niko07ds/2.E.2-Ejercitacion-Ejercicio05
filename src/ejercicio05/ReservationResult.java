package ejercicio05;

/**
 * Resultado de un intento de reserva.
 */
public enum ReservationResult {
    RESERVED,    // reserva aceptada
    DUPLICATE,   // el socio ya tenía una reserva
    NO_SEATS     // no quedan lugares disponibles
}
