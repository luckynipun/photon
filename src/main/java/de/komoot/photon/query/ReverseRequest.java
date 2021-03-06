package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.io.Serializable;

/**
 * @author svantulden
 */
public class ReverseRequest implements Serializable {
    private Point location;
    private Coordinate coordinate;
    private String language;
    private Double radius;
    private Integer limit;
    private String queryStringFilter;
    private Boolean locationDistanceSort = true;

    public ReverseRequest(Point location, Coordinate coordinate, String language, Double radius, String queryStringFilter, Integer limit, Boolean locationDistanceSort) {
        this.location = location;
        this.coordinate = coordinate;
        this.language = language;
        this.radius = radius;
        this.limit = limit;
        this.queryStringFilter = queryStringFilter;
        this.locationDistanceSort = locationDistanceSort;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Point getLocation() {
        if (this.location == null && this.coordinate != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            return geometryFactory.createPoint(this.coordinate);
        }
        return this.location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getQueryStringFilter() {
        return queryStringFilter;
    }

    public void setQueryStringFilter(String queryStringFilter) {
        this.queryStringFilter = queryStringFilter;
    }

    public Boolean getLocationDistanceSort() {
        return locationDistanceSort;
    }

    public void setLocationDistanceSort(Boolean locationDistanceSort) {
        this.locationDistanceSort = locationDistanceSort;
    }
}
