package com.example.m_hikeapp.util;

import android.content.Context;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Locale;

public class WeatherHelper {

    public interface WeatherCallback {
        void onSuccess(String weatherInfo);
        void onFailure(String errorMsg);
    }

    public interface DetailedWeatherCallback {
        void onSuccess(double currentTemp, String condition, String forecastWarning);
        void onFailure(String errorMsg);
    }

    private static final OkHttpClient client = new OkHttpClient();

    public static void fetchCurrentWeather(Context context, WeatherCallback callback) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Default location (Hanoi/London) if permission not granted
            getWeatherForLocation(21.0285, 105.8542, callback);
            return;
        }

        LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getWeatherForLocation(location.getLatitude(), location.getLongitude(), callback);
                    } else {
                        getWeatherForLocation(21.0285, 105.8542, callback);
                    }
                })
                .addOnFailureListener(e -> getWeatherForLocation(21.0285, 105.8542, callback));
    }

    private static void getWeatherForLocation(double lat, double lon, WeatherCallback callback) {
        String url = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current_weather=true",
                lat, lon);

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        JSONObject currentWeather = json.getJSONObject("current_weather");
                        double temp = currentWeather.getDouble("temperature");
                        int weatherCode = currentWeather.getInt("weathercode");

                        String condition = translateWeatherCode(weatherCode);
                        String result = String.format(Locale.getDefault(), "%.1f°C • %s", temp, condition);
                        callback.onSuccess(result);
                    } catch (Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                } else {
                    callback.onFailure("Server response error");
                }
            }
        });
    }

    public static void fetchDetailedWeather(Context context, DetailedWeatherCallback callback) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            getDetailedWeatherForLocation(21.0285, 105.8542, callback);
            return;
        }

        LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getDetailedWeatherForLocation(location.getLatitude(), location.getLongitude(), callback);
                    } else {
                        getDetailedWeatherForLocation(21.0285, 105.8542, callback);
                    }
                })
                .addOnFailureListener(e -> getDetailedWeatherForLocation(21.0285, 105.8542, callback));
    }

    private static void getDetailedWeatherForLocation(double lat, double lon, DetailedWeatherCallback callback) {
        String url = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current_weather=true&hourly=weathercode",
                lat, lon);

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        JSONObject currentWeather = json.getJSONObject("current_weather");
                        double temp = currentWeather.getDouble("temperature");
                        int weatherCode = currentWeather.getInt("weathercode");

                        String condition = translateWeatherCode(weatherCode);
                        String warning = "";

                        try {
                            String currentTime = currentWeather.getString("time");
                            JSONObject hourly = json.getJSONObject("hourly");
                            org.json.JSONArray timeArray = hourly.getJSONArray("time");
                            org.json.JSONArray codeArray = hourly.getJSONArray("weathercode");

                            for (int i = 0; i < timeArray.length() - 1; i++) {
                                if (timeArray.getString(i).equals(currentTime)) {
                                    int nextHourCode = codeArray.getInt(i + 1);
                                    if (nextHourCode >= 51) {
                                        warning = "Weather Warning (Next Hour): " + translateWeatherCode(nextHourCode);
                                    }
                                    break;
                                }
                            }
                        } catch (Exception ignore) {}

                        callback.onSuccess(temp, condition, warning);
                    } catch (Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                } else {
                    callback.onFailure("Server response error");
                }
            }
        });
    }

    private static String translateWeatherCode(int code) {
        switch (code) {
            case 0: return "☀️ Clear sky / Sunny";
            case 1: return "🌤️ Mainly clear";
            case 2: return "⛅ Partly cloudy";
            case 3: return "☁️ Overcast / Cloudy";
            case 45: case 48: return "🌫️ Foggy";
            case 51: case 53: case 55: return "🌦️ Light drizzle";
            case 61: case 63: case 65: return "🌧️ Rain showers";
            case 71: case 73: case 75: return "❄️ Snowfall";
            case 80: case 81: case 82: return "🌧️ Heavy rain / Wind";
            case 95: case 96: case 99: return "🌩️ Thunderstorm";
            default: return "🌬️ Breezy";
        }
    }
}
