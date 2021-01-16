package tech.intac.devtools.cachingproxy;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpRequest.newBuilder;

public class ProxyServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var config = Config.getInstance();
        var requestMethod = request.getMethod().toLowerCase();
        var reqBody = String.join("\n", IOUtils.readLines(request.getInputStream()));

        checkIfPassThrough:
        {
            if ("get".equals(requestMethod) && config.isCacheGetRequests()) {
                break checkIfPassThrough;
            }

            if ("post".equals(requestMethod) && config.isCachePostRequests()) {
                break checkIfPassThrough;
            }

            try {
                var remoteResponse = sendRemoteRequest(config, request, reqBody);
                IOUtils.copyLarge(remoteResponse.body(), response.getOutputStream());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        var reqCacheFolder = LocalCacheResolver.generateCacheFolderName(request, reqBody);
        var reqCacheParentFolder = LocalCacheResolver.resolve(new URL(config.getBaseUrl() + request.getRequestURI()));

        var reqCacheAbsoluteFolder = config.getLocalOverridesPath()
                .resolve(reqCacheParentFolder)
                .resolve(reqCacheFolder);

        if (!Files.exists(reqCacheAbsoluteFolder)) {
            reqCacheAbsoluteFolder.toFile().mkdirs();
        }

        var cachedRespHeadersPath = reqCacheAbsoluteFolder.resolve("response_headers");
        var cachedRespContentPath = reqCacheAbsoluteFolder.resolve("response_body");
        var cachedRespHeaders = new Properties();

        if (Files.exists(reqCacheAbsoluteFolder)) {
            if (Files.exists(cachedRespHeadersPath)) {
                try (var reader = new FileReader(cachedRespHeadersPath.toFile())) {
                    cachedRespHeaders.load(reader);
                    cachedRespHeaders.forEach((key, value) -> response.setHeader(key.toString(), value + ""));
                }
            }
            if (Files.exists(cachedRespContentPath)) {
                response.setHeader("Content-Length", String.valueOf(Files.size(cachedRespContentPath)));
                Files.copy(cachedRespContentPath, response.getOutputStream());
                return; // end response
            }
        }

        try {
            var remoteResponse = sendRemoteRequest(config, request, reqBody);
            remoteResponse.headers().map()
                    .forEach((k, v) -> cachedRespHeaders.put(k, v.size() > 0 ? v.get(0) : ""));

            // cache the response headers
            try (var os = new FileOutputStream(cachedRespHeadersPath.toFile())) {
                cachedRespHeaders.store(os, "cached headers @ " + new Date());
            }

            // cache the response in memory
            var respBody = String.join("\n", IOUtils.readLines(new InputStreamReader(remoteResponse.body())));
            respBody = respBody.replace(config.getBaseUrl(), "http://localhost:8090");
            var respBodyInBytes = respBody.getBytes(StandardCharsets.UTF_8);

            // cache the response
            try (var os = new FileOutputStream(cachedRespContentPath.toFile())) {
                IOUtils.copyLarge(new ByteArrayInputStream(respBodyInBytes), os);
            }

            // send the file to the http request
            response.setHeader("Content-Length", String.valueOf(Files.size(cachedRespContentPath)));
            IOUtils.copyLarge(new ByteArrayInputStream(respBodyInBytes), response.getOutputStream());

            // store the request details
            var cachedReqHeadersPath = reqCacheAbsoluteFolder.resolve("request_headers");
            var cachedReqContentPath = reqCacheAbsoluteFolder.resolve("request_body");
            var cachedReqHeaders = new Properties();

            request.getHeaderNames().asIterator()
                    .forEachRemaining(headerName -> {
                        var value = request.getHeader(headerName);
                        cachedReqHeaders.setProperty(headerName, value);
                    });

            try (var os = new FileOutputStream(cachedReqHeadersPath.toFile())) {
                cachedReqHeaders.store(os, "cached headers @ " + new Date());
            }

            try (OutputStream os = new FileOutputStream(cachedReqContentPath.toFile())) {
                var contentInBytes = reqBody.getBytes(StandardCharsets.UTF_8);
                IOUtils.copyLarge(new ByteArrayInputStream(contentInBytes), os);
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    private HttpResponse<InputStream> sendRemoteRequest(Config config, HttpServletRequest request, String reqBody) throws Exception {
        var client = HttpClient.newBuilder().build();
        var remoteUri = new URI(config.getBaseUrl() + request.getRequestURI() +
                (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

        var reqbldr = newBuilder()
                .uri(remoteUri);

        switch (request.getMethod().toLowerCase()) {
            case "get":
                reqbldr.GET();
                break;
            case "post":
                reqbldr.POST(BodyPublishers.ofString(reqBody));
                break;
            case "head":
                reqbldr.method("HEAD", BodyPublishers.noBody());
                break;
            default:
                // no-op
        }

        request.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> {
                    try {
                        var value = request.getHeader(headerName);
                        reqbldr.header(headerName, value);
                    } catch (Exception ex) {
                        // do nothing
                    }
                });

        return client.send(reqbldr.build(), HttpResponse.BodyHandlers.ofInputStream());
    }
}
