package com.campus.secondhand.message.mq;

import com.campus.secondhand.message.service.NotificationService;
import com.campus.secondhand.common.core.constants.MQConstants;
import com.campus.secondhand.common.core.dto.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = MQConstants.NOTIFICATION_QUEUE)
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitHandler
    public void handle(NotificationMessage message) {
        notificationService.create(message.getUserId(), message.getTitle(), message.getContent());
    }
}
