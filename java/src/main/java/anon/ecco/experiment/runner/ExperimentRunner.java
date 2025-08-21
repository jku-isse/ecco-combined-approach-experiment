package anon.ecco.experiment.runner;

import anon.ecco.experiment.config.Boosting;
import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.exception.ExperimentException;
import anon.ecco.experiment.mistake.MistakeCreator;
import anon.ecco.experiment.mistake.MistakeException;
import anon.ecco.experiment.result.ResultCalculator;
import anon.ecco.experiment.result.persister.ResultPersister;
import anon.ecco.experiment.utils.vevos.GroundTruth;
import at.jku.isse.ecco.maintree.building.MainTreeBuildingStrategy;
import at.jku.isse.ecco.storage.ser.maintree.SerAssociationMerger;
import at.jku.isse.ecco.storage.ser.maintree.SerBoostedAssociationMerger;
import at.jku.isse.ecco.tree.*;
import at.jku.isse.ecco.repository.Repository;
import at.jku.isse.ecco.feature.*;
import anon.ecco.experiment.utils.LiteralCleanUpVisitor;
import org.tinylog.Logger;

import java.util.*;


public class ExperimentRunner implements ExperimentRunnerInterface {

    private final Repository.Op repository;
    private final ResultPersister persister;
    private MainTreeBuildingStrategy boostedBuildingStrategy;
    private MainTreeBuildingStrategy nonBoostedBuildingStrategy;
    private ExperimentIterationConfiguration config;
    private int tries = 0;

    public ExperimentRunner(Repository.Op repository, ResultPersister persister){
        this.repository = repository;
        this.persister = persister;
        this.nonBoostedBuildingStrategy = new SerAssociationMerger();
        this.boostedBuildingStrategy = new SerBoostedAssociationMerger();
    }

    public void performExperimentIteration(ExperimentIterationConfiguration config){
        this.config = config;
        List<String> features = config.getVevosConfiguration().gatherSampledFeatures();
        MistakeCreator mistakeCreator = new MistakeCreator(config.getInputConfiguration().getMistakeStrategy(features));
        RepositoryPreparator repositoryPreparator = new RepositoryPreparator(mistakeCreator, config.getEvaluationConfiguration().getListPicker());
        GroundTruth groundTruth = new GroundTruth(config.getVevosConfiguration().getVariantsBaseDir());
        int featureTracePercentage = config.getInputConfiguration().getFeatureTracePercentage();
        int mistakePercentage = config.getInputConfiguration().getMistakePercentage();

        try {
            repositoryPreparator.prepareRepository(this.repository, featureTracePercentage, mistakePercentage, groundTruth);
        } catch (MistakeException e){
            if (this.tries == 10){
                throw new ExperimentException(String.format("Introducing mistakes failed after %d tries.", this.tries));
            }
            Logger.info(e.getMessage());
            Logger.info(String.format("Attempting to run experiment iteration again after %d tries.", this.tries));
            this.tries++;
            this.performExperimentIteration(config);
            return;
        }

        Boosting boosting = config.getEvaluationConfiguration().getBoosting();

        // perform without boost
        if (boosting.equals(Boosting.DISABLED) || boosting.equals(Boosting.BOTH)) {
            this.repository.setMaintreeBuildingStrategy(this.nonBoostedBuildingStrategy);
            this.evaluateMainTree(false, groundTruth);
        }

        // perform with boost
        if (boosting.equals(Boosting.ENABLED) || boosting.equals(Boosting.BOTH)) {
            this.repository.setMaintreeBuildingStrategy(this.boostedBuildingStrategy);
            this.evaluateMainTree(true, groundTruth);
        }

        repositoryPreparator.undoPreparation();
    }

    private void evaluateMainTree(boolean boost, GroundTruth groundTruth){
        this.repository.buildMainTree();
        Node.Op mainTree = this.repository.getMainTree();
        this.literalNameCleanup(mainTree);
        ResultCalculator metricsCalculator = new ResultCalculator(this.config, this.persister, boost, groundTruth);
        metricsCalculator.calculateMetrics(mainTree);
    }

    private void literalNameCleanup(Node.Op node){
        // necessary for when there are features in the ground-truth without revision-ID
        Map<String, String> literalNameMap = new HashMap<>();
        for (String groundTruthName : this.config.getVevosConfiguration().gatherSampledFeaturesIncludingBase()){
            Collection<Feature> features = this.repository.getFeaturesByName(groundTruthName);
            if (!features.isEmpty()){
                String repoName = features.iterator().next().getLatestRevision().getLogicLiteralRepresentation();
                literalNameMap.put(groundTruthName, repoName);
            }
        }
        LiteralCleanUpVisitor visitor = new LiteralCleanUpVisitor(literalNameMap);
        node.traverse(visitor);
    }
}
