package services;

import base.BaseTest;
import builders.RefundCreateParams;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import static constants.StripeConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RefundService extends BaseTest {
    public void refund(String refundAmount,
                              String chargeID) {
        Response response =
                createAndExtractResponse(
                        REFUNDS,
                        RefundCreateParams.builder()
                                .amount(refundAmount)
                                .charge(chargeID)
                                .build()
                                .create());
        //HardAssertions
        assertThat(response.statusCode(), is(200));

        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("refund");
        soft.assertThat(response.jsonPath().getString("status")).isEqualTo(STATUS_SUCCEEDED);
        soft.assertThat(response.jsonPath().getString("id")).startsWith("re_");
    }
    public void verify(String refundAmount, String chargeID) {

        Response response = getResponse(CHARGES, chargeID);

        assertThat(response.statusCode(), equalTo(200));

        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(response.jsonPath().getString("charge.refunded")).isEqualTo(true);
        soft.assertThat(response.jsonPath().getString("amount_refunded")).isEqualTo(refundAmount);
        soft.assertAll();
    }

}
