package app.coolweather.develop.tlabs.com.coolweather;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.coolweather.develop.tlabs.com.coolweather.data.WeatherContract;
import app.coolweather.develop.tlabs.com.coolweather.data.WeatherContract.WeatherEntry;

/**
 * Created by Roadis on 11/4/2016.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView tvWeather;
    private static final int DETAIL_LOADER = 0;
    static final String DETAIL_URI = "URI";
    private ShareActionProvider shareActionProvider;
    private String mForecastStr;
    private TextView tvDay;
    private TextView tvDateView;
    private TextView tvMaxTemp;
    private TextView tvMinTemp;
    private TextView tvForcast;
    private TextView tvWind;
    private TextView tvPressure;
    private TextView tvHumidity;
    private ImageView ivWeather;
    private Uri mUri;

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;


    public static DetailFragment newInstance() {

        Bundle args = new Bundle();

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        tvDay = (TextView) rootView.findViewById(R.id.tvDay);
        tvDateView = (TextView) rootView.findViewById(R.id.tvDateView);
        tvMaxTemp = (TextView) rootView.findViewById(R.id.tvMaxTempView);
        tvMinTemp = (TextView) rootView.findViewById(R.id.tvMinTempView);
        tvForcast = (TextView) rootView.findViewById(R.id.tvForecastView);
        tvWind = (TextView) rootView.findViewById(R.id.tvWind);
        tvPressure = (TextView) rootView.findViewById(R.id.tvPressure);
        tvHumidity = (TextView) rootView.findViewById(R.id.tvHumidity);
        ivWeather = (ImageView) rootView.findViewById(R.id.ivWeatherView);

        return rootView;
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_menu, menu);
        MenuItem menuitem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuitem);
        if (mForecastStr != null) {
            shareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent setting_intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(setting_intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent getShareIntent() {
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);
        share_intent.setType("text/plain");
        share_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        share_intent.putExtra(Intent.EXTRA_TEXT, mForecastStr + "#COOLWeatherAPp");
        return share_intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
       /* Intent intent = getActivity().getIntent();
        if (intent == null && intent.getData() == null) {
            return null;
        }*/

        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            ivWeather.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            tvDateView.setText(friendlyDateText);
            tvDay.setText(dateText);

            // Read description from cursor and update view
            String description = data.getString(COL_WEATHER_DESC);
            tvForcast.setText(description);

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high, isMetric);
            tvMaxTemp.setText(highString);

            // Read low temperature from cursor and update view
            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
            tvMinTemp.setText(lowString);

            // Read humidity from cursor and update view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            tvHumidity.setText(getActivity().getString(R.string.format_humidity, humidity));

            // Read wind speed and direction from cursor and update view
            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            tvWind.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            // Read pressure from cursor and update view
            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            tvPressure.setText(getActivity().getString(R.string.format_pressure, pressure));

            // We still need this for the share intent
            mForecastStr = String.format("%s - %s - %s/%s", dateText, description, high, low);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(getShareIntent());
            }


        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
