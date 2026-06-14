package customers;

import base.BaseTest;
import builders.PaymentIntentRequestBuilder;
import builders.RefundCreateParams;
import io.restassured.response.Response;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static constants.StripeConstants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class RefundTestSuite extends BaseTest {
    String decline_chargeId;
    String chargeId ;
    @BeforeSuite
    public void beforeSuite() {
        chargeId = createAndExtract(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5000")
                        .currency("usd")
                        .payment_Method(PAYMENT_METHOD_MASTERCARD)
                        .returnUrl("https://example.com")
                        .confirm(true)
                        .build().create(),"latest_charge");

    }
    @Test(description = "TC-RF-001: Create refund with valid charge id")
    public void createRefundWithValidChargeId() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("2000")
                        .charge(chargeId)
                        .build().create()
        );


        assertThat(response.statusCode(), is(200));
        assertThat(response.path("status"), equalTo(STATUS_SUCCEEDED));
        assertThat(response.path("id"), startsWith("re_"));
        assertThat(response.path("object"), equalTo("refund"));

    }
    @Test(description = "TC-RF-002: Create refund with partial amount")
    public void createRefundWithPartialAmount() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("2500")
                        .charge(chargeId)
                        .build().create()
        );


        assertThat(response.statusCode(), is(200));
        assertThat(response.path("status"), equalTo(STATUS_SUCCEEDED));
    }
    @Test(description = "TC-RF-003: Create refund with reason is duplicate")
    public void createRefundWithDuplicateReason() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("1000")
                        .charge(chargeId)
                        .reason("duplicate")
                        .build().create()
        );


        assertThat(response.statusCode(), is(200));
        assertThat(response.path("status"), equalTo(STATUS_SUCCEEDED));
        assertThat(response.path("reason"), equalTo("duplicate"));
    }
    @Test(description = "TC-RF-004: Create refund with amount greater than charge")
    public void createRefundWithNegativeAmount() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("6000")
                        .charge(chargeId)
                        .build().create()
        );



        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error.message"),containsString("is greater than charge"));
    }
    @Test(description = "TC-RF-005: Create refund with invalid charge id")
    public void createRefundWithInvalidChargeId() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("1000")
                        .charge("ch_invalid")
                        .build().create()
        );


        assertThat(response.statusCode(), is(404));
        assertThat(response.path("error.message"),containsString("No such charge"));
    }
    @Test(description = "TC-RF-006: Retrieve List refunds")
    public void retrieveRefunds() {
        Response response = getResponse(REFUNDS);


        assertThat(response.statusCode(), is(200));
        assertThat(response.path("data.status[0]"), equalTo(STATUS_SUCCEEDED));
        assertThat(response.path("object"), equalTo("list"));
    }
}
