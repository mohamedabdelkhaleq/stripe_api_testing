package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import builders.PaymentMethodBuilder;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import static constants.StripeConstants.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import io.qameta.allure.*;

@Epic("Stripe API Automation")
@Feature("Payment Methods")
@Owner("Mohamed Abdelkhalek")
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
    }
    @Test(description = "TC-PM-001: Verify Create Payment Method with visa brand token")
    @Story("Create Payment Method")
    @Severity(SeverityLevel.CRITICAL)
    public void createPaymentMethod() {
        Response response = createAndExtractResponse(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_SUCCESS)
                        .build().create());
        paymentMethodId = response.jsonPath().getString("id");

        Assert.assertEquals(response.statusCode(),200);
        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id")).startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_method");
        soft.assertAll();

    }
    @Test(
            description = "TC-PM-002: Attach payment method to a valid customer",
            dependsOnMethods = "createPaymentMethod"
    )
    @Story("Attach Payment Method")
    @Severity(SeverityLevel.CRITICAL)
    public void attachPaymentMethodWithValidCustomer() {
        Response response = postActionOnId(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .customer(customerId)
                        .build().create(),paymentMethodId,"attach");

        Assert.assertEquals(response.statusCode(),200);
        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id")).startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_method");
        soft.assertThat(response.jsonPath().getString("customer")).isEqualTo(customerId);
        soft.assertAll();
    }
    @Test(
            description = "TC-PM-003: Attach valid payment method to a non-existent customer",
            dependsOnMethods = "createPaymentMethod"
    )
    @Story("Negative Scenarios")
    @Severity(SeverityLevel.NORMAL)
    public void attachPaymentMethodWithInvalidCustomer() {
            Response response = postActionOnId(PAYMENT_METHODS,
                    PaymentMethodBuilder.builder()
                            .customer("Invalid_customerId")
                            .build().create(),paymentMethodId,"attach");

            Assert.assertEquals(response.statusCode(),400);
            SoftAssertions soft = new SoftAssertions();
            soft.assertThat(response.jsonPath().getString("error.message")).startsWith("No such customer");
        soft.assertAll();


    }
    @Test(
            description = "TC-PM-004: Create Declined Payment Method"
    )
    @Story("Create Payment Method")
    @Severity(SeverityLevel.NORMAL)
    public void createDeclinedPaymentMethod() {
        Response response = createAndExtractResponse(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_DECLINED)
                        .build().create());
        paymentMethodId = response.jsonPath().getString("id");
        Assert.assertEquals(response.statusCode(),200);
        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id")).startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_method");
        soft.assertAll();

    }
    @Test(
            description = "TC-PM-005: Attach Declined PaymentMethod to Valid Customer",
            dependsOnMethods = "createDeclinedPaymentMethod"
    )
    @Story("Negative Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    public void attachDeclinePaymentMethod() {
        Response response = postActionOnId(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .customer(customerId)
                        .build().create(),paymentMethodId,"attach");

        Assert.assertEquals(response.statusCode(),402);
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("error.code")).isEqualTo("card_declined");
        soft.assertAll();
    }
    @Test(
            description = "TC-PM-006: Create Payment Method With 3D Secure"
    )
    @Story("Create 3d Secure Payment Method")
    @Severity(SeverityLevel.NORMAL)
    public void createPaymentMethodWith3DSecure() {
        Response response = createAndExtractResponse(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .type("card")
                        .cardToken(CARD_3DS)
                        .build().create());


        paymentMethodId = response.jsonPath().getString("id");
        Assert.assertEquals(response.statusCode(), 200);
        //SoftAssertions
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id")).startsWith("pm_");
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("payment_method");
        soft.assertAll();
    }
        @Test(
                description = "TC-PM-007: Attach 3D Secure PaymentMethod to Valid Customer",
                dependsOnMethods = "createPaymentMethodWith3DSecure"
        )
        @Story("Authentication Flows")
        @Severity(SeverityLevel.CRITICAL)
    public void attach3DSecurePaymentMethod() {
        Response response = postActionOnId(PAYMENT_METHODS,
                PaymentMethodBuilder.builder()
                        .customer(customerId)
                        .build().create(),paymentMethodId,"attach");

        Assert.assertEquals(response.statusCode(),200);
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("customer")).isEqualTo(customerId);
            soft.assertAll();
    }

}
