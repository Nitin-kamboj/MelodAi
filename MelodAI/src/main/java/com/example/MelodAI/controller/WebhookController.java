package com.example.MelodAI.controller;

import com.example.MelodAI.model.Users;
import com.example.MelodAI.model.Credits;
import com.example.MelodAI.repository.usersRepository;
import com.example.MelodAI.repository.creditsRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired
    private usersRepository userRepository;

    @Autowired
    private creditsRepository creditsRepository;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("🔥 WEBHOOK RECEIVED!");

        if (sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("❌ SIGNATURE VERIFICATION FAILED: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parsing error");
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        switch (event.getType()) {
            case "checkout.session.completed":
                if (stripeObject instanceof Session) {
                    Session session = (Session) stripeObject;

                    String customerEmail = session.getCustomerDetails() != null ?
                            session.getCustomerDetails().getEmail() : null;
                    Long amountTotal = session.getAmountTotal(); // Amount in cents

                    System.out.println("SUCCESSFUL PAYMENT for session ID: " + session.getId());
                    System.out.println("Customer Email: " + customerEmail);

                    if (customerEmail != null) {
                        // Find user by email in your database
                        Optional<Users> optionalUser = userRepository.findByEmail(customerEmail);

                        if (optionalUser.isPresent()) {
                            Users user = optionalUser.get();

                            // Calculate credits based on amountTotal (e.g., 100 cents = 10 credits)
                            int creditsToAdd = calculateCreditsForAmount(amountTotal);

                            // Fetch existing credit record or create a new one using user.getId()
                            Credits userCredits = creditsRepository.findById(user.getId())
                                    .orElseGet(() -> {
                                        Credits newCredits = new Credits();
                                        newCredits.setUser(user);
                                        newCredits.setCredits(0); // Starts at 0 if no record exists yet
                                        return newCredits;
                                    });

                            // Update credits balance and save to database
                            userCredits.setCredits(userCredits.getCredits() + creditsToAdd);
                            creditsRepository.save(userCredits);

                            System.out.println("✅ Successfully added " + creditsToAdd + " credits to user: " + customerEmail + ". New balance: " + userCredits.getCredits());

                        } else {
                            System.out.println("⚠️ User not found in database for email: " + customerEmail);
                        }
                    }
                }
                break;

            case "payment_intent.payment_failed":
                System.out.println("Payment failed.");
                break;

            default:
                System.out.println("Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Webhook handled successfully");
    }

    private int calculateCreditsForAmount(Long amountInCents) {
        // Simple conversion rule: e.g., 100 cents ($1.00) = 10 credits
        if (amountInCents == null) return 0;
        return (int) (amountInCents / 10);
    }
}