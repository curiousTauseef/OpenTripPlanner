package org.opentripplanner.api.resource;

/**
 * Created by klemen on 12.8.2015.
 */
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opentripplanner.routing.car_rental.CarRentalStation;

@XmlRootElement(name="BikeRentalStationList")

public class CarRentalStationList {
    @XmlElements(value = { @XmlElement(name="station") })
    public List<CarRentalStation> stations = new ArrayList<CarRentalStation>();
}
