package services;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import io.restassured.response.Response;
import logs.LogsManager;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import static constants.StripeConstants.*;

public class CustomerService extends BaseTest {
    public String createCustomer() {
        LogsManager.info("Creating Customer");
        String email =
                "test_" + System.currentTimeMillis()
                        + "@example.com";

        Response response =
                createAndExtractResponse(
                        CUSTOMERS,
                        CustomerRequestBuilder.builder()
                                .email(email)
                                .name("Test Customer")
                                .build());


        Assert.assertEquals(response.statusCode(), 200);

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("id"))
                .startsWith("cus_");
        soft.assertThat(response.jsonPath().getString("object"))
                .isEqualTo("customer");
        soft.assertAll();
        LogsManager.info("Customer Created with ID: " + response.jsonPath().getString("id"));
        return response.jsonPath().getString("id");
    }
}


