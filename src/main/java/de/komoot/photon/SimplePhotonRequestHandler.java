package de.komoot.photon;

import de.komoot.photon.query.PhotonQueryBuilder;
import de.komoot.photon.query.PhotonRequest;
import de.komoot.photon.searcher.PhotonSearcher;
import de.komoot.photon.searcher.PhotonSearcherFactory;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Sachin Dole on 2/12/2015.
 */
public class SimplePhotonRequestHandler implements PhotonRequestHandler {

    private final PhotonSearcherFactory searcherFactory = new PhotonSearcherFactory();
    @Override
    public String handle(PhotonRequest photonRequest) {
        PhotonQueryBuilder.builder(photonRequest.getQuery()).build();
        PhotonSearcher searcher = searcherFactory.getSearcher(photonRequest);
        List<JSONObject> results = searcher.searchStrict();
        if (results.size() == 0) {
            results = searcher.search();
        }
        return "";

    }
}
