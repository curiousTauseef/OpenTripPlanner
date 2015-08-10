package org.opentripplanner.routing.vertextype;

import org.opentripplanner.common.MavenVersion;
import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;

/**
 * Created by klemen on 6.8.2015.
 */
public class CarRentalStationVertex extends Vertex {

    private static final long serialVersionUID = MavenVersion.VERSION.getUID();

    private int carsAvailable;

    private int spacesAvailable;

    private String id;

    public CarRentalStationVertex(Graph g, CarRentalStation station) {
        super(g, "car rental station " + station.id, station.x, station.y, station.name);
        this.setId(station.id);
        this.setCarsAvailable(station.bikesAvailable);
        this.setSpacesAvailable(station.spacesAvailable);
    }

    public int getCarsAvailable() {
        return carsAvailable;
    }

    public int getSpacesAvailable() {
        return spacesAvailable;
    }

    public void setCarsAvailable(int bikes) {
        this.carsAvailable = bikes;
    }

    public void setSpacesAvailable(int spaces) {
        this.spacesAvailable = spaces;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
