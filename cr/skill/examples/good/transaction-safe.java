// ✅ 正例：事务安全

package com.example.order.service.impl;

import com.example.order.entity.OrderPO;
import com.example.order.event.OrderCreatedEvent;
import com.example.order.repository.OrderRepository;
import com.example.payment.client.PaymentClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                           PaymentClient paymentClient,
                           TransactionTemplate transactionTemplate,
                           ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
        this.transactionTemplate = transactionTemplate;
        this.eventPublisher = eventPublisher;
    }

    // ✅ 正确：先执行远程调用，再开启本地事务
    public void createOrder(OrderCreateRequest request) {
        // 1. 先执行远程调用（事务外）
        PaymentResult result = paymentClient.charge(request.getAmount());

        if (!result.isSuccess()) {
            throw new PaymentException("支付失败");
        }

        // 2. 再开启本地事务保存数据
        transactionTemplate.execute(status -> {
            OrderPO order = createOrderPO(request);
            order.setPaymentId(result.getPaymentId());
            orderRepository.save(order);

            // ✅ 发布事件，由监听器在事务提交后处理
            eventPublisher.publishEvent(new OrderCreatedEvent(order.getId()));
            return order;
        });
    }

    // ✅ 正确：事务内只操作数据库
    @Transactional
    public OrderPO saveOrder(OrderPO order) {
        return orderRepository.save(order);
    }
}

// ✅ 事件监听器：事务提交后执行
package com.example.order.listener;

import com.example.order.event.OrderCreatedEvent;
import com.example.message.MQProducer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {

    private final MQProducer mqProducer;

    public OrderEventListener(MQProducer mqProducer) {
        this.mqProducer = mqProducer;
    }

    // ✅ 正确：AFTER_COMMIT 确保事务提交后才发送消息
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        mqProducer.send(new OrderMessage(event.getOrderId()));
    }
}
