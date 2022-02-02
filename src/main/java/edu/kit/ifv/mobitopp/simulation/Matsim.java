package edu.kit.ifv.mobitopp.simulation;

import edu.kit.ifv.mobitopp.data.TravelTimeMatrix;
import edu.kit.ifv.mobitopp.data.Zone;
import edu.kit.ifv.mobitopp.data.ZoneId;
import edu.kit.ifv.mobitopp.matsim.*;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.accessibility.AccessibilityModule;
import org.matsim.contrib.accessibility.AccessibilityUtils;
import org.matsim.core.controler.Controler;

import java.util.*;

public class Matsim {

    private final MatsimContext context;
    private final Scenario scenario;
    private final Network network;
    public final Population population;
    private final ActivityFilter filter;

    public Matsim(MatsimContext context, Scenario scenario, ActivityFilter filter) {
        this.context = context;
        this.scenario = scenario;
        this.filter = filter;
        this.network = scenario.getNetwork();
        this.population = scenario.getPopulation();
    }

    public void createPersons() {
        System.out.println("Create demand for matsim");
        createInternalDemand();
        //createExternalDemand();
    }

    private void createInternalDemand() {
        System.out.println("Create internal demand for matsim");
        List<Person> persons = persons();
        System.out.println(persons.size() + " persons simulated in mobiTopp");
        MatsimPersonCreator creator = new MatsimPersonCreator(population);
        List<org.matsim.api.core.v01.population.Person> matsim = creator.createPersons(persons);
        System.out.println(matsim.size() + " matsim persons created.");
    }

    private void createExternalDemand() {
        ExternalDemandCreator demandCreator = new ExternalDemandCreator(context);
        demandCreator.addDemandTo(population, network);
    }

    private List<Person> persons() {
        List<Person> persons = new ArrayList<Person>();
        Collection<Integer> ids = context.personLoader().getPersonOids();
        for (Integer id : ids) {
            Person p = context.personLoader().getPersonByOid(id);
            persons.add(p);
        }
        return persons;
    }

    public void createPlans() {
        System.out.println("Create plans for matsim persons");
        List<Person> persons = persons();
        MatsimPlanCreator creator = planCreator(population, network);
        creator.createPlansForPersons(persons);
    }

    private MatsimPlanCreator planCreator(Population population, Network network) {
        return new MatsimPlanCreator(population, network, filter);
    }

    public Controler simulate() {
        List<String> activityTypes = AccessibilityUtils.collectAllFacilityOptionTypes(scenario);
        System.out.println("The following activity types were found: " + activityTypes);

//        //version 1 with accessiblity module
//        Controler controler = new Controler(scenario);
//        for (final String actType : activityTypes) { // Add an overriding module for each activity type.
//            final AccessibilityModule module = new AccessibilityModule();
//            module.setConsideredActivityType(actType);
//            controler.addOverridingModule(module);
//        }
//        controler.run();
//        return controler;

        //version 2 only core model
        //controler.addOverridingModule( new OTFVisLiveModule() ) ;
        Controler controler = new Controler(scenario);
        controler.run();
        return controler;
    }

    public TreeMap<Integer, TravelTimeMatrix> createTravelTimeMatrices(
            Controler controler) {
        MatsimMatrixGenerator generate = new MatsimMatrixGenerator(network, context.getRoadNetwork(),
                idToOidMapping());
        return generate.travelTimeMatrices(controler);
    }

    private Map<Integer, ZoneId> idToOidMapping() {
        Map<ZoneId, Zone> zones = context.zoneRepository().zones();
        Map<Integer, ZoneId> id2OidMapping = new LinkedHashMap<>();
        for (Zone zone : zones.values()) {
            Integer id = Integer.valueOf(zone.getId().getExternalId().replaceFirst("Z", ""));
            id2OidMapping.put(id, zone.getId());
        }
        return id2OidMapping;
    }

    public Network getNetwork() {
        return this.network;
    }

}