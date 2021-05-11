package com.example.application.repository;

import com.example.application.models.VolumesResponse;
import com.example.application.service.AsyncRestCallback;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

@Repository
public class BookRepository {



    //READ paged
    public void getBooks(AsyncRestCallback<VolumesResponse> callback, String search, int maxResults,
                         int startIndex) {


        String raw = "https://api.nytimes.com/svc/movies/v2/reviews/search.json?query=%s&api-key=%s";
        String key = "R9Ia2GvsfU0ztkfIzpZD0BxSNRPmgbJy";
        String formatted = String.format(raw, search, key);
        WebClient.RequestHeadersSpec<?> spec = WebClient.create().get().uri(formatted);

        spec.retrieve().toEntity(VolumesResponse.class).subscribe(result -> {

            final VolumesResponse volumesResponse = result.getBody();

            if (null == volumesResponse.getResults()) return;

            callback.operationFinished(volumesResponse);

        });

    }
}
