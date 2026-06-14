package Requestes;

import java.util.HashMap;
import java.util.Map;

public class RefundRequest {
    private String amount;
    private String charge;
    private String reason;


    public RefundRequest(String amount, String charge, String reason) {
        this.amount = amount;
        this.charge = charge;
        this.reason = reason;
    }
    public String getAmount() {
        return amount;
    }
    public String getCharge() {
        return charge;
    }
    public String getReason() {
        return reason;
    }
    public Map<String, String> create() {
        Map<String, String> refund = new HashMap<>();
        if(amount != null) refund.put("amount", amount);
        if(charge != null) refund.put("charge", charge);
        if(reason != null) refund.put("reason", reason);
        return  refund;
    }


}
