package customers;

import Requestes.ChargesRequest;
import base.BaseTest;

import builders.CustomerRequestBuilder;
import builders.PaymentIntentRequestBuilder;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static constants.StripeConstants.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChargesTests extends BaseTest {
    String customerId;
    String decline_chargeId;
    String chargeId;
    @BeforeClass
    public void createBaseTest() {
        chargeId = createAndExtract(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5000")
                        .currency("usd")
                        .payment_Method(PAYMENT_METHOD_MASTERCARD)
                        .returnUrl("https://example.com")
                        .confirm(true)
                        .build().create(),"latest_charge");
        decline_chargeId = createAndExtract(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5000")
                        .currency("usd")
                        .payment_Method(PAYMENT_METHOD_DECLINED)
                        .returnUrl("https://example.com")
                        .confirm(true)
                        .build().create(),"error.charge");

        customerId = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("test_" + System.currentTimeMillis() + "@example.com")
                        .name("Test Customer")
                        .build(), "id");

    }
    @Test(description = "TC-CH-001: Get charge with valid payment intent")
    public void getChargeWithValidPaymentIntent()
    {
        Response response = getResponse(CHARGES, chargeId);
        ChargesRequest chargesRequest = response.body().as(ChargesRequest.class);




        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path("object"),equalTo("charge"));
        assertThat(chargesRequest.getId(), startsWith("ch_"));
        assertThat(chargesRequest.getStatus(), equalTo(STATUS_SUCCEEDED));
        assertThat(chargesRequest.getAmount(), equalTo("5000"));
        assertThat(chargesRequest.isPaid(), equalTo(true));
    }
    @Test(description = "TC-CH-002: Get charge with invalid payment intent")
    public void getChargesWithInvalidPaymentIntent()
    {
        Response response = getResponse(CHARGES,decline_chargeId);
        ChargesRequest chargesRequest = response.body().as(ChargesRequest.class);


        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path("object"),equalTo("charge"));
        assertThat(response.path("failure_code"),equalTo("card_declined"));
        assertThat(chargesRequest.getStatus(), equalTo(STATUS_FAILED));


    }
    @Test(description = "TC-CH-003: Get charge with non-existent charge ID")
    public void getChargeWithNonExistentChargeId()
        {
        Response response = getResponse(CHARGES, chargeId);
        }
    @Test(description = "TC-CH-004: Get charge for Spacific Customer")
    public void getChargeForSpacificCustomer()
    {
        Response response = InquireById(CHARGES,customerId);

        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path("object"),equalTo("list"));


    }


}
