import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Looks up geographic coordinates (latitude, longitude) for a city name
 * using the Nominatim OpenStreetMap geocoding API.
 */
public class GeocodingService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "SunDirectionApp/1.0";

    /**
     * Search for coordinates of a city.
     *
     * @param city City name (in any language)
     * @return double[]{latitude, longitude}, or null if not found
     * @throws Exception on network or parsing errors
     */
    public static double[] search(String city) throws Exception {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = BASE_URL + "?q=" + encodedCity + "&format=json&limit=1";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                // Disable gzip: Java 11 HttpClient does not auto-decompress responses,
                // so requesting plain text avoids receiving garbled compressed bytes.
                .header("Accept-Encoding", "identity")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status != 200) {
            throw new RuntimeException("Geocoding API returned HTTP " + status
                    + ". Check your internet connection.");
        }
        String body = response.body().trim();

        // Empty array means no results
        if (body.equals("[]") || body.isEmpty()) {
            return null;
        }

        // Nominatim returns lat/lon as quoted strings: "lat":"37.5666"
        double lat = parseQuotedDouble(body, "lat");
        double lon = parseQuotedDouble(body, "lon");

        return new double[]{lat, lon};
    }

    /**
     * Extracts a double from JSON where the value is a quoted string.
     * e.g.  ... "lat":"37.5666" ...
     */
    private static double parseQuotedDouble(String json, String key) {
        String prefix = "\"" + key + "\":\"";
        int start = json.indexOf(prefix);
        if (start == -1) {
            throw new RuntimeException("Key not found in response: " + key);
        }
        start += prefix.length();
        int end = json.indexOf('"', start);
        if (end == -1) {
            throw new RuntimeException("Malformed JSON value for key: " + key);
        }
        return Double.parseDouble(json.substring(start, end).trim());
    }
}
