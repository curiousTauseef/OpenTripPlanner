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
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.model.error.PlannerError;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opentripplanner.routing.car_rental.CarRentalStationService;

import java.util.*;

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

            System.out.println("\n/api/resource/PlannerResource");
            System.out.println("Request: " + request);
            /* Find some good GraphPaths through the OTP Graph. */
            Router router = otpServer.getRouter(request.routerId);

            if (request.modes.toString().equals("TraverseMode (WALK, CAR)")) {
                // 46.062288322607856, 14.515128135681152  46.063479426751435,14.514634609222412  1.7976931348623157E308 Wed Aug 12 11:25:00 CEST 2015 false QUICK WALK 1
                //TODO realiziraj svoje metode

                RoutingRequest test1 = new RoutingRequest();
                test1.setModes(new TraverseModeSet(TraverseMode.WALK));
                test1.setFromString("::46.062288322607856,14.515128135681152");
                test1.setToString("::46.063479426751435,14.514634609222412");

                GraphPathFinder gpFinder = new GraphPathFinder(router);
                List<GraphPath> paths = gpFinder.graphPathFinderEntryPoint(test1);

                RoutingRequest test2 = new RoutingRequest();
                test2.setModes(new TraverseModeSet(TraverseMode.BICYCLE));
                test2.setFromString("::46.06232554500132,14.515085220336914");
                test2.setToString("::46.06056117595413,14.516673088073729");

                GraphPathFinder gpFinder2 = new GraphPathFinder(router);
                List<GraphPath> paths2 = gpFinder.graphPathFinderEntryPoint(test2);


                paths.addAll(paths2);
                TripPlan plan = GraphPathToTripPlanConverter.generatePlan(paths, request);


                Itinerary skupni = new Itinerary();
                List<Itinerary> skupinIterary = new ArrayList<Itinerary>();
                for(int i = 0; i < plan.itinerary.size(); i++ ) {
                    for (int j = 0; j < plan.itinerary.get(i).legs.size(); j++) {
                        skupni.addLeg(plan.itinerary.get(i).legs.get(j));
                    }
                }
                skupinIterary.add(skupni);

                plan.itinerary = skupinIterary;

                response.setPlan(plan);

            }else {
                GraphPathFinder gpFinder = new GraphPathFinder(router);
                List<GraphPath> paths = gpFinder.graphPathFinderEntryPoint(request);
                TripPlan plan = GraphPathToTripPlanConverter.generatePlan(paths, request);
                System.out.println(plan.itinerary.size());
                response.setPlan(plan);
            }

            //GraphPathFinder gpFinder = new GraphPathFinder(router); // we could also get a persistent router-scoped GraphPathFinder but there's no setup cost here
            //List<GraphPath> paths = gpFinder.graphPathFinderEntryPoint(request);

                /*
                System.out.println("\nList of edges on our path: ");
                for (int i = 0; i < paths.get(0).edges.size(); i++) {
                    System.out.println(paths.get(0).edges.get(i));
                }
                */

            /* Convert the internal GraphPaths to a TripPlan object that is included in an OTP web service Response. */
            //System.out.println("\nGraphPath converted to Plan (it contains other informations as well): ");
            //TripPlan plan = GraphPathToTripPlanConverter.generatePlan(paths, request);
                /*for (int i = 0; i < plan.itinerary.size(); i++) {
                    for (int j = 0; j < plan.itinerary.get(i).legs.size(); j++) {
                        System.out.print(j + 1 + " " + plan.itinerary.get(i).legs.get(j).mode + " : ");
                        System.out.print(plan.itinerary.get(i).legs.get(j).from.name + " : ");
                        System.out.print(plan.itinerary.get(i).legs.get(j).to.name + " : ");
                        System.out.println(plan.itinerary.get(i).legs.get(j).distance + " meters");
                    }
                }*/

            //response.setPlan(plan);

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

        System.out.println("\nResponse: " + response.requestParameters);
        return response;
    }
}
