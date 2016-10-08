package com.amsen.par.placessearch.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.amsen.par.placessearch.R;
import com.amsen.par.placessearch.model.Prediction;
import com.amsen.par.placessearch.view.view.PlacesAutoCompleteSearchView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Pär Amsen 2016
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.clickSearch)
    View clickSearch;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.selectedPrediction)
    TextView selectedPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        PlacesAutoCompleteSearchView searchView = (PlacesAutoCompleteSearchView) searchViewItem.getActionView();

        searchView.setOnPredictionClickListener((position, autoCompletePrediction) -> {
            clickSearch.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
            selectedPrediction.setVisibility(View.VISIBLE);

            Prediction prediction = (Prediction) autoCompletePrediction.value;
            selectedPrediction.setText(String.format("Place.description: \"%s\"\nPlace.placeId: \"%s\"", prediction.description, prediction.placeId));
            searchViewItem.collapseActionView();
        });

        return true;
    }
}