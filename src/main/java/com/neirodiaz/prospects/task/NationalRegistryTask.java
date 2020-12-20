package com.neirodiaz.prospects.task;

import com.neirodiaz.prospects.client.NationalRegistryClient;
import com.neirodiaz.prospects.client.model.PersonalData;
import lombok.extern.log4j.Log4j2;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
public class NationalRegistryTask implements Runnable {

    private NationalRegistryClient client;
    private List<Long> identifications;
    private Map<Long, PersonalData> dataMap;
    private final Object lock = new Object();

    public NationalRegistryTask(NationalRegistryClient client, List<Long> identifications, Map<Long, PersonalData> dataMap) {
        this.client = client;
        this.dataMap = dataMap;
        this.identifications = identifications;
    }

    @Override
    public void run() {
        if (!Thread.interrupted()) {
            synchronized (lock) {
                for (Long id : identifications) {
                    try {
                        Response<PersonalData> execute = client.getRecords(id).execute();

                        if (execute.isSuccessful()) {
                            PersonalData body = execute.body();
                            if (body != null) {
                                dataMap.put(id, body);
                            }
                        }
                    } catch (IOException e) {
                        log.error("National Archives External Services fails, identification: " + id);
                    }
                }
            }
        }
    }
}
