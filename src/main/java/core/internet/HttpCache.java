package core.internet;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import core.MainLogger;
import core.restclient.RestClient;

public class HttpCache {

    public static CompletableFuture<HttpResponse> get(String url, Duration duration) {
        HttpHeader[] headers = new HttpHeader[] {
                new HttpHeader("X-Proxy-Url", url),
                new HttpHeader("X-Proxy-Minutes", String.valueOf(duration.toMinutes()))
        };
        return RestClient.WEBCACHE.get("cached_proxy", headers)
                .thenApply(httpResponse -> {
                    checkResponseCode(url, httpResponse);
                    return httpResponse;
                });
    }

    public static CompletableFuture<HttpResponse> post(String url, String body, String contentType, Duration duration) {
        HttpHeader[] headers = new HttpHeader[] {
                new HttpHeader("X-Proxy-Url", url),
                new HttpHeader("X-Proxy-Minutes", String.valueOf(duration.toMinutes()))
        };
        return RestClient.WEBCACHE.post("cached_proxy", contentType, body, headers)
                .thenApply(httpResponse -> {
                    checkResponseCode(url, httpResponse);
                    return httpResponse;
                });
    }

    private static void checkResponseCode(String url, HttpResponse httpResponse) {
        int code = httpResponse.getCode();
        if (code / 100 != 2) {
            MainLogger.get().warn("Error code {} for URL {}", code, url);
        }
    }

}