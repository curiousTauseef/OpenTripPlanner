package org.opentripplanner.routing.car_rental;

/**
 * Created by klemen on 20.8.2015.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.routing.services.FareServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class TimeBasedCarRentalFareServiceFactory implements FareServiceFactory {

    @Override
    public FareService makeFareService() {
        return null;
    }

    @Override
    public void processGtfs(GtfsRelationalDao dao) {

    }

    @Override
    public void configure(JsonNode config) {

    }
}
