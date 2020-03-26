package de.komoot.photon.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Point;
import spark.Request;

import java.util.*;

/**
 * @author svantulden
 */
public class ReverseRequestFactory {
    private final LanguageChecker languageChecker;
    private final static LocationParamConverter mandatoryLocationParamConverter = new LocationParamConverter(true);
    private final static BulkLocationParamConverter bulkMandatoryLocationParamConverter = new BulkLocationParamConverter(true);

    protected static HashSet<String> m_hsRequestQueryParams = new HashSet<>(Arrays.asList("lang", "lon", "lat", "radius", "query_string_filter", "distance_sort", "limit"));

    public ReverseRequestFactory(Set<String> supportedLanguages) {
        this.languageChecker = new LanguageChecker(supportedLanguages);
    }

    public <R extends ReverseRequest> R createWithBody(Request webRequest) throws BadRequestException {
        String body = webRequest.body();
        Gson gson = new GsonBuilder().create();
        ReverseRequest request = gson.fromJson(body, ReverseRequest.class);

        String language = checkLanguage(request);

        Point location = request.getLocation();
        mandatoryLocationParamConverter.checkLatLonLimits(location.getCoordinate());

        Double radius = checkRadius(request);

        Boolean locationDistanceSort = request.getLocationDistanceSort();

        Integer limit = checkLimit(request);

        String queryStringFilter = request.getQueryStringFilter();

        ReverseRequest reverseRequest = new ReverseRequest(location, language, radius, queryStringFilter, limit, locationDistanceSort);
        return (R) reverseRequest;

    }

    public <R extends ReverseRequest> R create(Request webRequest) throws BadRequestException {
        checkAllowedParams(webRequest);

        String language = checkLanguage(webRequest);

        Point location = mandatoryLocationParamConverter.apply(webRequest);

        Double radius = checkRadius(webRequest);

        Boolean locationDistanceSort = checkLocationDistanceSort(webRequest);

        Integer limit = checkLimit(webRequest);

        String queryStringFilter = webRequest.queryParams("query_string_filter");

        ReverseRequest reverseRequest = new ReverseRequest(location, language, radius, queryStringFilter, limit, locationDistanceSort);
        return (R) reverseRequest;
    }

    public <R extends ReverseRequest> List<R> createBulkWithBody(Request webRequest) throws BadRequestException {
        String body = webRequest.body();
        Gson gson = new GsonBuilder().create();
        BulkReverseRequest request = gson.fromJson(body, BulkReverseRequest.class);

        String language = checkLanguage(request);


        for (Point location: request.getLocations())
            mandatoryLocationParamConverter.checkLatLonLimits(location.getCoordinate());

        Double radius = checkRadius(request);

        Boolean locationDistanceSort = request.getLocationDistanceSort();

        Integer limit = checkLimit(request);

        String queryStringFilter = request.getQueryStringFilter();

        List<R> result = new ArrayList<>();

        for (Point location : request.getLocations()) {
            ReverseRequest reverseRequest = new ReverseRequest(location, language, radius, queryStringFilter, limit, locationDistanceSort);
            result.add((R) reverseRequest);
        }

        return result;
    }

    public <R extends ReverseRequest> List<R> createBulk(Request webRequest) throws BadRequestException {
        checkAllowedParams(webRequest);

        String language = checkLanguage(webRequest);

        List<Point> locations = bulkMandatoryLocationParamConverter.apply(webRequest);

        Double radius = checkRadius(webRequest);

        Boolean locationDistanceSort = checkLocationDistanceSort(webRequest);

        Integer limit = checkLimit(webRequest);

        String queryStringFilter = webRequest.queryParams("query_string_filter");

        List<R> result = new ArrayList<>();

        for (Point location : locations) {
            ReverseRequest reverseRequest = new ReverseRequest(location, language, radius, queryStringFilter, limit, locationDistanceSort);
            result.add((R) reverseRequest);
        }

        return result;
    }

    //This method is for Query Params
    private Boolean checkLocationDistanceSort(Request webRequest) throws BadRequestException {
        Boolean locationDistanceSort;
        try {
            locationDistanceSort = Boolean.valueOf(webRequest.queryParamOrDefault("distance_sort", "true"));
        } catch (Exception nfe) {
            throw new BadRequestException(400, "invalid parameter 'distance_sort', can only be true or false");
        }
        return locationDistanceSort;
    }

    //This method is for Query Params
    private String checkLanguage(Request webRequest) throws BadRequestException {
        String language = webRequest.queryParams("lang");
        language = language == null ? "en" : language;
        languageChecker.apply(language);
        return language;
    }

    //This method is for Request Body
    private String checkLanguage(ReverseRequest request) throws BadRequestException {
        String language = request.getLanguage();
        language = language == null ? "en" : language;
        languageChecker.apply(language);
        return language;
    }

    //This method is for Query Params
    private Integer checkLimit(Request webRequest) throws BadRequestException {
        Integer limit = 1;
        String limitParam = webRequest.queryParams("limit");
        if (limitParam != null) {
            try {
                limit = Integer.valueOf(limitParam);
            } catch (Exception nfe) {
                throw new BadRequestException(400, "invalid search term 'limit', expected an integer.");
            }
            if (limit <= 0) {
                throw new BadRequestException(400, "invalid search term 'limit', expected a strictly positive integer.");
            } else {
                // limit number of results to 50
                limit = Math.min(limit, 50);
            }
        }
        return limit;
    }

    //This method is for Request Body
    private Integer checkLimit(ReverseRequest request) throws BadRequestException {
        Integer limit = request.getLimit() == 0 ? 1 : request.getLimit();
        if (limit <= 0) {
            throw new BadRequestException(400, "invalid search term 'limit', expected a strictly positive integer.");
        } else {
            // limit number of results to 50
            limit = Math.min(limit, 50);
        }
        return limit;
    }

    //This method is for Query Params
    private Double checkRadius(Request webRequest) throws BadRequestException {
        Double radius = 1d;
        String radiusParam = webRequest.queryParams("radius");
        if (radiusParam != null) {
            try {
                radius = Double.valueOf(radiusParam);
            } catch (Exception nfe) {
                throw new BadRequestException(400, "invalid search term 'radius', expected a number.");
            }
            if (radius <= 0) {
                throw new BadRequestException(400, "invalid search term 'radius', expected a strictly positive number.");
            } else {
                // limit search radius to 5000km
                radius = Math.min(radius, 5000d);
            }
        }
        return radius;
    }

    //This method is for Request Body
    private Double checkRadius(ReverseRequest request) throws BadRequestException {
        Double radius = request.getRadius() == 0d ? 1d : request.getRadius();
        if (radius <= 0) {
            throw new BadRequestException(400, "invalid search term 'radius', expected a strictly positive number.");
        } else {
            // limit search radius to 5000km
            radius = Math.min(radius, 5000d);
        }
        return radius;
    }

    //This method is for Query Params
    private void checkAllowedParams(Request webRequest) throws BadRequestException {
        for (String queryParam : webRequest.queryParams()) {
            if (!m_hsRequestQueryParams.contains(queryParam))
                throw new BadRequestException(400, "unknown query parameter '" + queryParam + "'.  Allowed parameters are: " + m_hsRequestQueryParams);
        }
    }

}
