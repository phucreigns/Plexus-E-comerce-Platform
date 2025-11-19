package com.phuc.payment.controller;

import com.phuc.event.dto.StripeChargeRequest;
import com.phuc.payment.dto.ApiResponse;
import com.phuc.payment.dto.request.*;
import com.phuc.payment.dto.response.SessionResponse;
import com.phuc.payment.dto.response.StripeChargeResponse;
import com.phuc.payment.dto.response.StripeSubscriptionResponse;
import com.phuc.payment.httpclient.OrderClient;
import com.phuc.payment.service.StripeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StripeController {

      final StripeService stripeService;
      final OrderClient orderClient;
      final ObjectMapper objectMapper;

      @Value("${stripe.webhook.secret:}")
      String stripeWebhookSecret;

      @PostMapping("/charge")
      public ApiResponse<StripeChargeResponse> charge(@RequestBody @Valid StripeChargeRequest request) {
            return ApiResponse.<StripeChargeResponse>builder()
                    .result(stripeService.charge(request))
                    .build();
      }

      @PostMapping("/customer/subscription")
      public ApiResponse<StripeSubscriptionResponse> subscription(@RequestBody @Valid StripeSubscriptionRequest request) {
            return ApiResponse.<StripeSubscriptionResponse>builder()
                    .result(stripeService.createSubscription(request))
                    .build();
      }

      @PostMapping("/session/charge")
      public ApiResponse<SessionResponse> createChargeSession(@RequestBody @Valid ChargeSessionRequest request) {
            return ApiResponse.<SessionResponse>builder()
                    .result(stripeService.createChargeSession(request))
                    .build();
      }

      @PostMapping("/session/subscription")
      public ApiResponse<SessionResponse> createSubscriptionSession(@RequestBody @Valid SubscriptionSessionRequest request) {
            return ApiResponse.<SessionResponse>builder()
                    .result(stripeService.createSubscriptionSession(request))
                    .build();
      }

      @DeleteMapping("/subscription/{id}")
      public ApiResponse<Void> cancelSubscription(@PathVariable String id) {
            stripeService.cancelSubscription(id);
            return ApiResponse.<Void>builder()
                    .message("The subscription has been successfully canceled!")
                    .build();
      }

      @GetMapping("/subscriptions")
      public ApiResponse<List<StripeSubscriptionResponse>> retrieveAllSubscriptions() {
            return ApiResponse.<List<StripeSubscriptionResponse>>builder()
                    .result(stripeService.retrieveAllSubscriptions())
                    .build();
      }

      @PostMapping("/webhook")
      public ResponseEntity<String> handleStripeWebhook(@RequestHeader("Stripe-Signature") String signatureHeader,
                                                        @RequestBody String payload) {
            log.info("Received Stripe webhook event");
            if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
                  log.warn("Stripe webhook secret is not configured");
                  return ResponseEntity.badRequest().body("Webhook not configured");
            }
            try {
                  Event event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
                  log.info("Stripe webhook event verified successfully. Event type: {}", event.getType());

                  if ("checkout.session.completed".equals(event.getType())) {
                        log.info("Processing checkout.session.completed event");
                        
                        Session session = null;
                        try {
                              EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                              if (deserializer.getObject().isPresent()) {
                                    StripeObject stripeObject = deserializer.getObject().get();
                                    log.info("Deserialized object type: {}", stripeObject.getClass().getName());
                                    
                                    if (stripeObject instanceof Session) {
                                          session = (Session) stripeObject;
                                          log.info("Successfully deserialized Session object");
                                    } else {
                                          log.warn("Object is not Session type: {}, attempting to retrieve Session by ID", stripeObject.getClass().getName());
                                          JsonNode eventData = objectMapper.readTree(payload)
                                                  .get("data")
                                                  .get("object");
                                          String sessionId = eventData.get("id").asText();
                                          session = Session.retrieve(sessionId);
                                          log.info("Successfully retrieved Session {} by ID", sessionId);
                                    }
                              } else {
                                    log.warn("Deserialized object is not present, parsing session ID from payload");
                                    JsonNode eventData = objectMapper.readTree(payload)
                                            .get("data")
                                            .get("object");
                                    String sessionId = eventData.get("id").asText();
                                    session = Session.retrieve(sessionId);
                                    log.info("Successfully retrieved Session {} by ID from payload", sessionId);
                              }
                        } catch (Exception deserializeEx) {
                              log.error("Error deserializing Session object: {}", deserializeEx.getMessage(), deserializeEx);
                              try {
                                    JsonNode eventData = objectMapper.readTree(payload)
                                            .get("data")
                                            .get("object");
                                    String sessionId = eventData.get("id").asText();
                                    session = Session.retrieve(sessionId);
                                    log.info("Successfully retrieved Session {} by ID from payload (fallback)", sessionId);
                              } catch (Exception retrieveEx) {
                                    log.error("Failed to retrieve Session from payload: {}", retrieveEx.getMessage(), retrieveEx);
                              }
                        }
                        
                        if (session != null) {
                              log.info("Session ID: {}, Metadata: {}", session.getId(), session.getMetadata());
                              String orderIdStr = session.getMetadata() != null ? session.getMetadata().get("orderId") : null;
                              if (orderIdStr != null && !orderIdStr.isBlank()) {
                                    log.info("Found orderId in metadata: {}", orderIdStr);
                                    try {
                                          Long orderId = Long.parseLong(orderIdStr);
                                          log.info("Calling order service to mark order {} as PAID", orderId);
                                          orderClient.markOrderPaid(orderId);
                                          log.info("Order {} successfully marked as PAID via webhook callback", orderId);
                                    } catch (NumberFormatException e) {
                                          log.error("Invalid orderId format in metadata: {}. Expected numeric value.", orderIdStr);
                                    } catch (Exception ex) {
                                          log.error("Failed to callback order-service for order {}: {}", orderIdStr, ex.getMessage(), ex);
                                    }
                              } else {
                                    log.warn("checkout.session.completed without orderId metadata. Session metadata: {}", session.getMetadata());
                              }
                        } else {
                              log.error("Failed to deserialize or retrieve Session object from checkout.session.completed event");
                        }
                  }
                  return ResponseEntity.ok("received");
            } catch (SignatureVerificationException e) {
                  log.error("Stripe signature verification failed: {}", e.getMessage());
                  return ResponseEntity.status(400).body("Invalid signature");
            } catch (Exception e) {
                  log.error("Error handling Stripe webhook: {}", e.getMessage(), e);
                  return ResponseEntity.status(500).body("Webhook error");
            }
      }

}