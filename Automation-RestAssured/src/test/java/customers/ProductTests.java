package customers;
import base.BaseTest;
import builders.ProductBuilder;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;
import static constants.StripeConstants.PRODUCTS;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ProductTests extends BaseTest {

    @Test(description = "TC-CUS-001: Create product with valid name")
    public void createProductWithValidName() {
        Response response = createAndExtractResponse(PRODUCTS,
                ProductBuilder.builder()
                        .name("Test Product " + System.currentTimeMillis())
                        .build().toFormParams());

        createdProductIds.add(response.jsonPath().getString("id"));
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.statusCode()).isEqualTo(200);
        soft.assertThat(response.jsonPath().getString("id")).startsWith("prod_");
        soft.assertThat(response.jsonPath().getString("name")).startsWith("Test Product");
        soft.assertAll();
    }
    @Test(description = "TC-CUS-002: Create product with name and description")
    public void createProductWithValidNameAndDescription() {
        Response response = createAndExtractResponse(PRODUCTS,
                ProductBuilder.builder()
                        .name("Premium Leather Wallet " + System.currentTimeMillis())
                        .description("Handcrafted wallet made from genuine leather" + System.currentTimeMillis())
                        .build().toFormParams());

        createdProductIds.add(response.jsonPath().getString("id"));
        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("id"),startsWith("prod_"));
        assertThat(response.jsonPath().getString("object"),startsWith("product"));
    }


    @Test(description = "TC-CUS-003:Create product with metadata")
    public void createProductWithMetadata() {
      Response response = createAndExtractResponse(PRODUCTS,
              ProductBuilder.builder()
                      .name("test Product")
                      .metadata("category", "electronics")
                      .metadata("brand", "Acme")
                      .build().toFormParams());
        createdProductIds.add(response.jsonPath().getString("id"));
        Assert.assertEquals(response.statusCode(),200);
        assertThat(response.jsonPath().getString("id"),startsWith("prod_"));
        assertThat(response.jsonPath().getString("metadata.category"),is("electronics"));
        assertThat(response.jsonPath().getString("metadata.brand"),is("Acme"));

    }

    @Test(description = "TC-CUS-004:Create product without name")
    public void CreateProductWithoutName() {
        Response response = ExtractErrorResponse(PRODUCTS,
                ProductBuilder.builder()
                        .metadata("category", "electronics")
                        .metadata("brand", "Acme")
                        .build().toFormParams());
        Assert.assertEquals(response.statusCode(),400);
        Assert.assertEquals(response.jsonPath().getString("error.message"),"Missing required param: name.");
    }




}

