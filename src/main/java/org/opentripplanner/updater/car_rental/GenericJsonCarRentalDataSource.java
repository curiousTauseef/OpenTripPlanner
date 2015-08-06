package org.opentripplanner.updater.car_rental;

/**
 * Created by Klemen Kozelj on 2.8.2015.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.updater.JsonConfigurable;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GenericJsonCarRentalDataSource implements CarRentalDataSource, JsonConfigurable{

    private static final Logger log = LoggerFactory.getLogger(GenericJsonCarRentalDataSource.class);
    private String url;
    private String apiKey;

    private String jsonParsePath;

    ArrayList<CarRentalStation> stations = new ArrayList<CarRentalStation>();

    public GenericJsonCarRentalDataSource(String jsonPath) {
        jsonParsePath = jsonPath;
        apiKey= null;
    }

    public GenericJsonCarRentalDataSource(String jsonPath, String apiKeyValue) {
        jsonParsePath = jsonPath;
        apiKey = apiKeyValue;
    }

    public GenericJsonCarRentalDataSource() {
        jsonParsePath = "";
    }

    @Override
    public boolean update() {
        try {
            InputStream data = HttpUtils.getData(url, "ApiKey", apiKey);
            if (data == null) {
                log.warn("Failed to get data from url " + url);
                return false;
            }
            parseJSON(data);
            data.close();
        } catch (IllegalArgumentException e) {
            log.warn("Error parsing car rental feed from " + url, e);
            return false;
        } catch (JsonProcessingException e) {
            log.warn("Error parsing car rental feed from " + url + "(bad JSON of some sort)", e);
            return false;
        } catch (IOException e) {
            log.warn("Error reading car rental feed from " + url, e);
            return false;
        }
        return true;
    }

    private void parseJSON(InputStream dataStream) throws JsonProcessingException, IllegalArgumentException,
            IOException {

        ArrayList<CarRentalStation> out = new ArrayList<CarRentalStation>();

        String rentalString = convertStreamToString(dataStream);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(rentalString);

        if (!jsonParsePath.equals("")) {
            String delimiter = "/";
            String[] parseElement = jsonParsePath.split(delimiter);
            for(int i =0; i < parseElement.length ; i++) {
                rootNode = rootNode.path(parseElement[i]);
            }

            if (rootNode.isMissingNode()) {
                throw new IllegalArgumentException("Could not find jSON elements " + jsonParsePath);
            }
        }

        for (int i = 0; i < rootNode.size(); i++) {
            JsonNode node = rootNode.get(i);
            if (node == null) {
                continue;
            }
            CarRentalStation brstation = makeStation(node, i + 1);
            if (brstation != null)
                out.add(brstation);
        }
        synchronized(this) {
            stations = out;
        }
    }

    private String convertStreamToString(InputStream is) {
        java.util.Scanner scanner = null;
        String result="";
        try {

            scanner = new java.util.Scanner(is).useDelimiter("\\A");
            result = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        }
        finally
        {
            if(scanner!=null)
                scanner.close();
        }
        return result;

    }

    @Override
    public synchronized List<CarRentalStation> getStations() {
        return stations;
    }

    public String getUrl() {
        return url;
    }

    public abstract CarRentalStation makeStation(JsonNode rentalStationNode, int id);

    @Override
    public String toString() {
        return getClass().getName() + "(" + url + ")";
    }


    @Override
    public void configure (Graph graph, JsonNode jsonNode) {
        String url = jsonNode.path("url").asText();
        if (url == null) {
            throw new IllegalArgumentException("Missing mandatory 'url' configuration.");
        }
        this.url = url;
    }
}
