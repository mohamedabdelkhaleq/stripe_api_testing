package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import builders.PaymentIntentRequestBuilder;
import builders.PaymentMethodBuilder;
import constants.StripeConstants;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
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
    }
    @Test(description = "TC-PI-001: Create payment intent with valid amount and currency",priority = 1)
    public void CreateValidPaymentIntent()
    {
        Response response = createAndExtractResponse(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5000")
                        .currency("usd")
                        .build().create()
        );
        paymentIntentId = response.jsonPath().getString("id");

        Assert.assertEquals(response.statusCode(), 200);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("id")).startsWith("pi_");
        soft.assertThat(response.jsonPath().getString("amount")).isEqualTo("5000");
        soft.assertThat(response.jsonPath().getString("currency")).isEqualTo(USD);
        Assert.assertEquals(response.jsonPath().getString("status"),STATUS_REQUIRES_PAYMENT_METHOD);
        soft.assertAll();

    }
    @Test(description = "TC-PI-002:Create payment intent with minimum amount ($0.50)",priority = 3)
    public void CreatePaymentIntentWithMinimumamount()
    {
        Response response = createAndExtractResponse(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("50")
                        .currency("usd")
                        .build().create()
        );

        paymentIntentId =  response.jsonPath().getString("id");
        Assert.assertEquals(response.statusCode(), 200);
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("id")).startsWith("pi_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_intent");
        soft.assertThat(response.jsonPath().getString("currency")).isEqualTo(USD);
        soft.assertAll();
    }
    @Test(description = "TC-PI-003: Confirm payment intent with test card (success)",
            dependsOnMethods ="CreateValidPaymentIntent",priority = 2)
    public void ConfirmPaymentIntentWithValidTestcard()
    {
        Response response = postActionOnId(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .payment_Method(paymentMethodId)
                        .returnUrl("https://www.example.com")
                        .build().create(),paymentIntentId,"confirm"
        );

        Assert.assertEquals(response.statusCode(), 200);
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("id")).startsWith("pi_");
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_SUCCEEDED);
        soft.assertThat(response.jsonPath().getString("currency")).isEqualTo(USD);
        soft.assertThat(response.jsonPath().getString("latest_charge")).startsWith("ch_");
        soft.assertAll();
    }
    @Test(description = "TC-PI-004: Confirm payment intent with Declined Card",priority = 4)
    public void ConfirmPaymentIntentWithDeclinedcard()
    {
        Response response = createAndExtractResponse(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("65400")
                        .currency(EUR)
                        .payment_Method(PAYMENT_METHOD_DECLINED)
                        .returnUrl("https://www.example.com")
                        .confirm(true)
                        .build().create()
        );

        Assert.assertEquals(response.statusCode(), 402);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("error.code")).isEqualTo(STATUS_CARD_DECLINE);
        soft.assertAll();
    }
    @Test(description ="TC-PI-005:Confirm Paymentintent with PaymentMethod 3DSecure",
            dependsOnMethods ="CreateValidPaymentIntent",priority = 5)
    public void  ConfirmPaymentIntentWithPaymentMethod3DSecure()
    {
        Response response = createAndExtractResponse(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("6500")
                        .currency(EUR)
                        .payment_Method(PAYMENT_METHOD_3DS)
                        .confirm(true)
                        .returnUrl("https://www.example.com")
                        .build().create()
        );

        Assert.assertEquals(response.statusCode(), 200);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_REQUIERS_ACTIONS);
        soft.assertAll();
    }

    @Test(description = "TC-PI-006: Cancel payment intent",
    dependsOnMethods = "CreatePaymentIntentWithMinimumamount",priority = 6)
    public void CancelPaymentIntent()
    {
        Response response = postActionOnId(PAYMENT_INTENTS,paymentIntentId,"cancel");

        Assert.assertEquals(response.statusCode(), 200);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_CANCELED);
        soft.assertAll();

    }
    @Test(description = "TC-PI-007: Cancel already cancelled payment intent",
    dependsOnMethods = {
            "CreatePaymentIntentWithMinimumamount"
    },priority = 7)
    public void CancelAlreadyCancelledPaymentIntent()
    {
        postActionOnId(PAYMENT_INTENTS,paymentIntentId,"cancel");
        Response response = postActionOnId(PAYMENT_INTENTS,paymentIntentId,"cancel");

        Assert.assertEquals(response.statusCode(), 400);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("error.message"))
                        .contains("You cannot cancel this PaymentIntent because it has a status of canceled");
        soft.assertAll();
    }

}
