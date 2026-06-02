package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StripeConfig {
    private static final Properties PROPS = new Properties();
    static {
        try (InputStream in = StripeConfig.class
                .getResourceAsStream("/config.properties")) {
            if (in == null) throw new RuntimeException("config.properties not found");
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static String baseUrl()        { return PROPS.getProperty("base.url"); }
    public static String apiKey()         { return PROPS.getProperty("api.key"); }
    public static String publishableKey() { return PROPS.getProperty("publishable.key"); }
}