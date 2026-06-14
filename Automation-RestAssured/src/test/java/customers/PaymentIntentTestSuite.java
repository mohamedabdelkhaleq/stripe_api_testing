package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import builders.PaymentIntentRequestBuilder;
import builders.PaymentMethodBuilder;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static constants.StripeConstants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PaymentIntentTestSuite extends BaseTest {
    String customerId;
    String paymentMethodId;
    String paymentIntentId;
    @BeforeClass
    public void createBaseTest() {
        customerId = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("test_" + System.currentTimeMillis() + "@example.com")
                        .name("Test Customer")
                        .build(), "id");
        paymentMethodId =createAndExtract(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken("tok_visa")
                        .build().create(), "id");
        paymentIntentId = createAndExtract(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5000")
                        .currency("usd")
                        .build().create(),"id");
        createdCustomerIds.add(customerId);
        createdPaymentMethodsIds.add(paymentMethodId);
    }
    @Test(description = "TC-PI-001: Create payment intent with valid amount and currency")
    public void CreatePaymentIntentWithValidAmountandCurrency()
    {
        Response response = createAndExtractResponse(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5000")
                        .currency("usd")
                        .build().create()
        );
        Assert.assertEquals(response.statusCode(), 200);
        assertThat(response.jsonPath().getString("id"), startsWith("pi_"));
        Assert.assertEquals(response.jsonPath().getString("amount"),String.valueOf(5000));
        Assert.assertEquals(response.jsonPath().getString("currency"), USD);
        Assert.assertEquals(response.jsonPath().getString("status"),STATUS_REQUIRES_PAYMENT_METHOD);


    }
    @Test(description = "TC-PI-002:Create payment intent with minimum amount ($0.50)")
    public void CreatePaymentIntentWithMinimumamount()
    {
        Response response = createAndExtractResponse(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("50")
                        .currency("usd")
                        .build().create()
        );



        Assert.assertEquals(response.statusCode(), 200);
        assertThat(response.jsonPath().getString("id"), startsWith("pi_"));
        assertThat(response.jsonPath().getString("object"),startsWith("payment_intent"));
    }
    @Test(description = "TC-PI-003: Confirm payment intent with test card (success)")
    public void ConfirmPaymentIntentWithValidTestcard()
    {
        Response response = postActionOnId(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .payment_Method(paymentMethodId)
                        .returnUrl("https://www.example.com")
                        .build().create(),paymentIntentId,"confirm"
        );


        Assert.assertEquals(response.statusCode(), 200);
        assertThat(response.jsonPath().getString("status"), startsWith(STATUS_SUCCEEDED));
        assertThat(response.jsonPath().getString("latest_charge"),startsWith("ch_"));
    }
    @Test(description = "TC-PI-004: Confirm payment intent with Declined Card")
    public void ConfirmPaymentIntentWithDeclinedcard()
    {
        Response response = postActionOnId(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .payment_Method(PAYMENT_METHOD_DECLINED)
                        .returnUrl("https://www.example.com")
                        .build().create(),paymentIntentId,"confirm"
        );


        Assert.assertEquals(response.statusCode(), 402);
        assertThat(response.jsonPath().getString("error.code"), equalTo("card_declined"));
    }
    @Test(description ="TC-PI-005:Confirm Paymentintent with PaymentMethod 3DSecure")
    public void  ConfirmPaymentIntentWithPaymentMethod3DSecure()
    {
        Response response = postActionOnId(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .payment_Method(PAYMENT_METHOD_3DS)
                        .returnUrl("https://www.example.com")
                        .build().create(),paymentIntentId,"confirm"
        );


        Assert.assertEquals(response.statusCode(), 200);
        assertThat(response.jsonPath().getString("status"), equalTo(STATUS_REQUIERS_ACTIONS));
    }

    @Test(description = "TC-PI-006: Cancel payment intent")
    public void CancelPaymentIntent()
    {
        Response response = postActionOnId(PAYMENT_INTENTS,paymentIntentId,"cancel");



        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("status"), equalTo(STATUS_CANCELED));
    }
    @Test(description = "TC-PI-007: Cancel already cancelled payment intent")
    public void CancelAlreadyCancelledPaymentIntent()
    {
        postActionOnId(PAYMENT_INTENTS,paymentIntentId,"cancel");
        Response response = postActionOnId(PAYMENT_INTENTS,paymentIntentId,"cancel");


        Assert.assertEquals(response.statusCode(),400);
        assertThat(response.jsonPath().getString("error.message"),
                startsWith("You cannot cancel this PaymentIntent because it has a status of canceled"));
    }
    
}
