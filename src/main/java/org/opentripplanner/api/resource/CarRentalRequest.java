package org.opentripplanner.api.resource;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.bike_rental.TimeBasedBikeRentalFareService;
import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.car_rental.TimeBasedCarRentalFareService;
import org.opentripplanner.routing.core.*;
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
        TimeBasedCarRentalFareService carFare = router.graph.getCarFareService();

        RoutingRequest alternativeRequest = new RoutingRequest();
        RoutingRequest toStation = new RoutingRequest();
        RoutingRequest betweenStations = new RoutingRequest();
        RoutingRequest fromStation = new RoutingRequest();

        boolean alternativePathWasUsed = false;
        Collection<CarRentalStation> stations = router.graph.getCarRentalStations();
        CarRentalStation startStation = closestStation(start, stations, router, request, "start");
        CarRentalStation endStation = closestStation(end, stations, router, request, "end");

        alternativeRequest.from = start;
        alternativeRequest.to = end;
        if (mode == 0) {
            // Basic CarSharing
            alternativeRequest.setModes(new TraverseModeSet(TraverseMode.WALK));
            toStation.setModes(new TraverseModeSet(TraverseMode.WALK));
            fromStation.setModes(new TraverseModeSet(TraverseMode.WALK));
        } else if (mode == 1) {
            alternativeRequest.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
            toStation.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
            fromStation.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
        } else if (mode == 2) {
            // CarSharing + BikeSharing
            alternativeRequest.allowBikeRental = true;
            alternativeRequest.useBikeRentalAvailabilityInformation = true;
            alternativeRequest.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.BICYCLE));
            toStation.allowBikeRental = true;
            toStation.useBikeRentalAvailabilityInformation = true;
            fromStation.allowBikeRental = true;
            fromStation.useBikeRentalAvailabilityInformation = true;
            toStation.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.BICYCLE));
            fromStation.setModes(new TraverseModeSet(TraverseMode.WALK, TraverseMode.BICYCLE));
        }
        GraphPathFinder gpFinderWalking = new GraphPathFinder(router);
        List<GraphPath> pathsWalking = gpFinderWalking.graphPathFinderEntryPoint(alternativeRequest);
        TripPlan alternativePlan = GraphPathToTripPlanConverter.generatePlan(pathsWalking, request);

        if (startStation == endStation) {
            alternativePathWasUsed = true;
            return alternativePlan;
        }
        if (!alternativePathWasUsed) {
            toStation.from = start;
            toStation.setToString("::" + Double.toString(startStation.y) + "," + Double.toString(startStation.x));
            GraphPathFinder gpFinderToStation = new GraphPathFinder(router);
            List<GraphPath> pathsToStation = gpFinderToStation.graphPathFinderEntryPoint(toStation);
            TripPlan toStationPlan = GraphPathToTripPlanConverter.generatePlan(pathsToStation, request);

            betweenStations.setModes(new TraverseModeSet(TraverseMode.CAR));
            betweenStations.setFromString("::" + Double.toString(startStation.y) + "," + Double.toString(startStation.x));
            betweenStations.setToString("::" + Double.toString(endStation.y) + "," + Double.toString(endStation.x));
            GraphPathFinder gpFinderBetweenStation = new GraphPathFinder(router);
            List<GraphPath> pathsBetweenStation = gpFinderBetweenStation.graphPathFinderEntryPoint(betweenStations);
            TripPlan carSharingPlan = GraphPathToTripPlanConverter.generatePlan(pathsBetweenStation, request);

            fromStation.setFromString("::" + Double.toString(endStation.y) + "," + Double.toString(endStation.x));
            fromStation.to = end;
            GraphPathFinder gpFinderFromStation = new GraphPathFinder(router);
            List<GraphPath> pathsFromStation = gpFinderFromStation.graphPathFinderEntryPoint(fromStation);
            TripPlan fromStationPlan = GraphPathToTripPlanConverter.generatePlan(pathsFromStation, request);

            pathsToStation.addAll(pathsBetweenStation);
            pathsToStation.addAll(pathsFromStation);
            TripPlan mainPlan = GraphPathToTripPlanConverter.generatePlan(pathsToStation, request);

            Itinerary skupniItinerary = mergePath(mainPlan);
            List<Itinerary> skupnaListaItinerary = new ArrayList<Itinerary>();

            int cents = 0;
            Fare skupnaFare = new Fare();
            Fare fareToStation = toStationPlan.itinerary.get(0).fare;
            Fare fareFromStation = fromStationPlan.itinerary.get(0).fare;
            for (Map.Entry<Fare.FareType, Money> entry : fareToStation.fare.entrySet()) {
                cents += entry.getValue().getCents();
            }
            for (Map.Entry<Fare.FareType, Money> entry : fareFromStation.fare.entrySet()) {
                cents += entry.getValue().getCents();
            }

            Fare carSharingFare = carFare.getCost2(carSharingPlan);
            double distance = carSharingPlan.itinerary.get(0).legs.get(0).distance;
            if (distance > router.graph.CarSharingDistanceLimit) {
                carSharingFare.addCost2(router.graph.CarSharingDistanceLimitPenalty);
            }
            for (Map.Entry<Fare.FareType, Money> entry : carSharingFare.fare.entrySet()) {
                cents += entry.getValue().getCents();
            }

            skupnaFare.addFare(Fare.FareType.regular, new WrappedCurrency(carFare.getCurrency()), cents);
            skupniItinerary.fare = skupnaFare;

            mainPlan.from = skupniItinerary.legs.get(0).from;
            mainPlan.to = skupniItinerary.legs.get(skupniItinerary.legs.size() - 1).to;

            skupnaListaItinerary.add(skupniItinerary);
            mainPlan.itinerary = skupnaListaItinerary;

            double mainDuration = mainPlan.itinerary.get(0).duration;
            double alternativeDuration = alternativePlan.itinerary.get(0).duration;

            if (mainDuration > alternativeDuration * 1.3) {
                return alternativePlan;
            } else {
                return mainPlan;
            }
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
                    if (postaja.bikesAvailable > 0) {
                        if (števec2 < števec) {
                            števec = števec2;
                            vrni = postaja;
                        }
                    }
                } else if (nacin.equals("end")) {
                    if (postaja.spacesAvailable > 0) {
                        if (števec2 < števec) {
                            števec = števec2;
                            vrni = postaja;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return vrni;
    }

    public static Itinerary mergePath(TripPlan plan) {
        Itinerary skupniItinerary = new Itinerary();
        Leg pastLeg = new Leg();
        for (int i = 0; i < plan.itinerary.size(); i++) {
            for (int j = 0; j < plan.itinerary.get(i).legs.size(); j++) {
                Leg lg = plan.itinerary.get(i).legs.get(j);
                if (i == 0 && j == 0) {
                    pastLeg = lg;
                    skupniItinerary.addLeg(lg);
                } else {
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
        skupniItinerary.endTime = skupniItinerary.legs.get(skupniItinerary.legs.size() - 1).endTime;
        return skupniItinerary;
    }
}