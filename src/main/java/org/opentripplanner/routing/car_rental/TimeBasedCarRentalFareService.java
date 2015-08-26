package org.opentripplanner.routing.car_rental;

import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.routing.core.Fare;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.WrappedCurrency;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.vertextype.CarRentalStationVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Currency;
import java.util.List;

/**
 * Created by klemen on 21.8.2015.
 */

public class TimeBasedCarRentalFareService implements FareService, Serializable {
    private static final long serialVersionUID = 7222621661906177942L;

    private static Logger log = LoggerFactory.getLogger(TimeBasedCarRentalFareService.class);

    // Each entry is <max time, cents at that time>; the list is sorted in
    // ascending time order
    private List<P2<Integer>> pricing_by_second;

    private Currency currency;

    protected TimeBasedCarRentalFareService(Currency currency, List<P2<Integer>> pricingBySecond) {
        this.currency = currency;
        this.pricing_by_second = pricingBySecond;
    }

    @Override
    public Fare getCost(GraphPath path) {
        Fare fare = new Fare();
        fare.addFare(Fare.FareType.regular, new WrappedCurrency(currency), 0);
        return fare;
    }

    public Fare getCost2(TripPlan plan) {
        int cost = 0;
        int last_hour = 0;
        long seconds = plan.itinerary.get(0).duration;
        long numberOfHours = (seconds % 86400 ) / 3600;
        long numberOfMinutes = ((seconds % 86400 ) % 3600 ) / 60;
        for(int i = 0; i < numberOfHours; i++){
            try{
                cost += pricing_by_second.get(i).second;
            }catch (Exception e){
                cost += pricing_by_second.get(pricing_by_second.size()-1).second;
            }
            last_hour = i;
        }
        try{
            int price = pricing_by_second.get(last_hour).second;
            cost += price * (numberOfMinutes*1.0/60);
        }catch (Exception e){
            int price = pricing_by_second.get(pricing_by_second.size()-1).second;
            cost += price * (numberOfMinutes*1.0/60);
        }
        Fare fare = new Fare();
        fare.addFare(Fare.FareType.regular, new WrappedCurrency(currency), cost);
        return fare;
    }

    @Override
    public List<FareService> getFareServices() {
        return null;
    }

    public Currency getCurrency() {
        return this.currency;
    }
}
