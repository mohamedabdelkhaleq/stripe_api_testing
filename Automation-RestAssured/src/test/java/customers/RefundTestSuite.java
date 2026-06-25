package customers;

import base.BaseTest;
import builders.PaymentIntentRequestBuilder;
import builders.RefundCreateParams;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static constants.StripeConstants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import io.qameta.allure.*;

@Epic("Stripe API")
@Feature("Refunds")
@Owner("Mohamed Abdelkhalek")
public class RefundTestSuite extends BaseTest {
    String decline_chargeId;
    String chargeId ;
    @BeforeSuite
    public void beforeSuite() {
        chargeId = createAndExtract(PAYMENT_INTENTS,
                PaymentIntentRequestBuilder.Builder()
                        .amount("5500")
                        .currency("usd")
                        .payment_Method(PAYMENT_METHOD_MASTERCARD)
                        .returnUrl("https://example.com")
                        .confirm(true)
                        .build().create(),"latest_charge");
    }
    @Test(description = "TC-RF-001: Create refund with valid charge id", priority = 1)
    @Story("Create Refund")
    @Severity(SeverityLevel.CRITICAL)
    public void createRefundWithValidChargeId() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("2000")
                        .charge(chargeId)
                        .build().create()
        );


        assertThat(response.statusCode(), is(200));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_SUCCEEDED);
        soft.assertThat(response.jsonPath().getString("id")).startsWith("re_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("refund");
        soft.assertAll();

    }
    @Test(description = "TC-RF-002: Create refund with partial amount", priority = 2)
    @Story("Create Refund")
    @Severity(SeverityLevel.NORMAL)
    public void createRefundWithPartialAmount() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("2500")
                        .charge(chargeId)
                        .build().create()
        );


        assertThat(response.statusCode(), is(200));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_SUCCEEDED);
        soft.assertAll();
    }
    @Test(description = "TC-RF-003: Create refund with reason is duplicate")
    @Story("Create Refund")
    @Severity(SeverityLevel.NORMAL)
    public void createRefundWithDuplicateReason() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("1000")
                        .charge(chargeId)
                        .reason("duplicate")
                        .build().create()
        );


        assertThat(response.statusCode(), is(200));
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_SUCCEEDED);
        soft.assertThat(response.jsonPath().getString("reason")).isEqualTo("duplicate");
        soft.assertAll();
    }
    @Test(description = "TC-RF-004: Create refund with amount greater than charge")
    @Story("Negative Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    public void createRefundWithNegativeAmount() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("6000")
                        .charge(chargeId)
                        .build().create()
        );


        assertThat(response.statusCode(), is(400));

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("error.message")).contains("is greater than charge");
        soft.assertAll();
    }
    @Test(description = "TC-RF-005: Create refund with invalid charge id")
    @Story("Negative Scenarios")
    @Severity(SeverityLevel.NORMAL)
    public void createRefundWithInvalidChargeId() {
        Response response = createAndExtractResponse(REFUNDS,
                RefundCreateParams.builder()
                        .amount("1000")
                        .charge("ch_invalid")
                        .build().create()
        );

        assertThat(response.statusCode(), is(404));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("error.message")).contains("No such charge");
        soft.assertAll();
    }
    @Test(description = "TC-RF-006: Retrieve list of refunds")
    @Story("Retrieve Refunds")
    @Severity(SeverityLevel.MINOR)
    public void retrieveRefunds() {
        Response response = getResponse(REFUNDS);


        assertThat(response.statusCode(), is(200));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("data.status[0]")).isEqualTo(STATUS_SUCCEEDED);
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("list");
        soft.assertAll();
    }
}
