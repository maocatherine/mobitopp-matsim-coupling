package edu.kit.ifv.mobitopp.populationsynthesis;

import static edu.kit.ifv.mobitopp.populationsynthesis.EconomicalStatusCalculators.oecd2017;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import edu.kit.ifv.mobitopp.data.PanelDataRepository;
import edu.kit.ifv.mobitopp.populationsynthesis.carownership.*;
import edu.kit.ifv.mobitopp.populationsynthesis.householdlocation.HouseholdLocationSelector;
import edu.kit.ifv.mobitopp.populationsynthesis.householdlocation.LanduseCLCwithRoadsHouseholdLocationSelector;
import edu.kit.ifv.mobitopp.simulation.IdSequence;
import edu.kit.ifv.mobitopp.simulation.ImpedanceIfc;
import edu.kit.ifv.mobitopp.simulation.emobility.EmobilityPersonCreator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopulationSynthesisMatsim extends BasicPopulationSynthesisIpf {

    public PopulationSynthesisMatsim(
            CarOwnershipModel carOwnershipModel, HouseholdLocationSelector householdLocationSelector,
            PersonCreator personCreator, ActivityScheduleAssigner activityScheduleAssigner,
            SynthesisContext context) {

        super(carOwnershipModel, householdLocationSelector, personCreator,
                activityScheduleAssigner, oecd2017(), context);
    }

    //Start population synthesis here
    public static void main(String... args) throws Exception {
        if (1 > args.length) {
            log.error("Usage: ... <configuration file>");
            System.exit(-1);
        }

        File configurationFile = new File(args[0]);
        LocalDateTime start = LocalDateTime.now();
        startSynthesis(configurationFile);
        LocalDateTime end = LocalDateTime.now();
        Duration runtime = Duration.between(start, end);
        log.info("Population synthesis took " + runtime);
    }

    static PopulationSynthesisMatsim startSynthesis(File configurationFile) throws Exception {
        SynthesisContext context = new ContextBuilder().buildFrom(configurationFile);
        return startSynthesis(context);
    }

    public static PopulationSynthesisMatsim startSynthesis(SynthesisContext context) {
        context.printStartupInformationOn(System.out);
        PopulationSynthesisMatsim synthesizer = populationSynthesis(context);
        synthesizer.createPopulation();
        return synthesizer;
    }

    private static HouseholdLocationSelector householdLocations(SynthesisContext context) {
        return new LanduseCLCwithRoadsHouseholdLocationSelector(context);
    }

    private static PopulationSynthesisMatsim populationSynthesis(SynthesisContext context) {

        //Define household locations.
        HouseholdLocationSelector householdLocationSelector = householdLocations(context);

        //Define commute ticket. Use the null model.
        CommutationTicketModelIfc commuterTicketModel = commuterTickets(context);

        //Define car ownership.
        CarOwnershipModel carOwnershipModel = carOwnership(context);

        //For electric vehicle. Not use in this project.
        //ChargePrivatelySelector chargePrivatelySelector = chargePrivately(context);

        //Define simulated persons based on panel data.
        PersonCreator personCreator = personCreator(context, commuterTicketModel);

        PanelDataRepository panelDataRepository = context.dataRepository().panelDataRepository();
        ActivityScheduleCreator scheduleCreator = new DefaultActivityScheduleCreator();
        ActivityScheduleAssigner activityScheduleAssigner = new DefaultActivityAssigner(
                panelDataRepository, scheduleCreator);
        return populationSynthesis(householdLocationSelector, carOwnershipModel,
                personCreator, activityScheduleAssigner, context);
    }

    private static PanelBasedPersonCreator personCreator(
            SynthesisContext context, CommutationTicketModelIfc commuterTicketModel) {
        //Map<String, MobilityProviderCustomerModel> carSharing = context.carSharing();
        //return new EmobilityPersonCreator(commuterTicketModel, carSharing, context.seed());
        return new PanelBasedPersonCreator(commuterTicketModel);
    }

    private static CommutationTicketModelIfc commuterTickets(SynthesisContext context) {
        String commuterTicketFile = context.configuration().getCommuterTicket();
        //return new CommutationTicketModelStuttgart(commuterTicketFile, context.seed());
        return new CommutationTicketNullModel();
    }

    private static CarOwnershipModel carOwnership(SynthesisContext context) {
        IdSequence carIDs = new IdSequence();
        long seed = context.seed();
//		File engineFile = context.carEngineFile();
//		String ownershipFile = context.configuration().getCarOwnership().getOwnership();
        String segmentFile = context.configuration().getCarOwnership().getSegment();
        ImpedanceIfc impedance = context.impedance();
        CarSegmentModel segmentModel = new LogitBasedCarSegmentModel(impedance, seed, segmentFile);
//		ProbabilityForElectricCarOwnershipModel calculator = new ElectricCarOwnershipBasedOnSociodemographic(
//				impedance, engineFile.getAbsolutePath());
//		return new GenericElectricCarOwnershipModel(carIDs, segmentModel, seed, calculator,
//				ownershipFile);
        return new DefaultCarOwnershipModel(carIDs, segmentModel);
    }

    private static AllowChargingProbabilityBased chargePrivately(SynthesisContext context) {
        return new AllowChargingProbabilityBased(context.seed());
    }

    private static PopulationSynthesisMatsim populationSynthesis(
            HouseholdLocationSelector householdLocationSelector, CarOwnershipModel carOwnershipModel, PersonCreator personCreator,
            ActivityScheduleAssigner activityScheduleAssigner, SynthesisContext context) {
        return new PopulationSynthesisMatsim(carOwnershipModel, householdLocationSelector,
                personCreator, activityScheduleAssigner, context);
    }

}
