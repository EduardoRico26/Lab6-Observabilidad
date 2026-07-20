package edu.eci.arsw.observability.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final Counter orderCreatedCounter;
    private final Counter orderFailedCounter;
    private final Random random = new Random();

    public OrderController(MeterRegistry meterRegistry) {
        this.orderCreatedCounter = Counter.builder("orders_created_total")
                .description("Total de pedidos creados correctamente")
                .register(meterRegistry);

        this.orderFailedCounter = Counter.builder("orders_failed_total")
                .description("Total de pedidos fallidos")
                .register(meterRegistry);
    }

    @PostMapping
    public Map<String, Object> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        logger.info("Solicitud recibida para crear pedido. customerId={}, total={}",
                request.customerId(), request.total());

        String orderId = "ORD-" + UUID.randomUUID();

        orderCreatedCounter.increment();

        logger.info("Pedido creado correctamente. orderId={}", orderId);

        return Map.of(
                "orderId", orderId,
                "customerId", request.customerId(),
                "total", request.total(),
                "status", "CREATED"
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrder(@PathVariable String id) {
        logger.debug("Consultando pedido con id={}", id);

        return Map.of(
                "orderId", id,
                "status", "CREATED"
        );
    }

    @GetMapping("/simulate-latency")
    public Map<String, Object> simulateLatency() throws InterruptedException {
        int delay = 500 + random.nextInt(2500);

        logger.warn("Simulando latencia artificial de {} ms", delay);

        Thread.sleep(delay);

        return Map.of(
                "message", "Respuesta con latencia simulada",
                "delayMs", delay
        );
    }

    @GetMapping("/simulate-error")
    public Map<String, Object> simulateError() {
        logger.error("Error simulado en el servicio de pedidos");

        orderFailedCounter.increment();

        throw new IllegalStateException("Error simulado para análisis de observabilidad");
    }

    public record CreateOrderRequest(
            @NotBlank String customerId,
            @Min(1) double total
    ) {
    }
}
