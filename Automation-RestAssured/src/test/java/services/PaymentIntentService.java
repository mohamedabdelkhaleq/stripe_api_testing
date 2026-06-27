package services;

import Requestes.ChargesRequest;
import base.BaseTest;
import builders.PaymentIntentRequestBuilder;
import io.restassured.response.Response;
import logs.LogsManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import static constants.StripeConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PaymentIntentService extends BaseTest {
    public String create(String customerId,String amount) {
        LogsManager.info("Creating Payment Intent");
        Response response =
                createAndExtractResponse(
                        PAYMENT_INTENTS,
                        PaymentIntentRequestBuilder.Builder()
                                .amount(amount)
                                .currency(USD)
                                .customer(customerId)
                                .build()
                                .create());

        //HardAssertions
        Assert.assertEquals(response.statusCode(), 200);
        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_REQUIRES_PAYMENT_METHOD);
        soft.assertThat(response.jsonPath().getString("id")).startsWith("pi_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_intent");
        soft.assertThat(response.jsonPath().getString("currency")).isEqualTo(USD);
        soft.assertAll();
        LogsManager.info("Payment Intent Created with ID: " + response.jsonPath().getString("id"));
        return response.jsonPath().getString("id");
    }

    public String confirm(String paymentIntentId,String paymentMethodId,String status) {
        Response response =
                postActionOnId(
                        PAYMENT_INTENTS,
                        PaymentIntentRequestBuilder.Builder()
                                .payment_Method(paymentMethodId)
                                .returnUrl("https://www.example.com")
                                .build()
                                .create(),
                        paymentIntentId,
                        "confirm");


        //HardAssertions
        Assert.assertEquals(response.statusCode(),200);
        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id")).startsWith("pi_");
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(status);
        soft.assertThat(response.jsonPath().getString("currency")).isEqualTo(USD);
        soft.assertThat(response.jsonPath().getString("latest_charge")).startsWith("ch_");
        soft.assertAll();

        LogsManager.info("Payment Intent Confirmed with ID: " + response.jsonPath().getString("id"));
        return  response.jsonPath().getString("latest_charge");
    }
    public void verifyCharge(String chargeID,String amount,String status) {
        Response response =
                getResponse(CHARGES, chargeID);

        ChargesRequest charge =
                response.as(ChargesRequest.class);



        assertThat(response.statusCode(), equalTo(200));

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("charge");
        soft.assertThat(charge.getId()).startsWith("ch_");
        soft.assertThat(charge.getStatus()).isEqualTo(status);
        soft.assertThat(charge.getAmount()).isEqualTo(amount);
        soft.assertAll();
    }
    public String getFailureCode(String chargeId) {
        Response response =
                getResponse(CHARGES, chargeId);

return response.jsonPath().getString("failure_code");

    }
    public String confirmInvalid(String paymentIntentId,String paymentMethodId,String status) {


        Response response =
                postActionOnId(
                        PAYMENT_INTENTS,
                        PaymentIntentRequestBuilder.Builder()
                                .payment_Method(paymentMethodId)
                                .returnUrl("https://www.example.com")
                                .build()
                                .create(),
                        paymentIntentId,
                        "confirm");


        //HardAssertions
        Assert.assertEquals(response.statusCode(),402);
        return  response.jsonPath().getString("error.payment_intent.latest_charge");

    }
    }
