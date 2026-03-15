// ❌ 反例：事务安全风险

package com.example.order.service.impl;

import com.example.order.entity.OrderPO;
import com.example.order.repository.OrderRepository;
import com.example.payment.client.PaymentClient;
import com.example.message.MQProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final MQProducer mqProducer;

    public OrderServiceImpl(OrderRepository orderRepository,
                           PaymentClient paymentClient,
                           MQProducer mqProducer) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
        this.mqProducer = mqProducer;
    }

    // ❌ 错误：事务内调用远程服务
    @Transactional
    public void createOrder(OrderCreateRequest request) {
        OrderPO order = createOrderPO(request);
        orderRepository.save(order);

        // 🔴 严重问题：事务内调用支付服务
        // 风险：支付服务超时会导致数据库连接长时间占用
        // 风险：支付成功但事务回滚会导致数据不一致
        paymentClient.charge(order.getId(), order.getAmount());
    }

    // ❌ 错误：事务内发送 MQ
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        orderRepository.updateStatus(orderId, status);

        // 🔴 严重问题：事务内发送消息
        // 风险：消息发送成功但事务回滚，导致消息与数据不一致
        mqProducer.send(new OrderStatusChangedEvent(orderId, status));
    }

    // ❌ 错误：事务内调用异步方法
    @Transactional
    public void processOrder(Long orderId) {
        OrderPO order = orderRepository.findById(orderId).orElseThrow();

        // 🟡 风险：异步操作可能在事务提交前执行
        asyncProcessor.process(order);
    }
}

// 正确做法见 good/transaction-safe.java
