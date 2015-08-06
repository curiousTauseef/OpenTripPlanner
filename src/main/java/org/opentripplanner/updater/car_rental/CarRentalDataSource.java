package org.opentripplanner.updater.car_rental;

import java.util.List;

import org.opentripplanner.routing.car_rental.CarRentalStation;

public interface CarRentalDataSource {

    boolean update();

    List<CarRentalStation> getStations();

}