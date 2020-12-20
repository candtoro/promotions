package com.neirodiaz.prospects.task;

import com.neirodiaz.prospects.client.NationalArchivesClient;
import com.neirodiaz.prospects.client.model.PersonalRecord;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NationalArchivesTask implements Runnable {

    private NationalArchivesClient client;
    private List<Long> identification;
    private Map<Long, Boolean> results;
    private final Object lock = new Object();

    public NationalArchivesTask(NationalArchivesClient client, List<Long> identification, Map<Long, Boolean> results) {
        this.client = client;
        this.results = results;
        this.identification = identification;
    }

    @Override
    public void run() {
        if (!Thread.interrupted()) {
            synchronized (lock) {
                for (Long id : identification) {
                    try {
                        Response<PersonalRecord> execute = client.getRecords(id).execute();

                        if (execute.isSuccessful()) {
                            PersonalRecord body = execute.body();
                            if (body != null) {
                                results.put(id, body.getHasRecords());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
