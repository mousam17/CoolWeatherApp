package app.coolweather.develop.tlabs.com.coolweather;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{
    private static final String TAG=MainActivity.class.getSimpleName();
    private static final String FORCASTFRAGMENT_TAG="ForecastFragment";
    private static final String DETAILFRAGMENT_TAG="DetailFragment";
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLocation=Utility.getPreferredLocation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       if(findViewById(R.id.detail_weather_container)!=null){
           mTwoPane=true;
           if(savedInstanceState==null){
               getSupportFragmentManager().beginTransaction().replace(R.id.detail_weather_container,DetailFragment.newInstance(),DETAILFRAGMENT_TAG).commit();
           }
       }else{
           mTwoPane=false;
       }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location=Utility.getPreferredLocation(this);
        if(location!=null && location.equals(mLocation)){
            ForecastFragment ff= (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
            if(ff!=null){
                ff.onLocationChanged();
            }

            DetailFragment df= (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df!=null){
                df.onLocationChanged(location);
            }
            mLocation=location;
        }

        Log.e(TAG,"onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy called");
    }

    @Override
        public void onItemSelected(Uri contentUri) {
                if (mTwoPane) {
                        // In two-pane mode, show the detail view in this activity by
                                // adding or replacing the detail fragment using a
                                        // fragment transaction.
                                                Bundle args = new Bundle();
                        args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

                                DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(args);
                               getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.detail_weather_container, fragment, DETAILFRAGMENT_TAG)
                                       .commit();
                   } else {
                        Intent intent = new Intent(this, DetailActivity.class)
                                        .setData(contentUri);
                       startActivity(intent);
                    }
            }
}
