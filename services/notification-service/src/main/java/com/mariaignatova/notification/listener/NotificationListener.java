package com.mariaignatova.notification.listener;

import com.mariaignatova.notification.config.RabbitConfig;
import com.mariaignatova.notification.event.OrderCancelledEvent;
import com.mariaignatova.notification.event.OrderNotificationEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final MeterRegistry meterRegistry;

    public NotificationListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void onOrderCreated(OrderNotificationEvent event) {
        Timer timer = meterRegistry.timer(
                "notification.processing.duration", "event_type", "order_created");

        timer.record(() -> {
            log.info("[NOTIFICATION] New order: orderId={}, productId={}, qty={}, total={}",
                    event.orderId(), event.productId(), event.quantity(), event.totalPrice());

            meterRegistry.counter(
                    "notifications.sent.total", "event_type", "order_created"
            ).increment();
        });
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void onOrderCancelled(OrderCancelledEvent event) {
        Timer timer = meterRegistry.timer(
                "notification.processing.duration",
                "event_type", "order_cancelled");

        timer.record(() -> {
            log.info("[NOTIFICATION] Order cancelled: orderId={}", event.orderId());

            meterRegistry.counter(
                    "notifications.sent.total",
                    "event_type", "order_cancelled"
            ).increment();
        });
    }
}