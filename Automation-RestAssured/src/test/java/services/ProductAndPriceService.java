package services;

import base.BaseTest;
import builders.PriceRequestBuilder;
import builders.ProductBuilder;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import static constants.StripeConstants.*;


public class ProductAndPriceService extends BaseTest {
    public String  createProduct(String productName)
    {
        Response response = createAndExtractResponse(PRODUCTS,
                ProductBuilder.builder()
                        .name("E2E Product")
                        .build().toFormParams());


        Assert.assertEquals(response.statusCode(), 200);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.jsonPath().getString("id")).startsWith("prod_");
        soft.assertThat(response.jsonPath().getString("name")).isEqualTo("E2E Product");
        soft.assertAll();

        return response.jsonPath().getString("id");
    }
    public void createPrice()
    {

        Response response = createAndExtractResponse(PRICES,
                PriceRequestBuilder.builder()
                        .currency("usd")
                        .unitAmount(1000)
                        .product_data("E2E Product")
                        .build());



        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.statusCode()).isEqualTo(200);
        soft.assertThat(response.jsonPath().getString("id")).startsWith("price_");
        soft.assertAll();
    }
}
