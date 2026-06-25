package dataReader;


import com.jayway.jsonpath.JsonPath;
import logs.LogsManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

public class JsonReader {
    private final String TEST_DATA_PATH = "src/test/resources/test-data/";
    static String jsonReader;
    String jsonFileName;

    public  JsonReader(String jsonFileName) {
        this.jsonFileName = jsonFileName;

        try (FileReader reader = new FileReader(TEST_DATA_PATH + jsonFileName)) {
            JSONObject data = (JSONObject) new JSONParser().parse(reader);
            jsonReader = data.toJSONString();
        } catch (Exception e) {
            LogsManager.error("Error reading json file: ", jsonFileName, e.getMessage());
            jsonReader = "{}";
        }
    }

    public  static String getJsonData(String jsonPath) {
        try {
            // FIX 2: Safely handle non-String JSON returns (like integers or booleans)
            Object result = JsonPath.read(jsonReader, jsonPath);
            return result != null ? String.valueOf(result) : "";
        } catch (Exception e) {
            LogsManager.error("Error reading json file: ", jsonPath, e.getMessage());
            return "";
        }
    }

}