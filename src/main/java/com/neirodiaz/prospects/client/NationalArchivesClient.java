package com.neirodiaz.prospects.client;

import com.neirodiaz.prospects.client.model.PersonalRecord;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NationalArchivesClient {

    @GET("/v1/archive/{id}")
    Call<PersonalRecord> getRecords(@Path("id") Long id);
}
