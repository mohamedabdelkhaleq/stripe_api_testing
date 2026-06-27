package services;

import base.BaseTest;
import builders.SubscriptionRequestBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import static constants.StripeConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;

public class SubscriptionService extends BaseTest {
public String create(String customerId,String monthlyPriceId,String paymentMethodId) {
    Response res =setupRequest()
            .formParams(SubscriptionRequestBuilder.builder()
                    .customer(customerId)
                    .price(monthlyPriceId)
                    .defaultPaymentMethod(paymentMethodId)
                    .build())
            .when().post(SUBSCRIPTIONS)
            .then()
            .statusCode(200)
            .time(lessThan(MAX_RESPONSE_TIME_MS))
            .extract().response();



    SoftAssertions soft = new SoftAssertions();
    soft.assertThat(res.path("id").toString()).startsWith("sub_");
    soft.assertThat(res.path("object").toString()).isEqualTo("subscription");
    soft.assertThat(res.path("status").toString()).isEqualTo(STATUS_ACTIVE);
    soft.assertThat(res.path("customer").toString()).isEqualTo(customerId);
    soft.assertAll();
    return res.path("id").toString();
}
    public String getStatus(String subscriptionId) {
        Response response =
                getResponse(SUBSCRIPTIONS, subscriptionId);

        return response.jsonPath().getString("status");
    }

    public String getPriceId(String subscriptionId) {
        Response response =
                getResponse(SUBSCRIPTIONS, subscriptionId);

        return response.jsonPath()
                .getString("items.data[0].price.id");
    }

    public String getSubscriptionItemId(String subscriptionId) {
        Response response =
                getResponse(SUBSCRIPTIONS, subscriptionId);

        return response.jsonPath()
                .getString("items.data[0].id");
    }

    public void update(String subscriptionId,
                       String newPriceId) {

        String itemId =
                getSubscriptionItemId(subscriptionId);

        Response response =
                PostForId(SUBSCRIPTIONS,
                        SubscriptionRequestBuilder.builder()
                                .updateItem(itemId, newPriceId).build(),subscriptionId);

        response.then().statusCode(200);
    }

    public void cancel(String subscriptionId) {
        Response response =
                deleteById(SUBSCRIPTIONS,
                        subscriptionId);

        response.then().statusCode(200);
    }

    public String getCanceledAt(String subscriptionId) {
        Response response =
                getResponse(SUBSCRIPTIONS,
                        subscriptionId);

        return response.jsonPath()
                .getString("canceled_at");
    }
}