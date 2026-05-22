package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.ReturnDTO;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.OrderItem;
import com.multivendor.ecommerce.entity.Return;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.OrderItemRepository;
import com.multivendor.ecommerce.repository.OrderRepository;
import com.multivendor.ecommerce.repository.PayoutRepository;
import com.multivendor.ecommerce.repository.ReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final PayoutRepository payoutRepository;

    /**
     * Customer raises a return request.
     * BUSINESS RULE: Order must be in DELIVERED state.
     */
    @Transactional
    public ReturnDTO.Response requestReturn(ReturnDTO.Request req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", req.getOrderId()));

        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new BusinessException("Return can only be requested for DELIVERED orders. Current status: " + order.getStatus());
        }

        OrderItem orderItem = orderItemRepository.findById(req.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", req.getOrderItemId()));

        if (!orderItem.getOrder().getId().equals(order.getId())) {
            throw new BusinessException("Order item does not belong to the given order.");
        }

        // Update order status to RETURN_REQUESTED
        order.setStatus(Order.OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        Return returnReq = Return.builder()
                .order(order)
                .orderItem(orderItem)
                .reason(req.getReason())
                .status(Return.ReturnStatus.REQUESTED)
                .build();

        Return saved = returnRepository.save(returnReq);
        log.info("Return requested: returnId={}, orderId={}", saved.getId(), order.getId());
        return ReturnDTO.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReturnDTO.Response> getAllReturns() {
        return returnRepository.findAll().stream()
                .map(ReturnDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReturnDTO.Response getReturnById(Long id) {
        Return ret = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return", id));
        return ReturnDTO.Response.from(ret);
    }

    @Transactional(readOnly = true)
    public List<ReturnDTO.Response> getReturnsByStatus(Return.ReturnStatus status) {
        return returnRepository.findByStatus(status).stream()
                .map(ReturnDTO.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * Admin approves a return.
     * BUSINESS RULE: Inventory increases when return is approved.
     *                Payout is put on HOLD.
     */
    @Transactional
    public ReturnDTO.Response approveReturn(Long returnId) {
        Return ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return", returnId));

        if (ret.getStatus() != Return.ReturnStatus.REQUESTED) {
            throw new BusinessException("Return is not in REQUESTED state. Current: " + ret.getStatus());
        }

        ret.setStatus(Return.ReturnStatus.APPROVED);
        ret.setResolvedAt(LocalDateTime.now());

        // BUSINESS RULE: Restore inventory on return approval
        OrderItem item = ret.getOrderItem();
        inventoryService.restoreStockOnReturn(item.getProduct().getId(), item.getQuantity());

        // BUSINESS RULE: Hold payout for the returned item
        payoutRepository.findByOrderId(ret.getOrder().getId()).forEach(payout -> {
            if (payout.getOrderItem().getId().equals(item.getId())) {
                payout.setStatus(com.multivendor.ecommerce.entity.Payout.PayoutStatus.HOLD);
                payoutRepository.save(payout);
            }
        });

        // Mark order as RETURNED
        Order order = ret.getOrder();
        order.setStatus(Order.OrderStatus.RETURNED);
        orderRepository.save(order);

        Return saved = returnRepository.save(ret);
        log.info("Return APPROVED: returnId={}, inventoryRestored for productId={}",
                saved.getId(), item.getProduct().getId());
        return ReturnDTO.Response.from(saved);
    }

    /**
     * Admin rejects a return.
     */
    @Transactional
    public ReturnDTO.Response rejectReturn(Long returnId) {
        Return ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return", returnId));

        if (ret.getStatus() != Return.ReturnStatus.REQUESTED) {
            throw new BusinessException("Return is not in REQUESTED state. Current: " + ret.getStatus());
        }

        ret.setStatus(Return.ReturnStatus.REJECTED);
        ret.setResolvedAt(LocalDateTime.now());

        // Revert order status back to DELIVERED
        Order order = ret.getOrder();
        order.setStatus(Order.OrderStatus.DELIVERED);
        orderRepository.save(order);

        Return saved = returnRepository.save(ret);
        log.info("Return REJECTED: returnId={}", saved.getId());
        return ReturnDTO.Response.from(saved);
    }
}
