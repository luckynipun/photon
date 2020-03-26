package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;

/**
 * Created by Sachin Dole on 2/12/2015.
 */
public class PhotonRequest implements Serializable {
    private String query;
    private Integer limit;
    private Point locationForBias;
    private String language;
    private final double scale;
    private Envelope bbox;

    public PhotonRequest(String query, int limit, Envelope bbox, Point locationForBias, double scale, String language) {
        this.query = query;
        this.limit = limit;
        this.locationForBias = locationForBias;
        this.scale = scale;
        this.language = language;
        this.bbox = bbox;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Point getLocationForBias() {
        return locationForBias;
    }

    public void setLocationForBias(Point locationForBias) {
        this.locationForBias = locationForBias;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getScale() {
        return scale;
    }

    public Envelope getBbox() {
        return bbox;
    }

    public void setBbox(Envelope bbox) {
        this.bbox = bbox;
    }

    public double getScaleForBias() {
        return scale;
    }
}
