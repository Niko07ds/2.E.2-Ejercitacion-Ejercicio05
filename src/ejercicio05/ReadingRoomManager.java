package ejercicio05;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestor de reservas de la sala de lectura.
 * Singleton seguro ante concurrencia: double-checked locking con atributo volatile.
 */
public class ReadingRoomManager {

    public static final int CAPACITY = 10;

    // volatile: evita que otro hilo vea una instancia parcialmente construida
    private static volatile ReadingRoomManager instance;

    // Socios con reserva activa; solo se accede dentro de métodos synchronized
    private final Set<String> reservations = new LinkedHashSet<>();

    // Constructor privado
    private ReadingRoomManager() {
    }

    public static ReadingRoomManager getInstance() {
        if (instance == null) {                          // 1.ª comprobación (sin bloqueo)
            synchronized (ReadingRoomManager.class) {
                if (instance == null) {                  // 2.ª comprobación (con bloqueo)
                    instance = new ReadingRoomManager();
                }
            }
        }
        return instance;
    }

    /**
     * Comprueba disponibilidad y registra la reserva como una única operación atómica.
     */
    public synchronized ReservationResult reserve(String memberId) {
        validateMember(memberId);
        String id = memberId.trim();
        if (reservations.contains(id)) {
            return ReservationResult.DUPLICATE;
        }
        if (reservations.size() >= CAPACITY) {
            return ReservationResult.NO_SEATS;
        }
        reservations.add(id);
        return ReservationResult.RESERVED;
    }

    /**
     * Cancela una reserva. Si no existe, no modifica nada.
     * @return true si se canceló; false si el socio no tenía reserva
     */
    public synchronized boolean cancel(String memberId) {
        validateMember(memberId);
        return reservations.remove(memberId.trim());
    }

    // Copia inmodificable de las reservas activas
    public synchronized List<String> getActiveReservations() {
        return List.copyOf(reservations);
    }

    public synchronized int getAvailableSeats() {
        return CAPACITY - reservations.size();
    }

    private void validateMember(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("El identificador del socio no puede estar vacío.");
        }
    }
}
