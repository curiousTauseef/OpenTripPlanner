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
 */
public class CarRentalRequest {

    Collection<CarRentalStation> stations;
    RoutingRequest request;
    Router router;

    public CarRentalRequest(Collection<CarRentalStation> stations, RoutingRequest request, Router router) {
        this.stations = stations;
        this.request = request;
        this.router = router;
    }

    public TripPlan getPlan(GenericLocation start, GenericLocation end, int mode) {
        TraverseModeSet tms = new TraverseModeSet();


        boolean shouldIwalk = false;
        Collection<CarRentalStation> stations = router.graph.getCarRentalStations();
        CarRentalStation startStation = closestStation(start, stations, router, request, "start");
        CarRentalStation endStation = closestStation(end, stations, router, request, "end");
        if (startStation == endStation) { // FIXME dodaj še 2 scenarija
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
            RoutingRequest betweenStations = new RoutingRequest();
            RoutingRequest fromStation = new RoutingRequest();

            if (mode == 0) {
                toStation.setModes(new TraverseModeSet(TraverseMode.WALK));
                fromStation.setModes(new TraverseModeSet(TraverseMode.WALK));
            } else if (mode == 1) {
                toStation.setModes(new TraverseModeSet(TraverseMode.TRANSIT));
                fromStation.setModes(new TraverseModeSet(TraverseMode.TRANSIT));
            } else if (mode == 2) {
                toStation.allowBikeRental = true;
                fromStation.allowBikeRental = true;
                toStation.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.BICYCLE));
                fromStation.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.BICYCLE));
            }

            toStation.from = start;
            toStation.setToString("::" + Double.toString(startStation.y) + "," + Double.toString(startStation.x));
            GraphPathFinder gpFinderToStation = new GraphPathFinder(router);
            List<GraphPath> pathsToStation = gpFinderToStation.graphPathFinderEntryPoint(toStation);

            betweenStations.setModes(new TraverseModeSet(TraverseMode.CAR));
            betweenStations.setFromString("::" + Double.toString(startStation.y) + "," + Double.toString(startStation.x));
            betweenStations.setToString("::" + Double.toString(endStation.y) + "," + Double.toString(endStation.x));
            GraphPathFinder gpFinderBetweenStation = new GraphPathFinder(router);
            List<GraphPath> pathsBetweenStation = gpFinderBetweenStation.graphPathFinderEntryPoint(betweenStations);

            fromStation.setFromString("::" + Double.toString(endStation.y) + "," + Double.toString(endStation.x));
            fromStation.to = end;
            GraphPathFinder gpFinderFromStation = new GraphPathFinder(router);
            List<GraphPath> pathsFromStation = gpFinderFromStation.graphPathFinderEntryPoint(fromStation);

            pathsToStation.addAll(pathsBetweenStation);
            pathsToStation.addAll(pathsFromStation);
            TripPlan plan = GraphPathToTripPlanConverter.generatePlan(pathsToStation, request);

            Itinerary skupniItinerary = mergePath(plan);
            List<Itinerary> skupnaListaItinerary = new ArrayList<Itinerary>();

            plan.from = skupniItinerary.legs.get(0).from;
            plan.to = skupniItinerary.legs.get(skupniItinerary.legs.size() - 1).to;

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

    public static Itinerary mergePath(TripPlan plan){
        Itinerary skupniItinerary = new Itinerary();
        Leg pastLeg = new Leg();
        for (int i = 0; i < plan.itinerary.size(); i++) {
            for (int j = 0; j < plan.itinerary.get(i).legs.size(); j++) {
                Leg lg = plan.itinerary.get(i).legs.get(j);
                if(i == 0 && j == 0){
                    pastLeg = lg;
                    skupniItinerary.addLeg(lg);
                }else{
                    Double duraton = lg.getDuration();
                    lg.startTime = pastLeg.endTime;
                    Calendar lgEndTime = (Calendar) lg.startTime.clone();
                    lgEndTime.add(Calendar.SECOND, duraton.intValue());
                    lg.endTime = lgEndTime;
                    skupniItinerary.addLeg(lg);
                }
                pastLeg = lg;
            }
        }
        skupniItinerary.startTime = skupniItinerary.legs.get(0).startTime;
        skupniItinerary.endTime = skupniItinerary.legs.get(skupniItinerary.legs.size() - 1 ).endTime;
        return skupniItinerary;
    }

}
