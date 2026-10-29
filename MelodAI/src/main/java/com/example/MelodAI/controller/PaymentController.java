package com.example.MelodAI.controller;

import com.example.MelodAI.dto.ChargeRequest;
import com.example.MelodAI.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChargeController {

    @Autowired
    private StripeService paymentsService;

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody ChargeRequest chargeRequest) {
        try {
            chargeRequest.setDescription("Credits Charge");
            chargeRequest.setCurrency(ChargeRequest.Currency.USD);
            Charge charge = paymentsService.charge(chargeRequest);

            // Return a JSON response map or a response DTO
            return ResponseEntity.ok(Map.of(
                    "id", charge.getId(),
                    "status", charge.getStatus(),
                    "balance_transaction", charge.getBalanceTransaction()
            ));
        } catch (StripeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}