package com.mariaignatova.order.listener;

import com.mariaignatova.order.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductEventListener.class);

    @RabbitListener(queues = RabbitConfig.PRODUCT_QUEUE)
    public void onProductCreated(Map<String, Object> event) {
        log.info("Received product event: id={}, name={}",
                event.get("id"), event.get("name"));
    }
}