package base;

import config.StripeConfig;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;


public class BaseTest {

    @BeforeSuite
    public void globalSetup() {
        RestAssured.baseURI = StripeConfig.baseUrl();
        RestAssured.authentication =
                RestAssured.preemptive().basic(StripeConfig.apiKey(), "");
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // track what each test creates so we can clean up after
    protected List<String> createdCustomerIds = new ArrayList<>();
    protected List<String> createdProductIds = new ArrayList<>();
    protected List<String> createdSubscriptionIds = new ArrayList<>();

    @AfterMethod
    public void cleanup() {
        try {
            createdCustomerIds.forEach(id ->
                    given().delete(CUSTOMERS + "/" + id).then().statusCode(200));
        } finally {
            createdCustomerIds.clear();
        }

        try {
            createdSubscriptionIds.forEach(id ->
                    given().delete(SUBSCRIPTIONS + "/" + id));
        } finally {
            createdSubscriptionIds.clear();
        }

        // products can't be deleted if they have prices, so we just archive them
        try {
            createdProductIds.forEach(id ->
                    given().contentType(ContentType.URLENC)
                            .formParam("active", "false")
                            .post(PRODUCTS + "/" + id));
        } finally {
            createdProductIds.clear();
        }
    }

    protected String createAndExtract(String endpoint,
                                      Map<String, String> body,
                                      String jsonPath) {
        return given()
                .contentType(ContentType.URLENC)
                .formParams(body)
                .when()
                .post(endpoint)
                .then()
                .statusCode(200)
                .extract().path(jsonPath);
    }
}
