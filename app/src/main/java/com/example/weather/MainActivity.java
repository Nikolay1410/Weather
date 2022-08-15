package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class
MainActivity extends AppCompatActivity {

    private EditText editTextCity;
    private TextView textViewWeather;
    private ImageView imageViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextCity = findViewById(R.id.editTextCity);
        textViewWeather = findViewById(R.id.textViewWeather);
        imageViewTitle = findViewById(R.id.imageViewTitle);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myCity = preferences.getString("city", null);
        if(myCity !=null) {
            showWeather(myCity);
        }
    }

    public void onClickShowWeather(View view) {
        String city = editTextCity.getText().toString().trim();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString("city", city).apply();
        showWeather(city);
    }

    public void showWeather(String city){
        if(!city.isEmpty()){
            DownloadWeatherTask weatherTask = new DownloadWeatherTask();
            String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=fa1d6d496f78ea32e26f75e059d36fa7&lang=ru&units=metric";
            String url = String.format(WEATHER_URL, city);
            weatherTask.execute(url);
        }
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null){
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String city = jsonObject.getString("name");
                String temp = jsonObject.getJSONObject("main").getString("temp");
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                double tempD  = Double.parseDouble(temp);
                int tempI = (int) tempD;
                if (tempI<0){
                    imageViewTitle.setImageResource(R.drawable.winter);
                }if (!description.equals("дождь")) {
                   imageViewTitle.setImageResource((R.drawable.sunny));
                }else {
                    imageViewTitle.setImageResource(R.drawable.rain);
                }
                String speed = jsonObject.getJSONObject("wind").getString("speed");
                String weather = String.format("%s\nТемпература: %s°C\nНа улице: %s\nСкорость ветра: %sм.с.", city, temp,description, speed);
                textViewWeather.setText(weather);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}