package com.example.countries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String REQUEST_URL = "https://restcountries.eu/rest/v2/region/asia";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();


    RecyclerView recyclerView;
    CountryAdapter countryAdapter;
    AppDatabase appDatabase;
    FloatingActionButton delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        delete = findViewById(R.id.delete);
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "asian_countries").build();
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appDatabase.countryDao().removeAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Deleted from db", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
            }
        });

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            fetchFromServer();
        } else {
            fetchFromRoom();
        }
    }

    private void fetchFromRoom() {

        appDatabase.countryDao().getAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<List<Country>>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(List<Country> countries) {
                updateUI(countries);
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private void fetchFromServer() {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, REQUEST_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                List<Country> arrayList = new ArrayList<>();
                arrayList = extractCountries(response);

                updateUI(arrayList);

                appDatabase.countryDao().insertAll(arrayList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        //All entries replaced.
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.getMessage());
            }
        });

        jsonArrayRequest.setShouldCache(false);
        requestQueue.add(jsonArrayRequest);
    }


    public void updateUI(List<Country> arrayList) {
        countryAdapter = new CountryAdapter(arrayList, MainActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(countryAdapter);
    }

    public List<Country> extractCountries(JSONArray root) {
        List<Country> arrayList = new ArrayList<>();

        try {
            int no_of_countries = root.length();
            for (int i = 0; i < no_of_countries; i++) {
                JSONObject country = root.getJSONObject(i);
                String name = country.getString("name");
                String capital = country.getString("capital");
                String flag = country.getString("flag");
                String region = country.getString("region");
                String subregion = country.getString("subregion");
                long population = country.getLong("population");
                JSONArray bordersArray = country.getJSONArray("borders");
                JSONArray languagesArray = country.getJSONArray("languages");

                String borders = "";
                for (int j = 0; j < bordersArray.length(); j++)
                    borders = borders + bordersArray.getString(j) + " ";

                String languages = "";
                for (int j = 0; j < languagesArray.length(); j++) {
                    JSONObject languagesObject = languagesArray.getJSONObject(j);
                    languages = languages + languagesObject.getString("name") + ", ";
                }
                languages = languages.substring(0, languages.length() - 2);

                arrayList.add(new Country(name, capital, flag, region, subregion, population + "", borders, languages));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    @Override
    protected void onDestroy() {
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
        super.onDestroy();
    }
}