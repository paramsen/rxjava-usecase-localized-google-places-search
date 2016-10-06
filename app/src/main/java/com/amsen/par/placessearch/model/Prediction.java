package com.amsen.par.placessearch.model;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * @author Pär Amsen 2016
 */
public class Prediction {
    public final String id;
    @Json(name = "place_id")
    public final String placeId;
    public final String description;
    public final List<Terms> terms;

    public Prediction(String id, String placeId, String description, List<Terms> terms) {
        this.id = id;
        this.placeId = placeId;
        this.description = description;
        this.terms = terms;
    }
}
