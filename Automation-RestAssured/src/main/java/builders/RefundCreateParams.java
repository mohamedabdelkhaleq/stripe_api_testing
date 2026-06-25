package builders;

import Requestes.RefundRequest;

public class RefundCreateParams {
    private String amount;
    private String charge;
    private String reason;
    public static RefundCreateParams builder() {
        return new RefundCreateParams();
    }
    public RefundCreateParams amount(String amount) {
        this.amount = amount;
        return this;
    }
    public RefundCreateParams charge(String charge) {
        this.charge = charge;
        return this;
    }
    public RefundCreateParams reason(String reason) {
        this.reason = reason;
        return this;
    }
    public RefundRequest build() {
        return new RefundRequest(amount, charge,reason);
    }
}
