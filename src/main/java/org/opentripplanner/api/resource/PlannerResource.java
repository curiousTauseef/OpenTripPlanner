/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.opentripplanner.api.resource;

import static org.opentripplanner.api.resource.ServerInfo.Q;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.opentripplanner.api.common.RoutingResource;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.model.error.PlannerError;
import org.opentripplanner.routing.bike_rental.TimeBasedBikeRentalFareService;
import org.opentripplanner.routing.car_rental.CarRentalStation;
import org.opentripplanner.routing.car_rental.TimeBasedCarRentalFareService;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

/**
 * This is the primary entry point for the trip planning web service.
 * All parameters are passed in the query string. These parameters are defined as fields in the abstract
 * RoutingResource superclass, which also has methods for building routing requests from query
 * parameters. This allows multiple web services to have the same set of query parameters.
 * In order for inheritance to work, the REST resources are request-scoped (constructed at each request)
 * rather than singleton-scoped (a single instance existing for the lifetime of the OTP server).
 */

@Path("routers/{routerId}/plan") // final element needed here rather than on method to distinguish from routers API
public class PlannerResource extends RoutingResource {

    private static final Logger LOG = LoggerFactory.getLogger(PlannerResource.class);

    // We inject info about the incoming request so we can include the incoming query
    // parameters in the outgoing response. This is a TriMet requirement.
    // Jersey uses @Context to inject internal types and @InjectParam or @Resource for DI objects.
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + Q, MediaType.TEXT_XML + Q})
    public Response plan(@Context OTPServer otpServer, @Context UriInfo uriInfo) {

        /*
         * TODO: add Lang / Locale parameter, and thus get localized content (Messages & more...)
         * TODO: from/to inputs should be converted / geocoded / etc... here, and maybe send coords 
         *       or vertex ids to planner (or error back to user)
         * TODO: org.opentripplanner.routing.module.PathServiceImpl has COOORD parsing. Abstract that
         *       out so it's used here too...
         */

        // Create response object, containing a copy of all request parameters. Maybe they should be in the debug section of the response.
        Response response = new Response(uriInfo);
        RoutingRequest request = null;

        try {
            /* Fill in request fields from query parameters via shared superclass method, catching any errors. */
            request = super.buildRequest();
            /* Find some good GraphPaths through the OTP Graph. */

            System.out.println(request);

            Router router = otpServer.getRouter(request.routerId);

            if (request.modes.toString().equals("TraverseMode (CARRENT)")) {
                // Only CarSharing
                Collection<CarRentalStation> stations = router.graph.getCarRentalStations();
                CarRentalRequest carRental = new CarRentalRequest(stations, request, router);
                response.setPlan(carRental.getPlan(request.from, request.to, 0));
            } else if (request.modes.toString().equals("TraverseMode (TRAM, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, GONDOLA, FUNICULAR, TRANSIT, TRAINISH, BUSISH, CARRENT)")) {
                // Transit + CarSharing + Transit
                Collection<CarRentalStation> stations = router.graph.getCarRentalStations();
                CarRentalRequest carRental = new CarRentalRequest(stations, request, router);
                response.setPlan(carRental.getPlan(request.from, request.to, 1));
            } else if (request.modes.toString().equals("TraverseMode (BICYCLE, CARRENT)")){
                // BicikeLJ + CarSharing + BicikeLJ
                Collection<CarRentalStation> stations = router.graph.getCarRentalStations();
                CarRentalRequest carRental = new CarRentalRequest(stations, request, router);
                response.setPlan(carRental.getPlan(request.from, request.to, 2));
            } else {
                // Default
                GraphPathFinder gpFinder = new GraphPathFinder(router);
                List<GraphPath> paths = gpFinder.graphPathFinderEntryPoint(request);
                TripPlan plan = GraphPathToTripPlanConverter.generatePlan(paths, request);
                response.setPlan(plan);
            }
        } catch (Exception e) {
            PlannerError error = new PlannerError(e);
            if (!PlannerError.isPlanningError(e.getClass()))
                LOG.warn("Error while planning path: ", e);
            response.setError(error);
        } finally {
            if (request != null) {
                if (request.rctx != null) {
                    response.debugOutput = request.rctx.debugOutput;
                }
                request.cleanup(); // TODO verify that this cleanup step is being done on Analyst web services
            }
        }
        return response;
    }
}
