package vn.thucvu.service;

import com.google.zxing.WriterException;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.controller.request.PlaceOrderRequest;
import vn.thucvu.controller.request.UpdateOrderRequest;
import vn.thucvu.controller.response.OrderListResponse;
import vn.thucvu.model.Order;

import java.awt.image.BufferedImage;


public interface OrderService {

    OrderListResponse getAllOrders(OrderStatus status, String sort, int page, int size);

    Order getOrderDetail(String orderId);

    Order createOrder(PlaceOrderRequest req);

    void updateOrder(UpdateOrderRequest req);

    void checkoutOrder(String orderId);

    void changeOrderStatus(String orderId, OrderStatus status);

    void cancelOrder(String orderId);

    BufferedImage generateQRCodeImage(String text) throws WriterException;

    BufferedImage generateBarCodeImage(String barcode) throws WriterException;

}
