import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class WeatherSwingApp extends JFrame {

    // 1) GUI COMPONENTS

    private JTextField locationInput;        
    private JComboBox<String> unitComboBox;  
    private JButton fetchButton;             

    private JLabel iconLabel;               
    private JLabel tempLabel;                
    private JLabel humidityLabel;            
    private JLabel windLabel;               
    private JLabel conditionsLabel;          

    private JPanel forecastPanel;            
    private DefaultListModel<String> historyListModel; 
    private JList<String> historyList;       

    private JPanel mainContentPane;          

    // 2) CONSTRUCTOR: BUILD THE UI

    public WeatherSwingApp() {
        super("Weather Information App (Swing)");

        // Set up the main content 
        mainContentPane = new JPanel(new BorderLayout(10, 10));
        mainContentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainContentPane);

        // ─ Top Panel 
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel locLabel = new JLabel("Location:");
        locationInput = new JTextField(20);
        JLabel unitLabel = new JLabel("Units:");
        unitComboBox = new JComboBox<>(new String[]{"Celsius", "Fahrenheit"});
        fetchButton = new JButton("Get Weather");

        topPanel.add(locLabel);
        topPanel.add(locationInput);
        topPanel.add(unitLabel);
        topPanel.add(unitComboBox);
        topPanel.add(fetchButton);

        mainContentPane.add(topPanel, BorderLayout.NORTH);

        // ─ Center Panel (Current Weather) 
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        iconLabel = new JLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tempLabel = new JLabel("Temperature: N/A");
        tempLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tempLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        humidityLabel = new JLabel("Humidity: N/A");
        humidityLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        humidityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        windLabel = new JLabel("Wind: N/A");
        windLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        windLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        conditionsLabel = new JLabel("Conditions: N/A");
        conditionsLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        conditionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(iconLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(tempLabel);
        centerPanel.add(humidityLabel);
        centerPanel.add(windLabel);
        centerPanel.add(conditionsLabel);

        mainContentPane.add(centerPanel, BorderLayout.CENTER);

        // ─ Right Panel 
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));

        // Forecast container
        JPanel forecastContainer = new JPanel();
        forecastContainer.setLayout(new BoxLayout(forecastContainer, BoxLayout.Y_AXIS));
        forecastContainer.setBorder(BorderFactory.createTitledBorder("Next 3 Hours Forecast"));

        forecastPanel = new JPanel();
        forecastPanel.setLayout(new BoxLayout(forecastPanel, BoxLayout.Y_AXIS));
        forecastContainer.add(forecastPanel);

        // History container
        JPanel historyContainer = new JPanel(new BorderLayout());
        historyContainer.setBorder(BorderFactory.createTitledBorder("Recent Searches"));
        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        historyList.setVisibleRowCount(8);
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyContainer.add(historyScrollPane, BorderLayout.CENTER);

        rightPanel.add(forecastContainer);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(historyContainer);

        mainContentPane.add(rightPanel, BorderLayout.EAST);

        mainContentPane.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);

        // fetch button
        fetchButton.addActionListener(e -> fetchWeatherData());

        // Final window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 3) fetchWeatherData(): CALLED WHEN “Get Weather” BUTTON IS CLICKED

    private void fetchWeatherData() {
        String query = locationInput.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a city name or coordinates (e.g., \"Montreal\" or \"43.65,-79.38\").",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Determine OWM unit string
        String unitParam = unitComboBox.getSelectedItem().equals("Celsius") ? "metric" : "imperial";

        try {
            // 1) Fetch current weather JSON from OWM
            JSONObject currentJson = ApiClient.fetchCurrentWeatherJson(query, unitParam);

            // 2) Check OWM's "cod" field: 200 = OK, otherwise error
            int code = currentJson.optInt("cod", 0);
            if (code != 200) {
                String msg = currentJson.optString("message", "Unknown error");
                JOptionPane.showMessageDialog(
                        this,
                        "Error fetching current weather:\n" + msg,
                        "Fetch Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 3) Parse current-weather fields
            JSONObject mainObj = currentJson.getJSONObject("main");
            double tempValue = mainObj.getDouble("temp");
            int humidityValue = mainObj.getInt("humidity");

            JSONObject windObj = currentJson.getJSONObject("wind");
            double windSpeedValue = windObj.getDouble("speed");

            JSONArray weatherArr = currentJson.getJSONArray("weather");
            JSONObject weatherObj = weatherArr.getJSONObject(0);
            String description = weatherObj.getString("description");
            String iconCode = weatherObj.getString("icon"); // e.g., "04d"
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            // 4) Update current-weather UI
            String unitSymbol = unitParam.equals("metric") ? "°C" : "°F";
            String windUnit = unitParam.equals("metric") ? "km/h" : "mph";

            tempLabel.setText(String.format("Temperature: %.1f %s", tempValue, unitSymbol));
            humidityLabel.setText("Humidity: " + humidityValue + "%");
            windLabel.setText(String.format("Wind: %.1f %s", windSpeedValue, windUnit));
            conditionsLabel.setText("Conditions: " + ApiClient.capitalizeEachWord(description));

            // Load icon image
            try {
                URL url = new URL(iconUrl);
                Image img = ImageIO.read(url);
                iconLabel.setIcon(new ImageIcon(img));
            } catch (IOException ioe) {
                iconLabel.setIcon(null);
            }

            // 5) Fetch forecast JSON from OWM
            JSONObject forecastJson = ApiClient.fetchForecastJson(query, unitParam);

            int fCode = forecastJson.optInt("cod", 0);
            if (fCode != 200) {
                String fMsg = forecastJson.optString("message", "Unable to fetch forecast");
                forecastPanel.removeAll();
                JLabel errorLabel = new JLabel("Forecast error: " + fMsg);
                errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                forecastPanel.add(errorLabel);
                forecastPanel.revalidate();
                forecastPanel.repaint();
            } else {
                updateForecastUI_OpenWeatherMap(forecastJson, unitSymbol);
            }

            // 6) Update search history (max 10 entries)
            String localTimeStr = ApiClient.getLocalTime(currentJson);
            String record = String.format("%s  —  %s", query, localTimeStr);

            if (historyListModel.getSize() == 10) {
                historyListModel.remove(9);
            }
            historyListModel.add(0, record);

            // 7) Change background
            setDynamicBackground(localTimeStr);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to retrieve weather data.\nCheck your network/API key and try again.",
                    "Fetch Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    // 4) updateForecastUI_OpenWeatherMap: SHOWS NEXT 3 FORECASTs

    private void updateForecastUI_OpenWeatherMap(JSONObject owmJson, String unitSymbol) {
        forecastPanel.removeAll();

        JSONArray listArray = owmJson.getJSONArray("list");
        int slotsFound = 0;

        // Take the first 3 entries
        for (int i = 0; i < listArray.length() && slotsFound < 3; i++) {
            JSONObject slot = listArray.getJSONObject(i);

            // Extract time
            String dateTime = slot.getString("dt_txt");
            String hourLabel = dateTime.substring(11, 16);

            JSONObject mainObj = slot.getJSONObject("main");
            double tempVal = mainObj.getDouble("temp");

            JSONArray weatherArr = slot.getJSONArray("weather");
            JSONObject weatherObj = weatherArr.getJSONObject(0);
            String desc = weatherObj.getString("description");
            String iconCode = weatherObj.getString("icon");
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            JPanel hourPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            JLabel timeLabel = new JLabel(hourLabel + ":");
            timeLabel.setPreferredSize(new Dimension(60, 20));
            hourPanel.add(timeLabel);

            // Load and display icon
            try {
                URL url = new URL(iconUrl);
                Image img = ImageIO.read(url);
                Image scaled = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                hourPanel.add(new JLabel(new ImageIcon(scaled)));
            } catch (IOException ioe) {
                // no icon if load fails
            }

            JLabel infoLabel = new JLabel(
                    String.format("%s, %.1f%s", ApiClient.capitalizeEachWord(desc), tempVal, unitSymbol)
            );
            hourPanel.add(infoLabel);

            forecastPanel.add(hourPanel);
            slotsFound++;
        }

        if (slotsFound == 0) {
            JLabel noneLabel = new JLabel("No forecast entries available.");
            noneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            forecastPanel.add(noneLabel);
        }

        forecastPanel.revalidate();
        forecastPanel.repaint();
    }

    // 5) setDynamicBackground: CHANGE BG COLOR 

    private void setDynamicBackground(String localTimeStr) {
        Color morningColor = new Color(255, 250, 205);
        Color dayColor     = new Color(173, 216, 230); 
        Color eveningColor = new Color(255, 99, 71);   
        Color nightColor   = new Color(25, 25, 112);   

        Color chosenColor = dayColor;

        if (localTimeStr != null) {
            try {
                String timePart = localTimeStr.split(" ")[1]; // "HH:mm"
                int hour = Integer.parseInt(timePart.split(":")[0]);
                if (hour >= 5 && hour < 12) {
                    chosenColor = morningColor;
                } else if (hour >= 12 && hour < 17) {
                    chosenColor = dayColor;
                } else if (hour >= 17 && hour < 20) {
                    chosenColor = eveningColor;
                } else {
                    chosenColor = nightColor;
                }
            } catch (Exception ignored) {
                chosenColor = dayColor;
            }
        }

        mainContentPane.setBackground(chosenColor);
    }

    // 6) main(): LAUNCH THE APPLICATION

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherSwingApp::new);
    }
}
