package com.redux.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.redux.android.sunshine.data.WeatherContract;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
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
    //private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ForecastAdapter mForecastAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The CursorAdapter will take data from our cursor and populate the ListView.
                mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



        //View rootview = inflater.inflate(R.layout.fragment_main, container, false);


        ListView listview = (ListView) rootView.findViewById(
                R.id.listview_forecast);
        listview.setAdapter(mForecastAdapter);
        // We'll call our MainActivity
          listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                  @Override
                   public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                                       // if it cannot seek to that position.
                                        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                                if (cursor != null) {
                                        String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                                                        .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                                                locationSetting, cursor.getLong(COL_WEATHER_DATE)
                                                                ));
                                        startActivity(intent);
                                    }
                            }
                    });


        return rootView;
    }

    @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

    void onLocationChanged( ) {
                updateWeather();
                getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    @Override

        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String locationSetting = Utility.getPreferredLocation(getActivity());

                    // Sort order:  Ascending, by date.
                            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                            locationSetting, System.currentTimeMillis());

                    return new CursorLoader(getActivity(),
                            weatherForLocationUri,
                            FORECAST_COLUMNS,
                            null,
                            null,
                            sortOrder);
        }

                @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mForecastAdapter.swapCursor(cursor);
        }

                @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            mForecastAdapter.swapCursor(null);
       }
}
