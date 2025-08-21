package anon.ecco.experiment.result;

import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.result.persister.ResultPersister;
import anon.ecco.experiment.utils.vevos.GroundTruth;
import at.jku.isse.ecco.tree.Node;
import org.logicng.datastructures.Assignment;

import java.util.Collection;
import java.util.stream.Collectors;

public class ResultCalculator {
    private final ResultPersister resultPersister;
    private final ExperimentIterationConfiguration config;
    private final boolean boosting;
    private GroundTruth groundTruth;

    public ResultCalculator(ExperimentIterationConfiguration config,
                            ResultPersister resultPersister,
                            boolean boosting,
                            GroundTruth groundTruth){
        this.config = config;
        this.resultPersister = resultPersister;
        this.boosting = boosting;
        this.groundTruth = groundTruth;
    }

    public void calculateMetrics(Node.Op mainTree){
        Collection<Assignment> assignments = AssignmentPowerset.getAssignmentPowerset(this.config.getVevosConfiguration().gatherSampledFeatures());
        EvaluationVisitor visitor = new EvaluationVisitor(assignments, this.groundTruth, this.config.getEvaluationConfiguration().getEvaluationStrategy());
        mainTree.traverse(visitor);
        Collection<NodeResult> nodeResults = visitor.getResults();
        Collection<Result> results = nodeResults.stream().map(NodeResult::getResult).collect(Collectors.toList());
        Result overallResult = Result.overallResult(results);
        this.resultPersister.persist(overallResult, this.config, this.boosting);
    }
}
