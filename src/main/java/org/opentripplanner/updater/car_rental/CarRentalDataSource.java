package org.opentripplanner.updater.car_rental;

import java.util.List;

import org.opentripplanner.routing.car_rental.CarRentalStation;

public interface CarRentalDataSource {

    /** Update the data from the source;
     * returns true if there might have been changes */
    boolean update();

    List<CarRentalStation> getStations();

}