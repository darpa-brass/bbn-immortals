package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiRequirements;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.ServerUpgradeLibrary;
import mil.darpa.immortals.das.DeploymentModelBuilder;
import org.apache.jena.rdf.model.Model;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class P2CP3TestCoordinator extends AbstractTestCoordinator {

    public P2CP3TestCoordinator() {
        super();
    }

    @Nonnull
    @Override
    protected String getDeploymentModel(@Nonnull SubmissionModel submissionModel) throws Exception {
        Model deploymentModel = new DeploymentModelBuilder(submissionModel).build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        deploymentModel.write(out, "TURTLE");

        String rval = new String(out.toByteArray());
        System.out.println(rval);
        return rval;
    }

    @Nonnull
    @Override
    synchronized List<String> validateSubmissionModel(SubmissionModel submissionModel) throws Exception {
        List<String> rval = new LinkedList<>();

        if (submissionModel.globalModel != null) {
            rval.add("Global requirements are not supported for CP3!");
        }

        Set<Object> upgradeObjects = new HashSet<>();

        if (submissionModel.martiServerModel != null && submissionModel.martiServerModel.requirements != null) {
            upgradeObjects.add(submissionModel.martiServerModel.requirements.libraryUpgrade);
            upgradeObjects.add(submissionModel.martiServerModel.requirements.partialLibraryUpgrade);
        }

        if (submissionModel.atakLiteClientModel != null && submissionModel.atakLiteClientModel.requirements != null) {
            upgradeObjects.add(submissionModel.atakLiteClientModel.requirements.libraryUpgrade);
            upgradeObjects.add(submissionModel.atakLiteClientModel.requirements.partialLibraryUpgrade);
        }

        int upgradeObjectCount = upgradeObjects.stream().filter(Objects::nonNull).collect(Collectors.toSet()).size();

        if (upgradeObjectCount > 1) {
            rval.add("At most a single library upgrade can be provided at once for CP3!");

        } else if (upgradeObjectCount == 0) {
            rval.add("At least one library upgrade must be provided if a submission model is provided for CP3!");
        }
        return rval;
    }

    @Override
    synchronized void setupChallengeProblem(SubmissionModel submissionModel) throws Exception {
        // The das performs the upgrade of libraries for CP3
    }

    public static void main(String[] args) {
        try {
            P2CP3TestCoordinator tc = new P2CP3TestCoordinator();

            SubmissionModel submissionModel = new SubmissionModel(
                    "1337Model",
                    new MartiSubmissionModel(
                            new MartiRequirements(
                                    null,
                                    ServerUpgradeLibrary.ElevationApi_2,
                                    null
                            )
                    ),
                    null,
                    null
            );


            tc.execute(submissionModel, false);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
