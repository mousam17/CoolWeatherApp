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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import app.coolweather.develop.tlabs.com.coolweather.data.WeatherContract;

/**
 * Created by Roadis on 11/2/2016.
 */

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    ForecastAdapter mForecastAdapter;
    ListView listViewForcast;
    private static final int FORECAST_LOADER = 0;
    int mPosition= ListView.INVALID_POSITION;
    static final String SELECTED_KEY="selected key";

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    /**
     * +     * A callback interface that all activities containing this fragment must
     * +     * implement. This mechanism allows activities to be notified of item
     * +     * selections.
     * +
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public static ForecastFragment newInstance() {

        Bundle args = new Bundle();

        ForecastFragment fragment = new ForecastFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(true);

        listViewForcast = (ListView) v.findViewById(R.id.listview_forcast);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        listViewForcast.setAdapter(mForecastAdapter);
        listViewForcast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                  /*  Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);*/
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
                }
                mPosition=position;
            }
        });

        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition=savedInstanceState.getInt(SELECTED_KEY);
        }
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onLocationChanged() {
        updateWeatherData();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateWeatherData() {
        FetchWeatherTask forcastWeatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        forcastWeatherTask.execute(location);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forcastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeatherData();
                return true;
            case R.id.action_settings:
                Intent setting_intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(setting_intent);
                return true;

            case R.id.action_map:
                String location = Utility.getPreferredLocation(getActivity());

                Intent map_intent = new Intent();
                map_intent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();
                map_intent.setData(uri);
                if (map_intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(map_intent);
                } else {
                    Log.i("ForecastFragment", "Couldn't call" + location);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition!=ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if(mPosition!=ListView.INVALID_POSITION){
            listViewForcast.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
