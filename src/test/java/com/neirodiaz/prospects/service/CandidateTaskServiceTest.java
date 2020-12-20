package com.neirodiaz.prospects.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.neirodiaz.prospects.client.NationalArchivesClient;
import com.neirodiaz.prospects.client.NationalRegistryClient;
import com.neirodiaz.prospects.client.model.PersonalData;
import com.neirodiaz.prospects.client.model.PersonalRecord;
import com.neirodiaz.prospects.model.Candidate;
import com.neirodiaz.prospects.repository.CandidateRepository;
import com.neirodiaz.prospects.service.impl.CandidateTaskServiceImpl;
import com.neirodiaz.prospects.service.impl.CandidateVerifierImpl;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.MediaType;
import org.springframework.http.HttpStatus;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class CandidateTaskServiceTest {

    private NationalRegistryClient nationalRegistryClient =
            getRetrofitClient("http://localhost:8080/").create(NationalRegistryClient.class);

    private NationalArchivesClient nationalArchivesClient =
            getRetrofitClient("http://localhost:8080/").create(NationalArchivesClient.class);

    @Mock
    CandidateRepository repository;

    @Mock
    CandidateVerifierImpl verifier;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ClientAndServer mockServer;

    @InjectMocks
    CandidateService service;

    private List<Candidate> candidates;

    @BeforeEach
    void setUp() {
        mockServer = startClientAndServer(8080);
        service = new CandidateTaskServiceImpl(nationalRegistryClient, nationalArchivesClient, repository, verifier);
        MockitoAnnotations.openMocks(this);
        when(verifier.verify(Mockito.any(Candidate.class), Mockito.any(PersonalData.class))).thenCallRealMethod();
        when(verifier.verifyAndApplyPromotion(Mockito.any(Candidate.class))).thenCallRealMethod();
        when(verifier.getMaximumScore()).thenReturn(100);
        when(verifier.getMinimumScore()).thenReturn(60);

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        candidates = new ArrayList<>(Arrays.asList(
                Candidate.builder().id(1).identification(12345L).firstName("FA").lastName("LA").build(),
                Candidate.builder().id(2).identification(123456L).firstName("FB").lastName("LB").build(),
                Candidate.builder().id(3).identification(1234567L).firstName("FC").lastName("LC").build()
        ));
    }

    @AfterEach
    void stopMockServer() {
        mockServer.stop();
    }

    @Test
    void runPipelineAllCandidateWithHighScore() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(100);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(99);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(88);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Mock National Registry Service.
        personalDataList.forEach(this::mockRegistryResponse);

        // Mock National Archive Service.
        personalRecordList.forEach(this::mockArchiveResponse);

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        Assertions.assertEquals(3, result.size());
    }

    @Test
    void runPipelineAllCandidateWithLowScore() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(50);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(40);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(30);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Mock National Registry Service.
        personalDataList.forEach(this::mockRegistryResponse);

        // Mock National Archive Service.
        personalRecordList.forEach(this::mockArchiveResponse);

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void runPipelineOnlyFirstCandidateWithHighScore() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(100);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(40);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(30);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Mock National Registry Service.
        personalDataList.forEach(this::mockRegistryResponse);

        // Mock National Archive Service.
        personalRecordList.forEach(this::mockArchiveResponse);

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(12345L, result.get(0).getIdentification());
    }

    @Test
    void runPipelineOnlySecondCandidateHavingJudicialRecord() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(100);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(90);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(90);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Set judicial records for the 2nd candidate.
        personalRecordList.get(1).setHasRecords(true);

        // Mock National Registry Service.
        personalDataList.forEach(this::mockRegistryResponse);

        // Mock National Archive Service.
        personalRecordList.forEach(this::mockArchiveResponse);

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        boolean isPresentCandidate1 = result.stream().anyMatch(c -> c.getIdentification().equals(12345L));
        boolean isPresentCandidate3 = result.stream().anyMatch(c -> c.getIdentification().equals(1234567L));

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(isPresentCandidate1);
        Assertions.assertTrue(isPresentCandidate3);
    }

    @Test
    void runPipelineOnlySecondCandidateWithoutNationalIdentification() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(100);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(90);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(90);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Change Identification number for the 2nd candidate in the National Identification Service.
        personalDataList.get(1).setIdentification(654321L);

        // Mock National Registry Service.
        personalDataList.forEach(this::mockRegistryResponse);

        // Mock National Archive Service.
        personalRecordList.forEach(this::mockArchiveResponse);

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        boolean isPresentCandidate1 = result.stream().anyMatch(c -> c.getIdentification().equals(12345L));
        boolean isPresentCandidate3 = result.stream().anyMatch(c -> c.getIdentification().equals(1234567L));

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(isPresentCandidate1);
        Assertions.assertTrue(isPresentCandidate3);
    }

    @Test
    void runPipelineOnlySecondWithNationalIdentificationLatency() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(100);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(99);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(88);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Mock National Registry Service.
        mockRegistryResponse(personalDataList.get(0));
        mockRegistryResponse(personalDataList.get(1), 12);
        mockRegistryResponse(personalDataList.get(2));

        // Mock National Archive Service.
        personalRecordList.forEach(this::mockArchiveResponse);

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        boolean isPresentCandidate1 = result.stream().anyMatch(c -> c.getIdentification().equals(12345L));
        boolean isPresentCandidate3 = result.stream().anyMatch(c -> c.getIdentification().equals(1234567L));

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(isPresentCandidate1);
        Assertions.assertTrue(isPresentCandidate3);
    }

    @Test
    void runPipelineOnlySecondWithNationalJudicialRecordLatency() {
        doReturn(candidates).when(repository).findByIdentifications(Mockito.anyCollection());
        when(verifier.calculateScore(candidates.get(0))).thenReturn(100);
        when(verifier.calculateScore(candidates.get(1))).thenReturn(99);
        when(verifier.calculateScore(candidates.get(2))).thenReturn(88);

        //Create objects with the same data as Candidate.
        List<PersonalData> personalDataList = convertToPersonaData(candidates);
        List<PersonalRecord> personalRecordList = convertToPersonalRecord(candidates);

        // Mock National Registry Service.
        personalDataList.forEach(this::mockRegistryResponse);

        // Mock National Archive Service.
        mockArchiveResponse(personalRecordList.get(0));
        mockArchiveResponse(personalRecordList.get(1), 12);
        mockArchiveResponse(personalRecordList.get(2));

        // Set the sales lead to be evaluated.
        List<Long> salesLead = Arrays.asList(12345L, 123456L, 1234567L);

        List<Candidate> result = service.runPipeline(salesLead);

        boolean isPresentCandidate1 = result.stream().anyMatch(c -> c.getIdentification().equals(12345L));
        boolean isPresentCandidate3 = result.stream().anyMatch(c -> c.getIdentification().equals(1234567L));

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(isPresentCandidate1);
        Assertions.assertTrue(isPresentCandidate3);
    }

    private void mockArchiveResponse(PersonalRecord data) {
        this.mockArchiveResponse(data, 1);
    }

    private void mockArchiveResponse(PersonalRecord data, int seconds) {
        try {
            String responseBody = objectMapper.writeValueAsString(data);
            new MockServerClient("localhost", 8080)
                    .when(request().withMethod("GET").withPath("/v1/archive/" + data.getId()))
                    .respond(response().withStatusCode(HttpStatus.OK.value())
                            .withDelay(Delay.seconds(seconds))
                            .withBody(responseBody, MediaType.APPLICATION_JSON));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mockRegistryResponse(PersonalData data) {
        this.mockRegistryResponse(data, 1);
    }

    private void mockRegistryResponse(PersonalData data, int seconds) {
        try {
            String responseBody = objectMapper.writeValueAsString(data);
            new MockServerClient("localhost", 8080)
                    .when(request().withMethod("GET").withPath("/v1/data/" + data.getIdentification()))
                    .respond(response().withStatusCode(HttpStatus.OK.value())
                            .withDelay(Delay.seconds(seconds))
                            .withBody(responseBody, MediaType.APPLICATION_JSON));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<PersonalData> convertToPersonaData(List<Candidate> candidates) {
        return candidates.stream()
                .map(c -> PersonalData.builder()
                        .identification(c.getIdentification())
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<PersonalRecord> convertToPersonalRecord(List<Candidate> candidates) {
        return candidates.stream()
                .map(c -> PersonalRecord.builder()
                        .id(c.getIdentification())
                        .hasRecords(false)
                        .build())
                .collect(Collectors.toList());
    }

    private static Retrofit getRetrofitClient(String basePath) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        return new Retrofit.Builder()
                .baseUrl(basePath)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(clientBuilder.build())
                .build();
    }
}