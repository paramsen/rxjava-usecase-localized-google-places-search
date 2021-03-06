package com.amsen.par.placessearch.view.view;

import android.content.Context;
import android.util.AttributeSet;

import com.amsen.par.placessearch.api.PlacesApi;
import com.amsen.par.placessearch.model.Prediction;
import com.amsen.par.searchview.AutoCompleteSearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * @author Pär Amsen 2016
 */
public class PlacesAutoCompleteSearchView extends AutoCompleteSearchView {
    private PublishSubject<String> queryStream;
    private PlacesApi api;

    public PlacesAutoCompleteSearchView(Context context) {
        super(context);
        init();
    }

    public PlacesAutoCompleteSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlacesAutoCompleteSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        api = new PlacesApi();
    }

    public void initBehavior() {
        publishRawQueries();
        subscribeRawQueries();
    }

    private void subscribeRawQueries() {
        Observable<String> filtered = queryStream.asObservable()
                .filter(query -> query.length() > 0)
                .doOnNext(e -> showLoader())
                .share();

        filtered.throttleWithTimeout(200, TimeUnit.MILLISECONDS, Schedulers.computation())
                .take(1)
                .concatWith(filtered.throttleWithTimeout(600, TimeUnit.MILLISECONDS, Schedulers.computation()))
                .flatMap(api::getPredictions)
                .map(this::toAutoCompletePredictions)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(e -> hideLoader())
                .subscribe(this::applyPredictions, Throwable::printStackTrace);
    }

    private List<com.amsen.par.searchview.prediction.Prediction> toAutoCompletePredictions(List<Prediction> predictions) {
        List<com.amsen.par.searchview.prediction.Prediction> transformed = new ArrayList<>();

        for (Prediction prediction : predictions) {
            transformed.add(new com.amsen.par.searchview.prediction.Prediction(prediction, prediction.description));
        }

        return transformed;
    }

    private void publishRawQueries() {
        setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryStream.onNext(newText);

                return true;
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        queryStream = PublishSubject.create();
        initBehavior();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        queryStream.onCompleted();
    }
}
