package de.komoot.photon.query;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import de.komoot.photon.utils.Function;
import spark.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Converts lon/lat parameter into location and validates the given coordinates.
 * Created by Holger Bruch on 10/13/2018.
 */
public class BulkLocationParamConverter implements Function<Request, List<Point>, BadRequestException> {
    private final static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private boolean mandatory;

    public BulkLocationParamConverter(boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    @Override
    public List<Point> apply(Request webRequest) throws BadRequestException {
        List<Point> result = new ArrayList<>();
        String lonsParam = webRequest.queryParams("lon");
        String latsParam = webRequest.queryParams("lat");

        if (!mandatory && lonsParam == null && latsParam == null)
            return null;

        List<String> lons = new ArrayList<>();
        List<String> lats = new ArrayList<>();

        if (lonsParam != null && latsParam != null) {
            lons = Arrays.asList(lonsParam.split(","));
            lats = Arrays.asList(latsParam.split(","));
        }

        if (lats.size() != lons.size())
            throw new BadRequestException(400, "number of lat's and lon's don't match");

        for (int i = 0; i < lats.size(); i++) {
            try {
                Double lon = Double.valueOf(lons.get(i));
                if (lon > 180.0 || lon < -180.00) {
                    throw new BadRequestException(400, "invalid search term 'lon', expected number >= -180.0 and <= 180.0");
                }
                Double lat = Double.valueOf(lats.get(i));
                if (lat > 90.0 || lat < -90.00) {
                    throw new BadRequestException(400, "invalid search term 'lat', expected number >= -90.0 and <= 90.0");
                }
                Point location = geometryFactory.createPoint(new Coordinate(lon, lat));
                result.add(location);
            } catch (NullPointerException | NumberFormatException e) {
                throw new BadRequestException(400, "invalid search term 'lat' and/or 'lon', try instead lat=51.5&lon=8.0");
            }
        }

        return result;
    }
}
