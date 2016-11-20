package app.coolweather.develop.tlabs.com.coolweather.parser;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import app.coolweather.develop.tlabs.com.coolweather.R;

/**
 * Created by Roadis on 11/3/2016.
 */

public class WeatherDataParser {
    private static String LOG_TAG = WeatherDataParser.class.getSimpleName();
    private final Context mCtx;

    public WeatherDataParser(Context ctx){
        this.mCtx=ctx;
    }

    public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) {
        try {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject
                        weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject
                        temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mCtx);
                String unitType=sharedPreferences.getString(mCtx.getString(R.string.pref_temprature_key),mCtx.getString(R.string.pref_temperature_metric));
                highAndLow = formatHighLows(high, low,unitType);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;

            }
            return resultStrs;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getReadableDateString(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd");
        return simpleDateFormat.format(time);
    }

    public String formatHighLows(double high, double low,String unitTypes) {
        if(unitTypes.equals(mCtx.getString(R.string.pref_temperature_imperial))){
            high=(high*1.8)+32;
            low=(low*1.8)+32;
        }else if(unitTypes.equals(mCtx.getString(R.string.pref_temperature_metric))){

        }

        long roundHign = Math.round(high);
        long roundLow = Math.round(low);
        return roundHign + "/" + roundLow;
    }
}
