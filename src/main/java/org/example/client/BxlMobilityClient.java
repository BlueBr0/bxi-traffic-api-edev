package org.example.client;

import org.example.exceptions.SnapshotUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

//Client Http
//Retourne le JSON brut, le parsing se fait dans la classe SnapshotParser.
@Component
public class BxlMobilityClient {

    private final Logger logger = LoggerFactory.getLogger(BxlMobilityClient.class);

    private final String url;
    private final RestClient restClient;

    public BxlMobilityClient(@Value("${bxl.api.url}") String url) {
        this.url = url;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public String fetchLatestSnapshotJson() {
        try {
            // Octets bruts : la source répond en gzip, ce que le client ne décompresse pas seul.
            byte[] body = restClient.get()
                    .uri(URI.create(url))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(byte[].class);
            if (body == null || body.length == 0) {
                throw new SnapshotUnavailableException("Réponse vide de la source");
            }
            String json = decode(body);
            logger.debug("Réponse brute de la source : {}", json);
            return json;
        } catch (RestClientException e) {
            throw new SnapshotUnavailableException("Source Brussels Mobility injoignable : " + e.getMessage(), e);
        }
    }

    private static String decode(byte[] body) {
        try {
            boolean gzip = body.length > 2 && (body[0] & 0xFF) == 0x1F && (body[1] & 0xFF) == 0x8B;
            if (gzip) {
                try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(body))) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            return new String(body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SnapshotUnavailableException("Réponse illisible : " + e.getMessage(), e);
        }
    }
}
