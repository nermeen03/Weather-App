# Weather Alert Android App

This is an Android mobile application that provides weather information based on the user's current location or a chosen location. The app allows users to:

- View weather status and temperature from their location.
- Choose a specific location on the map or search for it via auto-complete.
- Add locations to a list of favorite places and get weather information for those locations.
- Set alerts for specific weather conditions such as rain, wind, low/high temperatures, fog, snow, and more.

---

## Features

🌍 **Location-Based Weather**
- Get the weather forecast for your current location using GPS.
- Choose a location manually by selecting it on the map or using the auto-complete search.

🔔 **Weather Alerts**
- Set alerts for various weather conditions (rain, wind, snow, etc.).
- Customize the alarm type and set a duration for when the alert is active.
- Option to stop the alarm or notification once triggered.

🌡️ **Temperature Units & Settings**
- Choose temperature units (Kelvin, Celsius, Fahrenheit).
- Select wind speed units (meters/sec, miles/hour).
- Switch between English and Arabic language options.

🏠 **Home Screen**
- Displays current weather data including:
  - Temperature
  - Date & Time
  - Humidity
  - Wind speed
  - Pressure
  - Cloud coverage
  - City name
  - Weather icon & description (e.g., clear sky, light rain, etc.)
- Shows past hourly weather data for the current day.
- Provides a 5-day weather forecast.

💖 **Favorite Locations**
- Add and manage favorite locations.
- Access detailed weather forecasts for any of your favorite locations.
- Remove saved locations if needed.
- Add a new favorite location using an interactive map or search bar.

⚙️ **Settings Screen**
- Set preferences for location and temperature units.
- Change language settings (Arabic/English).

---

- **Settings Screen**: Allows you to configure location preferences, units, and language.
- **Home Screen**: Shows current weather data, hourly and 5-day forecasts.
- **Weather Alerts Screen**: Set weather alerts for different conditions.
- **Favorite Locations Screen**: Add, view, and remove your favorite locations with weather data.

---

Setup Instructions

Prerequisites
To get started with the project, make sure you have:

- Android Studio installed.
- An active internet connection to retrieve weather data from OpenWeather API.
- Your OpenWeather API key.

API Key Setup
1. Visit [OpenWeatherMap](https://openweathermap.org/) and create an account.
2. Obtain your API key.
3. Replace API_KEY in the code with your actual API key.

Installation

1. Clone this repository:
  -> git clone https://github.com/nermeen03/weather-alert-app.git

2. Open the project in Android Studio.

3. Build and run the app on an emulator or physical device.

---

## API Documentation

This app uses the [OpenWeatherMap API](https://api.openweathermap.org/data/2.5/forecast) to fetch weather data. Be sure to read the API documentation to understand how the data is structured and to pick the appropriate endpoints for your use case.

