package org.opentripplanner.updater.car_rental;

/**
 * Created by klemen on 6.8.2015.
 */

import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.updater.bike_rental.GenericJsonBikeRentalDataSource;
import org.opentripplanner.util.NonLocalizedString;

import com.fasterxml.jackson.databind.JsonNode;


public class ComtradeCarRentalDataSource extends GenericJsonBikeRentalDataSource {

    public ComtradeCarRentalDataSource() {
        super("");
    }


    public CarRentalStation makeStation(JsonNode node) {
        CarRentalStation station = new CarRentalStation();
        station.id = String.format("%d", node.path("number").asInt());
        station.x = node.path("position").path("lng").asDouble();
        station.y = node.path("position").path("lat").asDouble();
        station.name = new NonLocalizedString(node.path("name").asText());
        station.bikesAvailable = node.path("available_bikes").asInt();
        station.spacesAvailable = node.path("available_bike_stands").asInt();
        return station;
    }

}
