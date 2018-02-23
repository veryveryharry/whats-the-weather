package com.supernominal.whatstheweather;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    TextView weatherTextView;
    TextView tempTextView;
    LinearLayout linearLayout;
    EditText cityEditText;

    public void getWeather(View view) {
        String cityName = cityEditText.getText().toString();
        Log.i("City name:", cityName);

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityEditText.getWindowToken(), 0);

        Map<String, String> envMap = new HashMap();
        try {
            InputStream is = this.getResources().getAssets().open("key.env");
            Properties p = new Properties();
            p.load(is);
            Enumeration keys = p.propertyNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                envMap.put(key, p.getProperty(key));
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String apiKey = envMap.get("apikey");

        new JSONDownloader().execute("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(cityName) + "&units=imperial&appid=" + apiKey);
    }

    public class JSONDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("Result:", result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                String weatherInfo = jsonObject.getString("weather");
                JSONArray weatherArr = new JSONArray(weatherInfo); // weather info (rain etc)
                weatherTextView.setText(weatherArr.getJSONObject(0).getString("main"));

                String mainInfo = jsonObject.getString("main");
                JSONObject main = new JSONObject(mainInfo);
                tempTextView.setText(main.getString("temp") + (char) 0x00B0 + "F");

                linearLayout.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        cityEditText = (EditText) findViewById(R.id.cityEditText);
    }
}
