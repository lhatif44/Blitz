package com.blitz.ingestion.client;

import com.blitz.exception.IngestionException;
import com.blitz.ingestion.csv.CsvTable;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

// downloads CSV assets published to the nflverse-data GitHub releases
@Component
public class NflverseClient {

    private static final String BASE_URL = "https://github.com/nflverse/nflverse-data/releases/download";

    // downloads an asset that is always expected to exist (teams, players, PFR advanced stats)
    public CsvTable download(String releaseTag, String fileName) {
        return tryDownload(releaseTag, fileName)
                .orElseThrow(() -> new IngestionException("Asset not found: " + releaseTag + "/" + fileName));
    }

    // downloads a per-season asset that may not be published yet (e.g. the current season before its first game)
    public Optional<CsvTable> tryDownload(String releaseTag, String fileName) {
        String url = BASE_URL + "/" + releaseTag + "/" + fileName;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            return client.execute(request, response -> {
                if (response.getCode() == 404) {
                    return Optional.empty();
                }
                if (response.getCode() >= 300) {
                    throw new IngestionException("Failed to download " + url + " — HTTP " + response.getCode());
                }
                try (InputStream body = response.getEntity().getContent()) {
                    return Optional.of(CsvTable.parse(body));
                }
            });
        } catch (IOException e) {
            throw new IngestionException("Failed to download " + url, e);
        }
    }
}
