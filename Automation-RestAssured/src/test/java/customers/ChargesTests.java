package customers;

import Requestes.ChargesRequest;
import base.BaseTest;

import builders.CustomerRequestBuilder;
import builders.PaymentIntentRequestBuilder;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
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

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("charge");
        soft.assertThat(chargesRequest.getId()).startsWith("ch_");
        soft.assertThat(chargesRequest.getStatus()).isEqualTo(STATUS_SUCCEEDED);
        soft.assertThat(chargesRequest.getAmount()).isEqualTo("5000");
        soft.assertThat(chargesRequest.isPaid()).isEqualTo(true);
        soft.assertAll();
    }
    @Test(description = "TC-CH-002: Get charge with invalid payment intent")
    public void getChargesWithInvalidPaymentIntent()
    {
        Response response = getResponse(CHARGES,decline_chargeId);
        ChargesRequest chargesRequest = response.body().as(ChargesRequest.class);

        assertThat(response.statusCode(), equalTo(200));

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("charge");
        soft.assertThat(chargesRequest.getId()).startsWith("ch_");
        soft.assertThat(chargesRequest.getStatus()).isEqualTo(STATUS_FAILED);
        soft.assertThat(response.jsonPath().getString("failure_code")).isEqualTo(STATUS_CARD_DECLINE);
        soft.assertAll();
    }
    @Test(description = "TC-CH-003: Get charge with non-existent charge ID")
    public void getChargeWithNonExistentChargeId()
        {

            Response response = getResponse(CHARGES, "ivalid_charge");
            assertThat(response.statusCode(), equalTo(404));

            SoftAssertions soft = new SoftAssertions();
            soft.assertThat(response.jsonPath().getString("error.message")).contains("No such charge");
            soft.assertAll();

        }
    @Test(description = "TC-CH-004: Get charge for Specific Customer")
    public void getChargeForSpecificCustomer()
    {
        Response response = InquireById(CHARGES,customerId);

        assertThat(response.statusCode(),equalTo(200));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("list");
        soft.assertAll();
    }


}
