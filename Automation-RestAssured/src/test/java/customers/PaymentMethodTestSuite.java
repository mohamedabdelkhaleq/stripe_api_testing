package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import builders.PaymentMethodBuilder;
import io.restassured.response.Response;
import org.testng.Assert;

import static constants.StripeConstants.*;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

public class PaymentMethodTestSuite extends BaseTest {
    private String customerId;
    private String customerEmail;
    private String paymentMethodId;
    @BeforeClass
    public void createBaseCustomer() {
        customerEmail = "test_" + System.currentTimeMillis() + "@example.com";
        customerId = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email(customerEmail)
                        .name("Test Customer")
                        .build(), "id");
        paymentMethodId =createAndExtract(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken("tok_visa")
                        .build().create(), "id");
        createdCustomerIds.add(customerId);
        createdPaymentMethodsIds.add(paymentMethodId);
    }
    @Test(description = "TC-PM-001: Verify Create Payment Method with visa brand token")
    public void createPaymentMethod() {
        Response response = createAndExtractResponse(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_SUCCESS)
                        .build().create());
        createdPaymentMethodsIds.add(response.jsonPath().getString("id"));
        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("id"),startsWith("pm_"));
        assertThat(response.jsonPath().getString("object"),startsWith("payment_method"));
    }
    @Test(description = "TC-PM-002:attach that payment method to a valid customer")
    public void attachPaymentMethodWithValidCustomer() {
        Response response = postActionOnId(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .customer(createdCustomerIds.getLast())
                        .build().create(),createdPaymentMethodsIds.getLast(),"attach");

        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("id"),startsWith("pm_"));
        assertThat(response.jsonPath().getString("object"),equalTo("payment_method"));
        assertThat(response.jsonPath().getString("customer"),equalTo(createdCustomerIds.getLast()));
    }
    @Test
    public void attachPaymentMethodWithInvalidCustomer() {
            Response response = postActionOnId(PAYMENT_METHODS,
                    PaymentMethodBuilder.builder()
                            .customer(customerId)
                            .build().create(),"xyz123","attach");

            Assert.assertEquals(response.statusCode(),404);


    }
    @Test
    public void createPaymentMethodChargeDeclined() {
        Response response = createAndExtractResponse(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_DECLINED)
                        .build().create());
        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("id"),startsWith("pm_"));
        assertThat(response.jsonPath().getString("object"),startsWith("payment_method"));
    }
    @Test
    public void createPaymentMethodWith3DSecure() {
        Response response = createAndExtractResponse(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_3DS)
                        .build().create());
        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("id"),startsWith("pm_"));
        assertThat(response.jsonPath().getString("object"),startsWith("payment_method"));
    }
}
