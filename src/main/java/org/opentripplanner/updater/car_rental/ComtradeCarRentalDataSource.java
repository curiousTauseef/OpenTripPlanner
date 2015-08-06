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
        station.x = node.path("geoLocation").path("longitude").asDouble();
        station.y = node.path("geoLocation").path("latitude").asDouble();
        station.name = new NonLocalizedString(node.path("name").asText());
        station.bikesAvailable = node.path("numberOfCars").asInt();
        station.spacesAvailable = node.path("parkingPlaces").asInt() - station.bikesAvailable;
        System.out.println(station);
        return station;
    }

}
