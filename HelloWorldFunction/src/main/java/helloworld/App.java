package helloworld;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String DEFAULT_USER_NAME = "postgres";
    public static final String DEFAULT_DB_CONNECTION_STRING = "jdbc:postgresql://patients-db.c2xms40gozhk.us-east-1.rds.amazonaws.com:5432/postgres";
    Map<String, String> env = System.getenv();
    String connectionStr = getConnectionString();
    String username = getUsername();
    String password = getPassword();
    DataSource dataSource = new DataSource(connectionStr, username, password);

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, String> path = input.getQueryStringParameters();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            String patientIdStr = "";
            if (path == null || (patientIdStr = path.get("id")) == null) {
                throw new IllegalArgumentException("id parameter must exist");
            }
            long patientId = 0;
            try {
                patientId = Long.parseLong(patientIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("patient id must be number");
            }
            Range range = dataSource.getRange(patientId);
            String output = String.format("{\"min\":%d, \"max\":%d}", range.min(), range.max());
            response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (NoSuchElementException e) {
            response
                    .withBody(e.toString())
                    .withStatusCode(404);
        } catch (IllegalArgumentException e) {
            response
                    .withBody(e.toString())
                    .withStatusCode(400);
        } catch (Exception e) {
            response
                    .withBody(e.toString())
                    .withStatusCode(500);
        }
        return response;
    }

    private String getPassword() {
        String password = env.get("DB_PASSWORD");
        if (password == null) {
            throw new RuntimeException("password must be specified in environment variable");
        }
        return password;
    }

    private String getUsername() {
        String username = env.getOrDefault("USERNAME", DEFAULT_USER_NAME);
        return username;
    }

    private String getConnectionString() {
        String connectionString = env.getOrDefault("DB_CONNECTION_STRING", DEFAULT_DB_CONNECTION_STRING);
        return connectionString;
    }

}