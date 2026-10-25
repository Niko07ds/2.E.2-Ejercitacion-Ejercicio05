package ejercicio05;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int TOTAL_REQUESTS = 30;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Ejercicio 5: Gestor concurrente de reservas de la sala de lectura ===");
        System.out.println("Lugares en la sala: " + ReadingRoomManager.CAPACITY);
        System.out.println("Solicitudes concurrentes a simular: " + TOTAL_REQUESTS);

        // Estructuras compartidas entre los hilos
        Set<ReadingRoomManager> observedInstances = ConcurrentHashMap.newKeySet();
        Map<ReservationResult, AtomicInteger> counters = new EnumMap<>(ReservationResult.class);
        for (ReservationResult result : ReservationResult.values()) {
            counters.put(result, new AtomicInteger());
        }

        CountDownLatch startSignal = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        // Se crean los hilos (uno por puesto de atención, con socios diferentes)
        for (int i = 1; i <= TOTAL_REQUESTS; i++) {
            String memberId = String.format("SOCIO-%02d", i);
            Thread thread = new Thread(
                    new ReservationRequest(memberId, startSignal, observedInstances, counters),
                    "puesto-" + i);
            threads.add(thread);
            thread.start();
        }

        System.out.println("\n--- Lanzando las solicitudes al mismo tiempo ---");
        startSignal.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        ReadingRoomManager manager = ReadingRoomManager.getInstance();
        List<String> active = manager.getActiveReservations();
        int available = manager.getAvailableSeats();

        System.out.println("\n--- Resultados de la simulación ---");
        System.out.println("Instancias distintas del gestor observadas por los hilos: " + observedInstances.size());
        System.out.println("Reservas aceptadas: " + counters.get(ReservationResult.RESERVED).get());
        System.out.println("Solicitudes rechazadas por falta de lugar: " + counters.get(ReservationResult.NO_SEATS).get());
        System.out.println("Solicitudes rechazadas por duplicado: " + counters.get(ReservationResult.DUPLICATE).get());
        System.out.println("Reservas activas (" + active.size() + "): " + active);
        System.out.println("Lugares disponibles: " + available);

        boolean invariants = active.size() <= ReadingRoomManager.CAPACITY
                && available >= 0
                && active.size() + available == ReadingRoomManager.CAPACITY;
        System.out.println("¿Se respetan los límites (reservas <= 10 y disponibilidad >= 0)? " + (invariants ? "Sí" : "No"));

        // Reserva duplicada de un socio que ya tiene lugar
        System.out.println("\n--- Reserva duplicada ---");
        String firstMember = active.get(0);
        System.out.println("Reservar nuevamente a " + firstMember + ": " + manager.reserve(firstMember));
        System.out.println("Lugares disponibles: " + manager.getAvailableSeats());

        // Cancelar una reserva inexistente no debe liberar lugares
        System.out.println("\n--- Cancelación de una reserva inexistente ---");
        System.out.println("Cancelar a SOCIO-99: " + (manager.cancel("SOCIO-99") ? "cancelada" : "no existía la reserva"));
        System.out.println("Lugares disponibles: " + manager.getAvailableSeats());

        // Cancelar una reserva existente recupera el lugar
        System.out.println("\n--- Cancelación de una reserva existente ---");
        System.out.println("Cancelar a " + firstMember + ": " + (manager.cancel(firstMember) ? "cancelada" : "no existía la reserva"));
        System.out.println("Lugares disponibles: " + manager.getAvailableSeats());

        // El lugar recuperado puede ser reservado por otro socio
        System.out.println("\n--- Nueva reserva en el lugar recuperado ---");
        System.out.println("Reservar a SOCIO-31: " + manager.reserve("SOCIO-31"));
        System.out.println("Reservas activas: " + manager.getActiveReservations().size()
                + " | Lugares disponibles: " + manager.getAvailableSeats());
    }
}
