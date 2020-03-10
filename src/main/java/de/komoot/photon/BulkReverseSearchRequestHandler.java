package de.komoot.photon;

import de.komoot.photon.query.BadRequestException;
import de.komoot.photon.query.ReverseRequest;
import de.komoot.photon.query.ReverseRequestFactory;
import de.komoot.photon.searcher.ReverseElasticsearchSearcher;
import de.komoot.photon.searcher.ReverseRequestHandler;
import de.komoot.photon.searcher.ReverseRequestHandlerFactory;
import de.komoot.photon.utils.ConvertToGeoJson;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.RouteImpl;

import java.util.*;

import static spark.Spark.halt;

/**
 * @author svantulden
 */
public class BulkReverseSearchRequestHandler<R extends ReverseRequest> extends RouteImpl {
    private final ReverseRequestFactory reverseRequestFactory;
    private final ReverseRequestHandlerFactory requestHandlerFactory;
    private final ConvertToGeoJson geoJsonConverter;

    BulkReverseSearchRequestHandler(String path, Client esNodeClient, String languages) {
        super(path);
        Set<String> supportedLanguages = new HashSet<>(Arrays.asList(languages.split(",")));
        this.reverseRequestFactory = new ReverseRequestFactory(supportedLanguages);
        this.geoJsonConverter = new ConvertToGeoJson();
        this.requestHandlerFactory = new ReverseRequestHandlerFactory(new ReverseElasticsearchSearcher(esNodeClient));
    }

    @Override
    public String handle(Request request, Response response) {

        List<String> responses = new ArrayList<>();

        List<R> photonRequests = null;
        try {
            photonRequests = reverseRequestFactory.createBulk(request);
        } catch (BadRequestException e) {
            JSONObject json = new JSONObject();
            json.put("message", e.getMessage());
            halt(e.getHttpStatus(), json.toString());
        }

        for (R photonRequest : photonRequests) {
            ReverseRequestHandler<R> handler = requestHandlerFactory.createHandler(photonRequest);
            List<JSONObject> results = handler.handle(photonRequest);
            JSONObject geoJsonResults = geoJsonConverter.convert(results);
            String s;
            if (request.queryParams("debug") != null)
                s = geoJsonResults.toString(4);
            else
                s = geoJsonResults.toString();
            responses.add(s);
        }

        return responses.toString();
    }
}
