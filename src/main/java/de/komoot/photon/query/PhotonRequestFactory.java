package de.komoot.photon.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import spark.QueryParamsMap;
import spark.Request;

import java.util.*;

/**
 * A factory that creates a {@link PhotonRequest} from a {@link Request web request}
 * Created by Sachin Dole on 2/12/2015.
 */
public class PhotonRequestFactory {
    private final LanguageChecker languageChecker;
    private final static LocationParamConverter optionalLocationParamConverter = new LocationParamConverter(false);
    private final BoundingBoxParamConverter bboxParamConverter;

    protected static HashSet<String> m_hsRequestQueryParams = new HashSet<>(Arrays.asList("lang", "q", "lon", "lat",
            "limit", "osm_tag", "location_bias_scale", "bbox", "debug"));
    protected static HashSet<String> m_hsBulkRequestQueryParams = new HashSet<>(Arrays.asList("lang", "qlist", "lon", "lat",
            "limit", "osm_tag", "location_bias_scale", "bbox", "debug"));

    public PhotonRequestFactory(Set<String> supportedLanguages) {
        this.languageChecker = new LanguageChecker(supportedLanguages);
        this.bboxParamConverter = new BoundingBoxParamConverter();
    }

    public <R extends PhotonRequest> R createWithBody(Request webRequest) throws BadRequestException {
        String body = webRequest.body();
        Gson gson = new GsonBuilder().create();
        PhotonRequest request = gson.fromJson(body, PhotonRequest.class);

        String language = checkLanguage(request);

        String query = request.getQuery();
        if (query == null) throw new BadRequestException(400, "missing param 'query'");

        Integer limit = checkLimit(request);

        Point locationForBias = request.getLocationForBias();
        optionalLocationParamConverter.checkLatLonLimits(locationForBias.getCoordinate());

        Envelope bbox = request.getBbox();
        bboxParamConverter.checkBbox(bbox);

        double scale = checkScale(request);

        //osm_tag should always be in Params
        QueryParamsMap tagFiltersQueryMap = webRequest.queryMap("osm_tag");
        if (!new CheckIfFilteredRequest().execute(tagFiltersQueryMap)) {
            return (R) new PhotonRequest(query, limit, bbox, locationForBias, scale, language);
        }
        FilteredPhotonRequest photonRequest = new FilteredPhotonRequest(query, limit, bbox, locationForBias, scale, language);
        String[] tagFilters = tagFiltersQueryMap.values();
        setUpTagFilters(photonRequest, tagFilters);

        return (R) photonRequest;
    }

    public <R extends PhotonRequest> R create(Request webRequest) throws BadRequestException {

        checkAllowedParameters(webRequest);

        String language = checkLanguage(webRequest);

        String query = webRequest.queryParams("q");
        if (query == null) throw new BadRequestException(400, "missing search term 'q': /?q=berlin");

        Integer limit = checkLimit(webRequest);

        Point locationForBias = optionalLocationParamConverter.apply(webRequest);
        Envelope bbox = bboxParamConverter.apply(webRequest);

        double scale = checkScale(webRequest);

        QueryParamsMap tagFiltersQueryMap = webRequest.queryMap("osm_tag");
        if (!new CheckIfFilteredRequest().execute(tagFiltersQueryMap)) {
            return (R) new PhotonRequest(query, limit, bbox, locationForBias, scale, language);
        }
        FilteredPhotonRequest photonRequest = new FilteredPhotonRequest(query, limit, bbox, locationForBias, scale, language);
        String[] tagFilters = tagFiltersQueryMap.values();
        setUpTagFilters(photonRequest, tagFilters);

        return (R) photonRequest;
    }

    public <R extends PhotonRequest> List<R> createBulkWithBody(Request webRequest) throws BadRequestException {
        String body = webRequest.body();
        Gson gson = new GsonBuilder().create();
        BulkPhotonRequest request = gson.fromJson(body, BulkPhotonRequest.class);

        List<R> results = new ArrayList<>();

        String language = checkLanguage(request);

        List<String> queries = request.getQueries();
        if (queries == null || queries.size() == 0) throw new BadRequestException(400, "missing queries");

        Integer limit = checkLimit(request);

        Point locationForBias = request.getLocationForBias();
        optionalLocationParamConverter.checkLatLonLimits(locationForBias.getCoordinate());

        Envelope bbox = request.getBbox();
        bboxParamConverter.checkBbox(bbox);

        double scale = checkScale(request);

        //osm_tag should always be in Params
        QueryParamsMap tagFiltersQueryMap = webRequest.queryMap("osm_tag");
        if (!new CheckIfFilteredRequest().execute(tagFiltersQueryMap)) {
            for (String query : queries)
                results.add((R) new PhotonRequest(query, limit, bbox, locationForBias, scale, language));
            return results;
        }

        for (String query : queries) {
            FilteredPhotonRequest photonRequest = new FilteredPhotonRequest(query, limit, bbox, locationForBias, scale, language);
            String[] tagFilters = tagFiltersQueryMap.values();
            setUpTagFilters(photonRequest, tagFilters);
            results.add((R) photonRequest);
        }

        return results;
    }

    public <R extends PhotonRequest> List<R> createBulk(Request webRequest) throws BadRequestException {
        List<R> results = new ArrayList<>();

        checkAllowedParameters(webRequest);

        String language = checkLanguage(webRequest);

        String queriesParam = webRequest.queryParams("q");
        if (queriesParam == null) throw new BadRequestException(400, "missing search term 'q': /?q=berlin");

        /**
         * Below line of code is crucial for bulk GeoCoding API, The addresses are split by underscore("_")
         * Hence the individual address cannot contain underscore("_")
         * */
        List<String> queries = Arrays.asList(queriesParam.split("_"));

        Integer limit = checkLimit(webRequest);

        Point locationForBias = optionalLocationParamConverter.apply(webRequest);
        Envelope bbox = bboxParamConverter.apply(webRequest);

        double scale = checkScale(webRequest);

        QueryParamsMap tagFiltersQueryMap = webRequest.queryMap("osm_tag");
        if (!new CheckIfFilteredRequest().execute(tagFiltersQueryMap)) {
            for (String query : queries)
                results.add((R) new PhotonRequest(query, limit, bbox, locationForBias, scale, language));
            return results;
        }

        for (String query : queries) {
            FilteredPhotonRequest photonRequest = new FilteredPhotonRequest(query, limit, bbox, locationForBias, scale, language);
            String[] tagFilters = tagFiltersQueryMap.values();
            setUpTagFilters(photonRequest, tagFilters);
            results.add((R) photonRequest);
        }

        return results;
    }

    //This method is for Query Params
    private Integer checkLimit(Request webRequest) {
        Integer limit;
        try {
            limit = Integer.valueOf(webRequest.queryParams("limit"));
        } catch (NumberFormatException e) {
            limit = 15;
        }
        return limit;
    }

    //This method is for Request Body
    private Integer checkLimit(PhotonRequest request) {
        return request.getLimit() == 0 ? 15 : request.getLimit();
    }

    //This method is for Query Params
    private double checkScale(Request webRequest) throws BadRequestException {
        // don't use too high default value, see #306
        double scale = 1.6;
        String scaleStr = webRequest.queryParams("location_bias_scale");
        if (scaleStr != null && !scaleStr.isEmpty())
            try {
                scale = Double.parseDouble(scaleStr);
            } catch (Exception nfe) {
                throw new BadRequestException(400, "invalid parameter 'location_bias_scale' must be a number");
            }
        return scale;
    }

    //This method is for Request Body
    private double checkScale(PhotonRequest request) throws BadRequestException {
        // don't use too high default value, see #306
        return request.getScale() == 0d ? 1.6 : request.getScale();
    }

    //This method is for Query Params
    private String checkLanguage(Request webRequest) throws BadRequestException {
        String language = webRequest.queryParams("lang");
        language = language == null ? "en" : language;
        languageChecker.apply(language);
        return language;
    }

    //This method is for Request Body
    private String checkLanguage(PhotonRequest request) throws BadRequestException {
        String language = request.getLanguage();
        language = language == null ? "en" : language;
        languageChecker.apply(language);
        return language;
    }

    private void checkAllowedParameters(Request webRequest) throws BadRequestException {
        for (String queryParam : webRequest.queryParams())
            if (!m_hsRequestQueryParams.contains(queryParam))
                throw new BadRequestException(400, "unknown query parameter '" + queryParam + "'.  Allowed parameters are: " + m_hsRequestQueryParams);
    }

    private void setUpTagFilters(FilteredPhotonRequest request, String[] tagFilters) {
        for (String tagFilter : tagFilters) {
            if (tagFilter.contains(":")) {
                //might be tag and value OR just value.
                if (tagFilter.startsWith("!")) {
                    //exclude
                    String keyValueCandidate = tagFilter.substring(1);
                    if (keyValueCandidate.startsWith(":")) {
                        //just value
                        request.notValues(keyValueCandidate.substring(1));
                    } else {
                        //key and value
                        String[] keyAndValue = keyValueCandidate.split(":");
                        String excludeKey = keyAndValue[0];
                        String value = keyAndValue[1].startsWith("!") ? keyAndValue[1].substring(1) : keyAndValue[1];
                        Set<String> valuesToExclude = request.notTags().get(excludeKey);
                        if (valuesToExclude == null) valuesToExclude = new HashSet<String>();
                        valuesToExclude.add(value);
                        request.notTags(excludeKey, valuesToExclude);
                    }
                } else {
                    //include key, not sure about value
                    if (tagFilter.startsWith(":")) {
                        //just value

                        String valueCandidate = tagFilter.substring(1);
                        if (valueCandidate.startsWith("!")) {
                            //exclude value
                            request.notValues(valueCandidate.substring(1));
                        } else {
                            //include value
                            request.values(valueCandidate);
                        }
                    } else {
                        //key and value
                        String[] keyAndValue = tagFilter.split(":");

                        String key = keyAndValue[0];
                        String value = keyAndValue[1];
                        if (value.startsWith("!")) {
                            //exclude value
                            Set<String> tagKeysValuesNotIncluded = request.tagNotValues().get(key);
                            if (tagKeysValuesNotIncluded == null) tagKeysValuesNotIncluded = new HashSet<String>();
                            tagKeysValuesNotIncluded.add(value.substring(1));
                            request.tagNotValues(key, tagKeysValuesNotIncluded);
                        } else {
                            //include value
                            Set<String> valuesToInclude = request.tags().get(key);
                            if (valuesToInclude == null) valuesToInclude = new HashSet<String>();
                            valuesToInclude.add(value);
                            request.tags(key, valuesToInclude);
                        }
                    }
                }
            } else {
                //only tag
                if (tagFilter.startsWith("!")) {
                    request.notKeys(tagFilter.substring(1));
                } else {
                    request.keys(tagFilter);
                }
            }
        }
    }
}
