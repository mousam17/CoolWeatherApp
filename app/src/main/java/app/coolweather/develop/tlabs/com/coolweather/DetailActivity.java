package app.coolweather.develop.tlabs.com.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        if (savedInstanceState == null) {
            Bundle bundle=new Bundle();
            bundle.putParcelable(DetailFragment.DETAIL_URI,getIntent().getData());

            DetailFragment fragment1=new DetailFragment();
            fragment1.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.detail_weather_container, fragment1).commit();
        }
    }
}
