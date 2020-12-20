# Prospect evaluator backend

This services evaluates sales lead to know if they are able to become a prospect, external services used:

## External Services
- National Registry Identification Service: get personal information for one candidate. 
- National Judicial Records Service: get judicial records for one candidate if apply.

## Internal system
- Qualification systems: Assign a score to the candidate.

## Rules
````
 1. Candidate's personal information must match with the National Registry Identification service information.
 2. The Candidate must not have any judicial records.
 3. The minimum score to become a prospect is 60.
````

## Endpoint
````
- http://localhost:8080/parallel/{ids}
````
For instance:
 - http://localhost:8080/parallel/123456,12345678,12345679

## Test
````
 For improving readability use:  mvn test -Dmockserver.logLevel=OFF  
````
##Notes
 - Candidates are not being saved after the process (states are lost). 
 - Externals System could be another spring boot application.
 - Using threads is risky, it can be improved with ThreadPool, but it still will be risky.
 - Kubernetes should be implemented, It offers horizontal scaling in order to improve the availability and avoid threads.
 - Docker would be helpful, it offers an integrate/isolate environment.
 - Fault tolerance could be implemented with Hystrix.
 - Candidate service could be improved the "runPipeline(...)" method to return an customized object with: 
   Nonexistent Candidates, Candidates as Prospect, Candidates rejected (showing the score value), 
   Candidates without National Registry Identification, Candidates having Judicial records
 - "CandidateVerifierImpl" class could be uncoupled with the "Interface segregation" Principle: Extract "calculator score" method in a separated interface.
 - UnitTests were created only for Service Layer. 
     