package org.opentripplanner.updater.car_rental;

/**
 * Created by klemen on 6.8.2015.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.databind.JsonNode;

import org.opentripplanner.graph_builder.linking.SimpleStreetSplitter;
import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.bike_rental.BikeRentalStationService;
import org.opentripplanner.routing.edgetype.RentABikeOffEdge;
import org.opentripplanner.routing.edgetype.RentABikeOnEdge;
import org.opentripplanner.routing.edgetype.loader.LinkRequest;
import org.opentripplanner.routing.edgetype.loader.NetworkLinkerLibrary;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.BikeRentalStationVertex;
import org.opentripplanner.routing.vertextype.CarRentalStationVertex;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.JsonConfigurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarRentalUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(CarRentalUpdater.class);

    private GraphUpdaterManager updaterManager;

    private static final String DEFAULT_NETWORK_LIST = "default";

    Map<CarRentalStation, CarRentalStationVertex> verticesByStation = new HashMap<CarRentalStation, CarRentalStationVertex>();

    private CarRentalDataSource source;

    private Graph graph;

    private SimpleStreetSplitter linker;

    private BikeRentalStationService service;

    private String network = "default";

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.updaterManager = updaterManager;
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {
        // Set data source type from config JSON
        String sourceType = config.path("sourceType").asText();
        String networkName = config.path("network").asText();
        CarRentalDataSource source = null;
        if (sourceType != null) {
            if (sourceType.equals("comtrade")) {
                source = new ComtradeCarRentalDataSource();
            }
        }

        if (source == null) {
            throw new IllegalArgumentException("Unknown car rental source type: " + sourceType);
        } else if (source instanceof JsonConfigurable) {
            ((JsonConfigurable) source).configure(graph, config);
        }

        LOG.info("Setting up car rental updater.");
        this.graph = graph;
        this.source = source;
        this.network = config.path("networks").asText(DEFAULT_NETWORK_LIST);
        LOG.info("Creating car-rental updater running every {} seconds : {}", frequencySec, source);
    }

    @Override
    public void setup() throws InterruptedException, ExecutionException {
        linker = new SimpleStreetSplitter(graph);

        updaterManager.executeBlocking(new GraphWriterRunnable() {
            @Override
            public void run(Graph graph) {
                service = graph.getService(BikeRentalStationService.class, true);
            }
        });
    }

    @Override
    protected void runPolling() throws Exception {
        LOG.debug("Updating car rental stations from " + source);
        if (!source.update()) {
            LOG.debug("No updates");
            return;
        }
        List<CarRentalStation> stations = source.getStations();

        CarRentalGraphWriterRunnable graphWriterRunnable = new CarRentalGraphWriterRunnable(stations);
        updaterManager.execute(graphWriterRunnable);
    }

    @Override
    public void teardown() {
    }

    private class CarRentalGraphWriterRunnable implements GraphWriterRunnable {

        private List<CarRentalStation> stations;

        public CarRentalGraphWriterRunnable(List<CarRentalStation> stations) {
            this.stations = stations;
        }

        @Override
        public void run(Graph graph) {
            Set<CarRentalStation> stationSet = new HashSet<CarRentalStation>();
            Set<String> defaultNetworks = new HashSet<String>(Arrays.asList(network));
            for (CarRentalStation station : stations) {
                if (station.networks == null) {
                    station.networks = defaultNetworks;
                }
                service.addBikeRentalStation(station);
                stationSet.add(station);
                CarRentalStationVertex vertex = verticesByStation.get(station);
                if (vertex == null) {
                    vertex = new CarRentalStationVertex(graph, station);
                    if (!linker.link(vertex)) {
                        LOG.warn("{} not near any streets; it will not be usable.", station);
                    }
                    System.out.println("\t "+vertex);
                    System.out.println("\t "+station);
                    verticesByStation.put(station, vertex);
                    new RentABikeOnEdge(vertex, vertex, station.networks);
                    if (station.allowDropoff)
                        new RentABikeOffEdge(vertex, vertex, station.networks);
                } else {
                    vertex.setBikesAvailable(station.bikesAvailable);
                    vertex.setSpacesAvailable(station.spacesAvailable);
                }
            }
            List<CarRentalStation> toRemove = new ArrayList<CarRentalStation>();
            for (Entry<CarRentalStation, CarRentalStationVertex> entry : verticesByStation.entrySet()) {
                CarRentalStation station = entry.getKey();
                if (stationSet.contains(station))
                    continue;
                CarRentalStationVertex vertex = entry.getValue();
                if (graph.containsVertex(vertex)) {
                    graph.removeVertexAndEdges(vertex);
                }
                toRemove.add(station);
                service.removeBikeRentalStation(station);
            }
            for (CarRentalStation station : toRemove) {
                verticesByStation.remove(station);
            }
        }
    }
}

