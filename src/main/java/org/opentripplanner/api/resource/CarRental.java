package org.opentripplanner.api.resource;

import static org.opentripplanner.api.resource.ServerInfo.Q;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.car_rental.CarRentalStationService;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.util.ResourceBundleSingleton;

import com.vividsolutions.jts.geom.Envelope;

@Path("/routers/{routerId}/car_rental")
@XmlRootElement
public class CarRental {
    @Context
    OTPServer otpServer;

    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + Q, MediaType.TEXT_XML + Q })
    public CarRentalStationList getCarRentalStations(
            @QueryParam("lowerLeft") String lowerLeft,
            @QueryParam("upperRight") String upperRight,
            @PathParam("routerId") String routerId,
            @QueryParam("locale") String locale_param) {

        Router router = otpServer.getRouter(routerId);
        if (router == null) return null;
        CarRentalStationService carRentalService = router.graph.getService(CarRentalStationService.class);
        Locale locale;
        locale = ResourceBundleSingleton.INSTANCE.getLocale(locale_param);
        if (carRentalService == null) return new CarRentalStationList();
        Envelope envelope;
        if (lowerLeft != null) {
            envelope = getEnvelope(lowerLeft, upperRight);
        } else {
            envelope = new Envelope(-180,180,-90,90);
        }
        Collection<CarRentalStation> stations = carRentalService.getCarRentalStations();
        List<CarRentalStation> out = new ArrayList<>();
        for (CarRentalStation station : stations) {
            if (envelope.contains(station.x, station.y)) {
                CarRentalStation station_localized = station.clone();
                station_localized.locale = locale;
                out.add(station_localized);
            }
        }
        CarRentalStationList brsl = new CarRentalStationList();
        brsl.stations = out;
        return brsl;
    }

    /** Envelopes are in latitude, longitude format */
    public static Envelope getEnvelope(String lowerLeft, String upperRight) {
        String[] lowerLeftParts = lowerLeft.split(",");
        String[] upperRightParts = upperRight.split(",");

        Envelope envelope = new Envelope(Double.parseDouble(lowerLeftParts[1]),
                Double.parseDouble(upperRightParts[1]), Double.parseDouble(lowerLeftParts[0]),
                Double.parseDouble(upperRightParts[0]));
        return envelope;
    }
}