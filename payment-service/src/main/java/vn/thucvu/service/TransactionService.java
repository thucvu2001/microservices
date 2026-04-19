package vn.thucvu.service;

import vn.thucvu.common.TransactionStatus;
import vn.thucvu.controller.request.AdvanceSearchRequest;
import vn.thucvu.controller.response.PageResponse;
import vn.thucvu.controller.response.TransactionResponse;
import vn.thucvu.model.Transaction;

public interface TransactionService {

    PageResponse<?> getAllTransactions(String keyword, String sort, int page, int size);

    PageResponse<?> advanceSearch(AdvanceSearchRequest request);

    TransactionResponse getTransactionDetail(Long id);

    String getOrderId(String paymentId);

    Long createTransaction(Transaction transaction);

    void updateTransactionStatus(String paymentId, TransactionStatus status);

}
