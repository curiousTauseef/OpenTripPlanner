package org.opentripplanner.api.resource;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.Router;

import java.util.*;

/**
 * Created by klemen on 12.8.2015.
 *
 */
public class CarRental {

    Collection<CarRentalStation> stations;
    RoutingRequest request;
    Router router;

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
