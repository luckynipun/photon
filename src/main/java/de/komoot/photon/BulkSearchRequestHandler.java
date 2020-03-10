package de.komoot.photon;

import de.komoot.photon.query.BadRequestException;
import de.komoot.photon.query.PhotonRequest;
import de.komoot.photon.query.PhotonRequestFactory;
import de.komoot.photon.searcher.BaseElasticsearchSearcher;
import de.komoot.photon.searcher.PhotonRequestHandler;
import de.komoot.photon.searcher.PhotonRequestHandlerFactory;
import de.komoot.photon.utils.ConvertToGeoJson;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.RouteImpl;

import java.util.*;

import static spark.Spark.halt;

/**
 * Modified by Cloudleaf on 6/03/2020.
 */
public class BulkSearchRequestHandler<R extends PhotonRequest> extends RouteImpl {
    private static final String DEBUG_PARAMETER = "debug";

    private final PhotonRequestFactory photonRequestFactory;
    private final PhotonRequestHandlerFactory requestHandlerFactory;
    private final ConvertToGeoJson geoJsonConverter;

    BulkSearchRequestHandler(String path, Client esNodeClient, String languages) {
        super(path);
        Set<String> supportedLanguages = new HashSet<String>(Arrays.asList(languages.split(",")));
        this.photonRequestFactory = new PhotonRequestFactory(supportedLanguages);
        this.geoJsonConverter = new ConvertToGeoJson();
        this.requestHandlerFactory = new PhotonRequestHandlerFactory(new BaseElasticsearchSearcher(esNodeClient));
    }

    @Override
    public String handle(Request request, Response response) {

        List<String> responses = new ArrayList<>();

        List<R> photonRequests = null;
        try {
            photonRequests = photonRequestFactory.createBulk(request);
        } catch (BadRequestException e) {
            JSONObject json = new JSONObject();
            json.put("message", e.getMessage());
            halt(e.getHttpStatus(), json.toString());
        }

        for (R photonRequest : photonRequests) {
            PhotonRequestHandler<R> handler = requestHandlerFactory.createHandler(photonRequest);
            List<JSONObject> results = handler.handle(photonRequest);
            JSONObject geoJsonResults = geoJsonConverter.convert(results);
            if (request.queryParams(DEBUG_PARAMETER) != null) {
                JSONObject debug = new JSONObject();
                debug.put("query", new JSONObject(handler.dumpQuery(photonRequest)));
                geoJsonResults.put(DEBUG_PARAMETER, debug);
                responses.add(geoJsonResults.toString(4));
            } else
                responses.add(geoJsonResults.toString());
        }

        return responses.toString();
    }

}