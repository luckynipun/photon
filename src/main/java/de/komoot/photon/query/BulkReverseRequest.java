package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;
import java.util.List;

/**
 * @author Cloudleaf
 */
public class BulkReverseRequest extends ReverseRequest implements Serializable {

    private List<Point> locations;

    public BulkReverseRequest(Point location, String language, Double radius, String queryStringFilter, Integer limit, Boolean locationDistanceSort, List<Point> locations) {
        super(location, language, radius, queryStringFilter, limit, locationDistanceSort);
        this.locations = locations;
    }

    public List<Point> getLocations() {
        return locations;
    }

    public void setLocations(List<Point> locations) {
        this.locations = locations;
    }
}
