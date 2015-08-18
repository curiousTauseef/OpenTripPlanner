package org.opentripplanner.api.resource;

import static org.opentripplanner.api.resource.ServerInfo.Q;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
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

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.car_rental.CarRentalStationService;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.util.ResourceBundleSingleton;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Created by klemen on 12.8.2015.
 *
 */

@Path("/routers/{routerId}/car_rental")
@XmlRootElement
public class CarRental {
	@Context
    OTPServer otpServer;
	
    Collection<CarRentalStation> stations;
    RoutingRequest request;
    Router router;

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
    
    public CarRental(Collection<CarRentalStation> stations, RoutingRequest request, Router router){
        this.stations = stations;
        this.request = request;
        this.router = router;
    }

    public TripPlan getPlan(GenericLocation start, GenericLocation end) {
        boolean shouldIwalk = false;
        Collection<CarRentalStation> stations = router.graph.getCarRentalStations();
        CarRentalStation startStation = closestStation(start, stations, router, request, "start");
        CarRentalStation endStation = closestStation(end, stations, router, request, "end");
        if (startStation == endStation) {
            RoutingRequest walkingRequest = new RoutingRequest();
            walkingRequest.setModes(new TraverseModeSet(TraverseMode.WALK));
            walkingRequest.from = start;
            walkingRequest.to = end;
            GraphPathFinder gpFinderWalking = new GraphPathFinder(router);
            List<GraphPath> pathsWalking = gpFinderWalking.graphPathFinderEntryPoint(walkingRequest);
            TripPlan planWalking = GraphPathToTripPlanConverter.generatePlan(pathsWalking, request);
            shouldIwalk = true;
            return planWalking;
        }
        if (!shouldIwalk) {
            RoutingRequest toStation = new RoutingRequest();
            toStation.setModes(new TraverseModeSet(TraverseMode.WALK));
            toStation.from = start;
            toStation.setToString("::" + Double.toString(startStation.y) + "," + Double.toString(startStation.x));
            GraphPathFinder gpFinderToStation = new GraphPathFinder(router);
            List<GraphPath> pathsToStation = gpFinderToStation.graphPathFinderEntryPoint(toStation);

            RoutingRequest betweenStations = new RoutingRequest();
            betweenStations.setModes(new TraverseModeSet(TraverseMode.CAR));
            betweenStations.setFromString("::" + Double.toString(startStation.y) + "," + Double.toString(startStation.x));
            betweenStations.setToString("::" + Double.toString(endStation.y) + "," + Double.toString(endStation.x));
            GraphPathFinder gpFinderBetweenStation = new GraphPathFinder(router);
            List<GraphPath> pathsBetweenStation = gpFinderBetweenStation.graphPathFinderEntryPoint(betweenStations);

            RoutingRequest fromStation = new RoutingRequest();
            fromStation.setModes(new TraverseModeSet(TraverseMode.WALK));
            fromStation.setFromString("::" + Double.toString(endStation.y) + "," + Double.toString(endStation.x));
            fromStation.to = end;
            GraphPathFinder gpFinderFromStation = new GraphPathFinder(router);
            List<GraphPath> pathsFromStation = gpFinderFromStation.graphPathFinderEntryPoint(fromStation);

            pathsFromStation.addAll(pathsBetweenStation);
            pathsFromStation.addAll(pathsToStation);
            TripPlan plan = GraphPathToTripPlanConverter.generatePlan(pathsFromStation, request);

            plan.from = plan.itinerary.get(2).legs.get(0).from;
            plan.to = plan.itinerary.get(0).legs.get(0).to;

            Itinerary skupniItinerary = new Itinerary();
            List<Itinerary> skupnaListaItinerary = new ArrayList<Itinerary>();
            skupniItinerary.startTime = plan.itinerary.get(2).legs.get(0).startTime;
            skupniItinerary.endTime = plan.itinerary.get(0).legs.get(0).endTime;

            Leg past_leg = new Leg();
            System.out.println("");
            for (int i = plan.itinerary.size() - 1; i >= 0; i--) {
                Leg obdelujem = plan.itinerary.get(i).legs.get(0);
                if (i == plan.itinerary.size() - 1) {
                    skupniItinerary.addLeg(obdelujem);
                } else {
                    Double duraton = obdelujem.getDuration();
                    obdelujem.startTime = past_leg.endTime;
                    Calendar obdelujemEndTime = (Calendar) obdelujem.startTime.clone();
                    obdelujemEndTime.add(Calendar.SECOND, duraton.intValue());
                    obdelujem.endTime = obdelujemEndTime;
                    skupniItinerary.addLeg(obdelujem);
                }
                past_leg = obdelujem;
            }
            
            skupniItinerary.startTime = plan.itinerary.get(0).startTime;
            skupniItinerary.endTime = plan.itinerary.get(0).endTime;
            
            skupnaListaItinerary.add(skupniItinerary);
            plan.itinerary = skupnaListaItinerary;
            return plan;
        }
        return null;
    }


    public static CarRentalStation closestStation(GenericLocation location, Collection<CarRentalStation> stations, Router router, RoutingRequest request, String nacin) {
        Iterator it = stations.iterator();
        CarRentalStation vrni = null;
        double števec = Double.MAX_VALUE;
        while (it.hasNext()) {
            CarRentalStation postaja = (CarRentalStation) it.next();
            try {
                RoutingRequest meritevRequest = new RoutingRequest();
                meritevRequest.from = location;
                meritevRequest.setToString("::" + Double.toString(postaja.y) + "," + Double.toString(postaja.x));
                meritevRequest.setModes(new TraverseModeSet(TraverseMode.WALK));
                GraphPathFinder gpFinder = new GraphPathFinder(router);
                List<GraphPath> paths = gpFinder.graphPathFinderEntryPoint(meritevRequest);
                TripPlan plan = GraphPathToTripPlanConverter.generatePlan(paths, request);
                double števec2 = 0;
                for (int i = 0; i < plan.itinerary.size(); i++) {
                    for (int j = 0; j < plan.itinerary.get(i).legs.size(); j++) {
                        števec2 += plan.itinerary.get(i).legs.get(j).distance;
                    }
                }
                if (nacin.equals("start")) {
                    //if(postaja.bikesAvailable > 0){ FIXME <------ Odkomentiraj if stavek
                    if (števec2 < števec) {
                        števec = števec2;
                        vrni = postaja;
                    }
                    //}
                } else if (nacin.equals("end")) {
                    //if (postaja.spacesAvailable > 0) { FIXME <------ Odkomentiraj if stavek
                    if (števec2 < števec) {
                        števec = števec2;
                        vrni = postaja;
                    }
                    //}
                }
            } catch (Exception e) {
            }
        }
        return vrni;
    }
}
