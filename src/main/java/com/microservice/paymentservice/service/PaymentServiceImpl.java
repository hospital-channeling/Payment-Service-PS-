package com.microservice.paymentservice.service;

import com.microservice.paymentservice.Utils.paymentMode;
import com.microservice.paymentservice.exception.PaymentServiceCustomException;
import com.microservice.paymentservice.model.TransactionDetails;
import com.microservice.paymentservice.payload.PaymentRequest;
import com.microservice.paymentservice.payload.PaymentResponse;
import com.microservice.paymentservice.repository.TransactionDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    private final TransactionDetailsRepository transactionDetailsRepository;
    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("PaymentServiceImpl | doPayment is called");

        log.info("PaymentServiceImpl | doPayment | Recording Payment Details: {}", paymentRequest);
        TransactionDetails transactionDetails
                = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .appointmentId(paymentRequest.getAppointmentId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();
        transactionDetails = transactionDetailsRepository.save(transactionDetails);

        log.info("Transaction Completed with Id: {}", transactionDetails.getId());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByAppointmentId(long appointmentId) {
        log.info("PaymentServiceImpl | getPaymentDetailsByOrderId is called");

        log.info("PaymentServiceImpl | getPaymentDetailsByOrderId | Getting payment details for the Order Id: {}", appointmentId);

        TransactionDetails transactionDetails
                = transactionDetailsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new PaymentServiceCustomException(
                        "TransactionDetails with given id not found",
                        "TRANSACTION_NOT_FOUND"));

        PaymentResponse paymentResponse
                = PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .paymentMode(paymentMode.valueOf(transactionDetails.getPaymentMode()))
                .paymentDate(transactionDetails.getPaymentDate())
                .appointmentId(transactionDetails.getAppointmentId())
                .status(transactionDetails.getPaymentStatus())
                .amount(transactionDetails.getAmount())
                .build();

        log.info("PaymentServiceImpl | getPaymentDetailsByOrderId | paymentResponse: {}", paymentResponse.toString());

        return paymentResponse;
    }

    @Override
    public List<PaymentResponse> getAllPaymentDetails() {
        log.info("PaymentServiceImpl | getAllPaymentDetails is called");

        List<PaymentResponse> paymentResponses = new ArrayList<>();

        List<TransactionDetails> transactionDetailsList = transactionDetailsRepository.findAll();

        for (TransactionDetails transactionDetails : transactionDetailsList) {
            PaymentResponse paymentResponse = PaymentResponse.builder()
                    .paymentId(transactionDetails.getId())
                    .paymentMode(paymentMode.valueOf(transactionDetails.getPaymentMode()))
                    .paymentDate(transactionDetails.getPaymentDate())
                    .appointmentId(transactionDetails.getAppointmentId())
                    .status(transactionDetails.getPaymentStatus())
                    .amount(transactionDetails.getAmount())
                    .build();

            paymentResponses.add(paymentResponse);
        }

        return paymentResponses;
    }
}
