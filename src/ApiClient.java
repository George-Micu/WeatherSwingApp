import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ApiClient
 *
 * Encapsulates all OpenWeatherMap API calls (current + 3-hour forecast),
 *  methods for parsing times
 */
public class ApiClient {

    // 1) OPENWEATHERMAP API KEY & ENDPOINT

    // OpenWeatherMap API key:
    private static final String OWM_API_KEY = "54d51884e8d02e43e90fc411d94ae066";

    /**
     * Current weather endpoint:
     * http://api.openweathermap.org/data/2.5/weather?q=<location>&units=<units>&appid=<key>
     */
    private static final String OWM_CURRENT_URL =
            "https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=" + OWM_API_KEY;

    /**
     * 3-hour forecast endpoint:
     * http://api.openweathermap.org/data/2.5/forecast?q=<location>&units=<units>&appid=<key>
     */
    private static final String OWM_FORECAST_URL =
            "https://api.openweathermap.org/data/2.5/forecast?q=%s&units=%s&appid=" + OWM_API_KEY;

    // 2) PUBLIC METHODS FOR WeatherSwingApp TO CALL

    /**
     * Fetch current-weather JSON from OpenWeatherMap.
     *
     * @param rawQuery  City name ( "Toronto") or "lat,lon".
     * @param unitParam "metric" or "imperial".
     * @return  JSONObject response.
     * @throws Exception network or JSON errors.
     */
    public static JSONObject fetchCurrentWeatherJson(String rawQuery, String unitParam) throws Exception {
        String urlString = String.format(OWM_CURRENT_URL, encode(rawQuery), unitParam);
        return getJsonFromUrl(urlString);
    }

    /**
     * Fetch 3-hour forecast JSON from OpenWeatherMap.
     *
     * @param rawQuery  City name 
     * @param unitParam "metric" or "imperial".
     * @return The full JSONObject response.
     * @throws Exception network or JSON errors.
     */
    public static JSONObject fetchForecastJson(String rawQuery, String unitParam) throws Exception {
        String urlString = String.format(OWM_FORECAST_URL, encode(rawQuery), unitParam);
        return getJsonFromUrl(urlString);
    }

    // 3)HTTP GET AND PARSE TO JSONObject

    /**
     * Perform an HTTP GET on the URL and returns the parsed JSON
     *
     *   1) Create URL in GET
     *   2) Read response code. if >299, read error; else input stream.
     *   3) Accumulate all lines
     *   4) Return new JSONObject of the accumulated string.
     *
     * @param urlString Fully formatted URL.
     * @return Parsed JSONObject of the response.
     * @throws Exception On network/JSON errors.
     */
    private static JSONObject getJsonFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        BufferedReader reader;
        int status = conn.getResponseCode();
        if (status > 299) {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return new JSONObject(sb.toString());
    }

    // 4) TIME

    /**
     * Extracts a “localtime” string from the OWM JSON.
     *
     * @param fullJson 
     * @return Local time string
     */
    public static String getLocalTime(JSONObject fullJson) {
        try {
            long dt = fullJson.getLong("dt");               
            int tzOffset = fullJson.getInt("timezone");     
            long localEpoch = dt + tzOffset;
            LocalDateTime ldt = LocalDateTime.ofEpochSecond(localEpoch, 0, java.time.ZoneOffset.UTC);
            return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }

    /**
     * Extracts date portion “yyyy-MM-dd” from localtime string.
     *
     * @param fullJson
     * @return string “yyyy-MM-dd”.
     */
    public static String getLocalDate(JSONObject fullJson) {
        String local = getLocalTime(fullJson);
        if (local.contains(" ")) {
            return local.split(" ")[0];
        }
        return local;
    }

    /**
     * Extracts the local hour (0–23) from localtime string.
     *
     * @param fullJson
     * @return  integer (0–23).
     */
    public static int getLocalHour(JSONObject fullJson) {
        String local = getLocalTime(fullJson);
        try {
            String timePart = local.split(" ")[1];         // HH:mm
            return Integer.parseInt(timePart.split(":")[0]);
        } catch (Exception e) {
            return LocalDateTime.now().getHour();
        }
    }

    /**
     * Capitalizes first letter of each word.
     *
     * @param input string of words separated by spaces.
     * @return Capitalized
     */
    public static String capitalizeEachWord(String input) {
        String[] arr = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : arr) {
            if (w.length() > 0) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Formats a 4-digit time
     *
     * @param hhmm 
     * @return Formatted “HH:mm”.
     */
    public static String formatHourLabel(String hhmm) {
        if (hhmm.length() == 4) {
            return hhmm.substring(0, 2) + ":" + hhmm.substring(2);
        }
        return hhmm;
    }

    /**
     *URL-encoding of spaces(replaces " " with "%20").
     *
     * @param text user input (like “New York”).
     * @return Encoded string “New%20York”.
     */
    public static String encode(String text) {
        return text.replace(" ", "%20");
    }
}
