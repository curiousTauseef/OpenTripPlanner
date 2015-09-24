package org.opentripplanner.updater.car_rental;

/**
 * Created by klemen on 6.8.2015.
 */

import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.util.NonLocalizedString;
import com.fasterxml.jackson.databind.JsonNode;


public class ComtradeCarRentalDataSource extends GenericJsonCarRentalDataSource {

    public ComtradeCarRentalDataSource() {
        super("");
    }

    public CarRentalStation makeStation(JsonNode node, int id) {
        CarRentalStation station = new CarRentalStation();
        station.id = Integer.toString(id);
        station.x = node.path("geoLocation").path("lng").asDouble();
        station.y = node.path("geoLocation").path("lat").asDouble();
        station.name = new NonLocalizedString(node.path("name").asText());
        station.bikesAvailable = node.path("reservableCars").asInt();
        station.spacesAvailable = node.path("parkingPlaces").asInt() - station.bikesAvailable;
        return station;
    }

}
