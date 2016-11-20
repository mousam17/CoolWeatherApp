package app.coolweather.develop.tlabs.com.coolweather;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.coolweather.develop.tlabs.com.coolweather.data.WeatherContract;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    private Context mContext;


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.mContext = context;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutid = -1;
        if (viewType == VIEW_TYPE_TODAY) {
            layoutid = R.layout.list_item_forecast_today;
        } else if (viewType == VIEW_TYPE_FUTURE_DAY) {
            layoutid = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate(layoutid, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder holder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY:

                holder.ivWeatherView.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            case VIEW_TYPE_FUTURE_DAY:
                holder.ivWeatherView.setImageResource(Utility.getIconResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
        }

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        holder.tvDateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.tvDescriptionView.setText(description);

        boolean isMetric = Utility.isMetric(context);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.tvMaxTempView.setText(Utility.formatTemperature(mContext, high, isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.tvMinTempView.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    public static class ViewHolder {
        public final ImageView ivWeatherView;
        public final TextView tvDateView;
        public final TextView tvDescriptionView;
        public final TextView tvMaxTempView;
        public final TextView tvMinTempView;

        public ViewHolder(View v) {
            ivWeatherView = (ImageView) v.findViewById(R.id.ivWeatherView);
            tvDateView = (TextView) v.findViewById(R.id.tvDateView);
            tvDescriptionView = (TextView) v.findViewById(R.id.tvForecastView);
            tvMaxTempView = (TextView) v.findViewById(R.id.tvMaxTempView);
            tvMinTempView = (TextView) v.findViewById(R.id.tvMinTempView);
        }
    }
}