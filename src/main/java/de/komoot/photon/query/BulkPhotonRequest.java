package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Cloudleaf
 */
public class BulkPhotonRequest extends PhotonRequest implements Serializable {

    private List<String> queries;

    public BulkPhotonRequest(String query, int limit, Envelope bbox, Point locationForBias, Coordinate coordinateForBias, double scale, String language, List<String> queries) {
        super(query, limit, bbox, locationForBias, coordinateForBias, scale, language);
        this.queries = queries;
    }

    public List<String> getQueries() {
        return queries;
    }

    public void setQueries(List<String> queries) {
        this.queries = queries;
    }

}
