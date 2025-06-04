Weather Information App

This is a Java Swing Weather Information App that retrieves and displays real-time weather data and a short-term forecast for a specified location using the OpenWeatherMap API. Users can enter a city name (or coordinates), choose between Celsius and Fahrenheit units, and view current conditions along with the next three forecasted hours. The application also tracks up to ten recent searches and dynamically changes its background color.

**Table of Contents**

Features

Prerequisites & Dependencies

Project Structure

Setup & Compilation

Running the Application

How to Use

Implementation Details

Error Handling



**Features**

Current Weather Display: Shows temperature, humidity, wind speed, and weather description with an icon.

Short-Term Forecast: Displays the next three hours forecast for the chosen city.

Unit Conversion: Switch between Celsius (°C, km/h) and Fahrenheit (°F, mph).

Recent Search History: Maintains up to ten previous searches with timestamps.

Dynamic Background: Changes the background color of the main window

Error Messages: Alerts user if the city is not found, API key is invalid, or network errors

**Prerequisites & Dependencies**

1. JDK (Java Development Kit) 8 or higher

Swing is included in JDK

2. OpenWeatherMap API Key

Sign up at openweathermap.org and obtain a free API key.

In this project, the key is stored in ApiClient.java: "private static final String OWM_API_KEY = "YOUR API KEY"

3. org.json library

Download the latest json-<version>.jar from https://github.com/stleary/JSON-java

Place it in the lib/ folder and reference it on the classpath when compiling/running.(example: javac -cp ".;lib\json-20250517.jar" ApiClient.java WeatherSwingApp.java)

**Project Structure**

WeatherProject/
├── ApiClient.java           # Handles all HTTP requests and JSON parsing
├── WeatherSwingApp.java     # Main Swing GUI and application logic
└── lib/
    └── json-20250517.jar    # org.json library JAR

**Setup & Compilation**

1. Download & Place org.json JAR
Ensure json-20250517.jar (or equivalent) is in WeatherProject/lib/.

2. Verify OpenWeatherMap API Key
In ApiClient.java, confirm the API key string is exactly correct (no extra whitespace or missing characters).

3. Compile Java Source Files
Open a terminal or PowerShell and navigate to the WeatherProject/ directory
Run the following command, making sure to adjust the classpath separator (; on Windows, : on macOS/Linux):
Windows (PowerShell or CMD): javac -cp ".;lib\json-20250517.jar" ApiClient.java WeatherSwingApp.java
macOS / Linux (Terminal): javac -cp ".:lib/json-20250517.jar" ApiClient.java WeatherSwingApp.java
This produces ApiClient.class and WeatherSwingApp.class in the same folder.

**Running the Application**
After compilation

Windows: java -cp ".;lib\json-20250517.jar" WeatherSwingApp
macOS / Linux: java -cp ".:lib/json-20250517.jar" WeatherSwingApp
If you see an error about an invalid API key, verify your key in ApiClient.java and re-compile.

**How to Use**

1. Enter Location
In the "Location" text field, type a city name (e.g., "Montreal") or geographic coordinates (e.g., "43.65,-79.38").

2. Select Units
Choose "Celsius" for metric (°C, km/h) or "Fahrenheit" for imperial (°F, mph) using the dropdown.

3. View Current Weather

4. View Short-Term Forecast

**Implementation Details**

1. API Calls & JSON Parsing (ApiClient.java)
Current Weather:
Builds URL: 
String url = String.format(
  "https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s",
  encode(location), units, OWM_API_KEY);

Parses JSON fields:

"main" → temp, humidity.

"wind" → speed.

"weather" (array) → [0].description, [0].icon.

"dt" (UTC seconds) + "timezone" (offset) → local date/time.

Forecast:
Builds URL:
String url = String.format(
  "https://api.openweathermap.org/data/2.5/forecast?q=%s&units=%s&appid=%s",
  encode(location), units, OWM_API_KEY);

The response contains a "list" array of 3-hour blocks (5-day forecast).

The code grabs the first three entries from list[0], list[1], list[2] (next ~3h, ~6h, ~9h).

Each block has: "dt_txt" (e.g., "2025-06-01 15:00:00"), "main.temp", "weather[0].description", "weather[0].icon".

Utilities:

encode(String) replaces spaces with %20 for simple URL encoding.

getLocalTime(JSONObject) uses dt + timezone to convert UTC seconds to LocalDateTime in the target locale.

capitalizeEachWord(String) uppercases the first letter of each word in a description.

formatHourLabel(String) converts a 4-digit string (e.g., "1500") to "15:00".

2. Swing GUI & Application Logic (WeatherSwingApp.java)

Layout:

BorderLayout on mainContentPane:

North: Top panel (location input, unit selection, fetch button).

Center: Current weather panel (icon + labels).

East: Right panel (forecast + history).

South: Padding.

Top Panel:

JTextField locationInput

JComboBox<String> unitComboBox ("Celsius", "Fahrenheit")

JButton fetchButton

Center Panel:

JLabel iconLabel (ImageIcon for current conditions).

JLabel tempLabel, humidityLabel, windLabel, conditionsLabel.

Right Panel:

Forecast Container (JPanel with BoxLayout.Y_AXIS, titled border):

forecastPanel (another vertical BoxLayout) holds three horizontal subpanels for each forecast slot.

History Container (JPanel with BorderLayout, titled border):

JList<String> historyList backed by a DefaultListModel<String>, wrapped in JScrollPane.

Event Handling:

fetchButton → calls fetchWeatherData().

fetchWeatherData():

Reads and validates locationInput.

Chooses unitParam = "metric" or "imperial".

Calls ApiClient.fetchCurrentWeatherJson(...):

Checks "cod" field for error codes (if not 200, shows a JOptionPane with the error message).

If OK, parses and updates center labels and icon.

Calls ApiClient.fetchForecastJson(...):

Checks "cod" again; if error, displays message in forecastPanel.

If OK, calls updateForecastUI_OpenWeatherMap(...) to display the next three entries.

Updates historyListModel (inserts current search + timestamp at index 0, removes index 9 if size > 10).

Calls setDynamicBackground(localTimeStr) to adjust background color.

Forecast UI Method (updateForecastUI_OpenWeatherMap(...)):

Empties forecastPanel.

Iterates through listArray from forecastJson, picks first three valid entries:

Extracts dt_txt → substring(11,16) for "HH:mm".

Reads main.temp, weather[0].description, weather[0].icon.

Creates a small horizontal JPanel with JLabel for time, ImageIcon, and description+temp text.

Adds it to forecastPanel.

If fewer than three entries, shows a message.

Calls revalidate() and repaint().

