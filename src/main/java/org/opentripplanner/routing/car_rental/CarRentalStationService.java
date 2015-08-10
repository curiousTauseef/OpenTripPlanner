package org.opentripplanner.routing.car_rental;

/**
 * Created by klemen on 10.8.2015.
 */

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.opentripplanner.routing.car_park.CarPark;

public class CarRentalStationService implements Serializable {
    private static final long serialVersionUID = -7222992939159246764L;

    private Set<CarRentalStation> carRentalStations = new HashSet<CarRentalStation>();

    private Set<CarPark> carParks = new HashSet<CarPark>();

    public Collection<CarRentalStation> getCarRentalStations() {
        return carRentalStations;
    }

    public void addCarRentalStation(CarRentalStation carRentalStation) {
        carRentalStations.remove(carRentalStation);
        carRentalStations.add(carRentalStation);
    }

    public void removeCarRentalStation(CarRentalStation carRentalStation) {
        carRentalStations.remove(carRentalStation);
    }

    public Collection<CarPark> getCarParks() {
        return carParks;
    }

    public void addCarPark(CarPark carPark) {
        carParks.remove(carPark);
        carParks.add(carPark);
    }

    public void removeCarPark(CarPark carPark) {
        carParks.remove(carPark);
    }


}
