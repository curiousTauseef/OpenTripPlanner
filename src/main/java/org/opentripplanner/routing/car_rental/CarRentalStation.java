package org.opentripplanner.routing.car_rental;

/**
 * Created by klemen on 6.8.2015.
 */

import org.opentripplanner.routing.bike_rental.BikeRentalStation;

import java.util.Locale;


public class CarRentalStation extends BikeRentalStation {

    public CarRentalStation() {
        super();
    }

    public boolean equals(Object o) {
        if (!(o instanceof CarRentalStation)) {
            return false;
        }
        CarRentalStation other = (CarRentalStation) o;
        return other.id.equals(id);
    }

    public CarRentalStation clone() {
        return (CarRentalStation) super.clone();
    }

    public String toString() {
        return String.format(Locale.US, "Car rental station %s at %.6f, %.6f", name, y, x);
    }


}