package services;

import base.BaseTest;
import builders.PaymentMethodBuilder;
import io.restassured.response.Response;
import logs.LogsManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import static constants.StripeConstants.*;

public class PaymentMethodService extends BaseTest {
    public String createValid() {
        LogsManager.info("Creating Valid Payment Method");
        Response response = createAndExtractResponse(
                PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_SUCCESS)
                        .build()
                        .create());


        Assert.assertEquals(response.statusCode(), 200);
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id"))
                .startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object"))
                .isEqualTo("payment_method");
        soft.assertAll();

        LogsManager.info("Valid Payment Method Created");
        return response.jsonPath().getString("id");
    }
    public void attach(String customerId,
                        String paymentMethodId) {
        LogsManager.info("Attaching Valid Payment Method");
        Response response =
                postActionOnId(
                        PAYMENT_METHODS,
                        PaymentMethodBuilder.builder()
                                .customer(customerId)
                                .build()
                                .create(),
                        paymentMethodId,
                        "attach");

        Assert.assertEquals(response.statusCode(), 200);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("id")).startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_method");
        soft.assertThat(response.jsonPath().getString("customer")).isEqualTo(customerId);
        soft.assertAll();
        LogsManager.info("Attached Payment Method");

    }
    public String createInvalid() {
        LogsManager.info("Creating Invalid Payment Method");
        Response response = createAndExtractResponse(
                PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_DECLINED)
                        .build()
                        .create());


        Assert.assertEquals(response.statusCode(), 200);
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id"))
                .startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object"))
                .isEqualTo("payment_method");
        soft.assertAll();


        LogsManager.info("Invalid Payment Method Created");
        return response.jsonPath().getString("id");
    }
}
