package ejercicio05;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simula un puesto de atención que solicita una reserva en su propio hilo.
 */
public class ReservationRequest implements Runnable {

    private final String memberId;
    private final CountDownLatch startSignal;
    private final Set<ReadingRoomManager> observedInstances;
    private final Map<ReservationResult, AtomicInteger> counters;

    public ReservationRequest(String memberId, CountDownLatch startSignal, Set<ReadingRoomManager> observedInstances, Map<ReservationResult, AtomicInteger> counters) {
        this.memberId = memberId;
        this.startSignal = startSignal;
        this.observedInstances = observedInstances;
        this.counters = counters;
    }

    @Override
    public void run() {
        try {
            // Todos los hilos esperan la señal para arrancar al mismo tiempo
            startSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        ReadingRoomManager manager = ReadingRoomManager.getInstance();
        observedInstances.add(manager);
        ReservationResult result = manager.reserve(memberId);
        counters.get(result).incrementAndGet();
    }
}
