package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;
import java.util.List;

/**
 * @author Cloudleaf
 */
public class BulkReverseRequest extends ReverseRequest implements Serializable {

    private List<Coordinate> coordinates;

    public BulkReverseRequest(Point location, Coordinate coordinate, String language, Double radius, String queryStringFilter, Integer limit, Boolean locationDistanceSort, List<Coordinate> coordinates) {
        super(location, coordinate, language, radius, queryStringFilter, limit, locationDistanceSort);
        this.coordinates = coordinates;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }
}
