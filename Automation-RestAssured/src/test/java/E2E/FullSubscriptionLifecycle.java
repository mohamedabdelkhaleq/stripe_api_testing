package E2E;


import base.BaseTest;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.*;

@Epic("Stripe API Automation")
@Feature("End-to-End Flows")
@Story("Full Subscription Lifecycle")
public class FullSubscriptionLifecycle extends BaseTest {
    ProductAndPriceService productService =
            new ProductAndPriceService();

    CustomerService customerService =
            new CustomerService();

    PaymentMethodService paymentMethodService =
            new PaymentMethodService();
    SubscriptionService  subscriptionService =
            new SubscriptionService();

    InvoiceService  invoiceService =
            new InvoiceService();
    @Test
    public void subscriptionLifecycleTest(){
        // Step 1: Create customer
        String customerId =
                customerService.createCustomer();


        String productId = productService.createProduct("E2E subscriptionLifecycle Test");


        // Step 3: Create monthly price
        String monthlyPriceID = productService.createPrice(productId,"month");


        String paymentMethodId =
                paymentMethodService
                        .createValid();

        paymentMethodService.attach(
                customerId,
                paymentMethodId);

        //Step 5: Create subscription
        String subscriptionId =
                subscriptionService.create(
                        customerId,
                        monthlyPriceID,
                        paymentMethodId);

        String subscriptionStatus =
                subscriptionService.getStatus(
                        subscriptionId);

        Assert.assertEquals(
                subscriptionStatus,
                "active");

        // Step 6: Verify invoice
        String invoiceStatus =
                invoiceService.getLatestInvoiceStatus(
                        customerId);

        Assert.assertEquals(
                invoiceStatus,
                "paid");

        // Step 7: Update subscription with new price
        String yearlyPriceId =
                productService.createPrice(
                        productId,
                        "year");

        subscriptionService.update(
                subscriptionId,
                yearlyPriceId);

        String updatedPriceId =
                subscriptionService.getPriceId(
                        subscriptionId);

        Assert.assertEquals(
                updatedPriceId,
                yearlyPriceId);

        // Step 8: Cancel subscription
        subscriptionService.cancel(
                subscriptionId);

        String statusAfterCancel =
                subscriptionService.getStatus(
                        subscriptionId);

        Assert.assertEquals(
                statusAfterCancel,
                "canceled");

        // Step 9: Verify canceled_at
        String canceledAt =
                subscriptionService.getCanceledAt(
                        subscriptionId);

        Assert.assertNotNull(canceledAt);

    }

}
