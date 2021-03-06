package com.amsen.par.placessearch.api;

import com.amsen.par.placessearch.BuildConfig;
import com.amsen.par.placessearch.api.response.PlacesResponse;
import com.amsen.par.placessearch.model.Prediction;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author Pär Amsen 2016
 */
public class PlacesApi {
    //private final static String API_KEY = "<YOUR_API_KEY>";
    private final static String API_KEY = "AIzaSyBtuM4Vw35jD9hW_uMx50yQw1sh-LDVZEw";

    private OkHttpClient client = new OkHttpClient();
    private Moshi moshi = new Moshi.Builder().build();
    private JsonAdapter<PlacesResponse> adapter = moshi.adapter(PlacesResponse.class);

    public PlacesApi() {
        client = new OkHttpClient.Builder()
                .addInterceptor(this::getDebugInterceptor)
                .build();

        establishInitialConnection();
    }

    public Observable<List<Prediction>> getPredictions(String query) {
        return fromApi(query)
                .subscribeOn(Schedulers.io());
    }

    private Observable<List<Prediction>> fromApi(String query) {
        return Observable.create(subscriber -> {
            if (API_KEY.contains("YOUR_API_KEY"))
                throw new RuntimeException("Set your API key for this example!");

            Request request = new Request.Builder()
                    .url("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + query + "&key=" + API_KEY)
                    .build();

            Response response;

            try {
                response = client.newCall(request)
                        .execute();
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }

            PlacesResponse fromJson;

            try {
                ResponseBody body = response.body();
                fromJson = adapter.fromJson(body.source());
                body.close();
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }

            subscriber.onNext(fromJson.predictions);
            subscriber.onCompleted();
        });
    }

    /**
     * The initial handshake adds a noticeable time
     * to the first request, better handshake in prior to
     * any "real" calls!
     */
    private void establishInitialConnection() {
        Observable.create(
                subscriber -> {
                    Request request = new Request.Builder()
                            .url("https://maps.googleapis.com/maps/api/place/autocomplete/json")
                            .build();

                    try {
                        client.newCall(request).execute();
                    } catch (IOException e) {
                        //ignore
                    }

                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private Response getDebugInterceptor(Interceptor.Chain chain) {
        try {
            Response response = chain.proceed(chain.request());

            if (BuildConfig.DEBUG) {
                System.out.println("========= Start OkHttp ==========");
                System.out.printf("OkHttp       Url: %s", response.request().url()).println();
                System.out.printf("OkHttp       Request time millis: %d", response.receivedResponseAtMillis() - response.sentRequestAtMillis()).println();
                System.out.println("========= End OkHttp ==========");
            }

            return response;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
