package being.altiplano.commumication.protocol;

import being.altiplano.commumication.mock.MCmdCode;
import being.altiplano.commumication.mock.MRequest;
import being.altiplano.commumication.mock.MResponse;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SyncClientWithSNTest extends ServerClientTestBase {
    @Test
    @Order(1)
    public void testWithOneQuery() {
        testWithServerAndClient((server, client) -> {
            SyncClientWithSN<MRequest, MResponse, Integer> syncClient = new SyncClientWithSN<>(client, MRequest::getSn, MResponse::getSn);
            Future<MResponse> responseFuture = syncClient.request(new MRequest(1, MCmdCode.ECHO    , "abc"));
            try {
                MResponse response = responseFuture.get();
                Assertions.assertEquals(new MResponse(1, "abc"), response);
            } catch (ExecutionException e) {
                Assertions.fail("Fail with exception");
            }
        });
    }

    @Test
    @Order(2)
    public void testWithMultiQuery() {
        testWithServerAndClient((server, client) -> {
            try {
                SyncClientWithSN<MRequest, MResponse, Integer> syncClient = new SyncClientWithSN<>(client, MRequest::getSn, MResponse::getSn);
                MRequest[] inputs = getInputRequests();
                MResponse[] expects = getExpectResponses();

                List<Future<MResponse>> futures = new ArrayList<>();
                for (MRequest request : inputs) {
                    futures.add(syncClient.request(request));
                }

                List<Future<MResponse>> shuffledFutures = new ArrayList<>(futures);
                Collections.shuffle(shuffledFutures);
                for (Future<MResponse> future : shuffledFutures) {
                    future.get();
                }

                List<MResponse> responses = new ArrayList<>();
                for (Future<MResponse> f : futures){
                    responses.add(f.get());
                }

                Assertions.assertArrayEquals(expects, responses.toArray(new MResponse[0]));
            } catch (ExecutionException e) {
                Assertions.fail("Fail with exception");
            }
        });
    }

    private MRequest[] getInputRequests(){
        int snGen = 0;
        MRequest[] requests = new MRequest[]{
                new MRequest(snGen++, MCmdCode.UPPER, "Abc"),
                new MRequest(snGen++, MCmdCode.LOWER, "Abc"),
                new MRequest(snGen++, MCmdCode.ECHO, "abc"),
                new MRequest(snGen++, MCmdCode.ECHO_TWICE, "abc"),
                new MRequest(snGen++, MCmdCode.REVERSE, "abc"),
                new MRequest(snGen++, MCmdCode.COUNT, "abc"),
                new MRequest(snGen++, MCmdCode.ECHO, "xyz"),
        };
        return requests;
    }

    private MResponse[] getExpectResponses(){
        int respSnGen = 0;
        MResponse[] expects = new MResponse[]{
                new MResponse(respSnGen++, "ABC"),
                new MResponse(respSnGen++, "abc"),
                new MResponse(respSnGen++, "abc"),
                new MResponse(respSnGen++, "abcabc"),
                new MResponse(respSnGen++, "cba"),
                new MResponse(respSnGen++, "3"),
                new MResponse(respSnGen, "xyz")
        };
        return expects;
    }

}