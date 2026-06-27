package services;

import base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static constants.StripeConstants.INVOICES;

public class InvoiceService extends BaseTest {
    public String getLatestInvoiceStatus(
            String customerId) {

        Response response =
                InquireById(
                        INVOICES,
                        customerId);

        response.then().statusCode(200);

        return response.jsonPath()
                .getString(
                        "data[0].status");
    }
}
