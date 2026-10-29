package com.example.MelodAI.controller;

import com.example.MelodAI.service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000") // Matches your frontend URL
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Object> request) {
        try {
            // Extract amount (in cents) and product name from the incoming JSON request
            Long amount = Long.valueOf(request.getOrDefault("amount", 1000).toString());
            String productName = (String) request.getOrDefault("productName", "MelodAI Credits");

            // Call the service to generate the Stripe-hosted checkout URL
            String checkoutUrl = stripeService.createCheckoutSession(amount, productName);

            // Return the URL to the frontend so it can redirect the user
            return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));

        } catch (StripeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request parameters: " + ex.getMessage()));
        }
    }
}