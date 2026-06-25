package customers;

import base.BaseTest;
import builders.CustomerRequestBuilder;
import builders.PaymentIntentRequestBuilder;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static constants.StripeConstants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.equalTo;

public class InvoiceTestSuite extends BaseTest {
    String customerId;
    @BeforeClass
    public void createBaseTest(){
        customerId = createAndExtract(CUSTOMERS,
                CustomerRequestBuilder.builder()
                        .email("test_" + System.currentTimeMillis() + "@example.com")
                        .name("Test Customer")
                        .build(), "id");
    }
    @Test(description = "TC-INV-001: List all invoices")
    public void listAllInvoices(){
        Response response = getResponse(INVOICES);

        assertThat(response.statusCode(),equalTo(200));

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("list");
        soft.assertAll();
    }
    @Test(description = "TC-INV-002: List invoices for specific customer")
    public void listInvoicesForCustomer(){
        Response response = InquireById(INVOICES,customerId);
        assertThat(response.statusCode(),equalTo(200));
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("object")).isEqualTo("list");
        soft.assertAll();
    }
}