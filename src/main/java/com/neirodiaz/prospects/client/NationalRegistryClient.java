package com.neirodiaz.prospects.client;

import com.neirodiaz.prospects.client.model.PersonalData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NationalRegistryClient {

    @GET("/v1/data/{id}")
    Call<PersonalData> getRecords(@Path("id") Long id);
}
