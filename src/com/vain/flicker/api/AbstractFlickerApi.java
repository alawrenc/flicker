package com.vain.flicker.api;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.vain.flicker.api.client.AbstractWebClient;
import com.vain.flicker.model.asset.Asset;
import com.vain.flicker.model.match.*;
import com.vain.flicker.model.player.Player;
import com.vain.flicker.model.sample.Sample;
import com.vain.flicker.model.status.Status;
import com.vain.flicker.utils.Shard;
import org.asynchttpclient.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Dominic Gunn (dominic@vain.gg)
 */
public abstract class AbstractFlickerApi extends AbstractWebClient {

    protected final static ResourceConverter resourceConverter = new ResourceConverter(
            Match.class, Participant.class, Player.class, Roster.class, Team.class, Status.class,
            Sample.class, Asset.class
    );

    private final static String API_VERSION = "gamelockerd-v6.1.3";

    private final static String CONTENT_ENCODING_HEADER = "Content-Encoding";
    private final static String CONTENT_ENCODING_GZIP = "gzip";

    private final static String X_TITLE_ID_HEADER = "X-TITLE-ID";
    private final static String X_TITLE_ID_VALUE = "semc-vainglory";

    private final static String ACCEPT_HEADER = "Accept";
    private final static String APPLICATION_VND_API_JSON = "application/vnd.api+json";

    private final static String AUTHORIZATION_HEADER = "Authorization";

    private final static String BASE_API_URL = "https://api.dc01.gamelockerapp.com/shards/";
    private final static String STATUS_API_URL = "https://api.dc01.gamelockerapp.com/status";

    private String jwtToken = null;
    private Shard shard = Shard.NA;

    private Status apiStatus;
    private boolean statusChecked;

    public AbstractFlickerApi(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    protected CompletableFuture<Response> get(final String requestUrl, final Map<String, List<String>> params) {
        if (jwtToken == null ) {
            throw new FlickerException("You must set an API Key before the server can be queried!");
        }

        if (!statusChecked) {
            checkStatus();
        }

        return httpClient.prepareGet(BASE_API_URL + requestUrl).setQueryParams(params)
                .addHeader(ACCEPT_HEADER, APPLICATION_VND_API_JSON)
                .addHeader(X_TITLE_ID_HEADER, X_TITLE_ID_VALUE)
                .addHeader(CONTENT_ENCODING_HEADER, CONTENT_ENCODING_GZIP)
                .addHeader(AUTHORIZATION_HEADER, "Bearer " + jwtToken)
                .execute().toCompletableFuture();
    }

    public void checkStatus() {
        statusChecked = true;
        httpClient.prepareGet(STATUS_API_URL).execute().toCompletableFuture().thenAccept(statusResponse -> {
            this.apiStatus = resourceConverter.readDocument(statusResponse.getResponseBodyAsBytes(), Status.class).get();
            if (!apiStatus.getVersion().equals(API_VERSION)) {
                System.out.println("**********************************");
                System.out.println("WARNING: Flicker is out of date");
                System.out.println("Flicker API Version: " + API_VERSION);
                System.out.println("Current API Version: " + apiStatus.getVersion());
                System.out.println("API update occurred: " + apiStatus.getReleasedAt());
                System.out.println("**********************************");
            }
        });
    }

    public Status getApiStatus() {
        return apiStatus;
    }

    public Shard getShard() {
        return shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
    }
}
