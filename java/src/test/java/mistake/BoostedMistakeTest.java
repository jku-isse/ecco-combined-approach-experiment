package mistake;

import at.jku.isse.ecco.core.Association;
import anon.ecco.experiment.config.ExperimentConfigurationIterator;
import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.picker.featuretracepicker.RandomFeatureTracePicker;
import anon.ecco.experiment.mistake.strategy.SwappedFeature;
import anon.ecco.experiment.mistake.MistakeCreator;
import anon.ecco.experiment.mistake.strategy.MistakeStrategy;
import anon.ecco.experiment.picker.featuretracepicker.SingleAssociationTracePicker;
import anon.ecco.experiment.runner.RepositoryPreparator;
import anon.ecco.experiment.sample.VevosFeatureSampler;
import anon.ecco.experiment.committer.EccoVariantCommitter;
import anon.ecco.experiment.utils.CounterVisitor;
import anon.ecco.experiment.picker.MemoryListPicker;
import anon.ecco.experiment.utils.vevos.GroundTruth;
import at.jku.isse.ecco.featuretrace.FeatureTrace;
import at.jku.isse.ecco.repository.Repository;
import at.jku.isse.ecco.storage.ser.maintree.SerBoostedAssociationMerger;
import anon.ecco.experiment.utils.directory.DirectoryException;
import anon.ecco.experiment.utils.directory.DirectoryUtils;
import anon.ecco.experiment.utils.resource.ResourceException;
import anon.ecco.experiment.utils.resource.ResourceUtils;
import utils.nodevisitor.EvaluatableNodeCounter;
import utils.nodevisitor.MistakeCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.variantsync.vevos.simulation.io.Resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoostedMistakeTest {

    private final String configPath = ResourceUtils.getResourceFolderPathAsString("configs/boost_mistakes.properties");
    private final Path variantBasePath = ResourceUtils.getResourceFolderPath("sample");
    private final Path repoPath = ResourceUtils.getResourceFolderPath("repo");

    public BoostedMistakeTest() throws ResourceException {
    }

    @BeforeEach
    public void setLoggerLevel(){
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.FINE);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.FINE);
        }
    }

    @BeforeEach
    public void deleteResources() throws DirectoryException {
        DirectoryUtils.deleteAndCreateFolder(this.variantBasePath);
        DirectoryUtils.deleteAndCreateFolder(this.repoPath);
    }

    private void analyzeAssociations(Collection<Association.Op> associations, MistakeCreator mistakeCreator){
        for (Association.Op association : associations) {
            CounterVisitor counterVisitor = new CounterVisitor();
            association.getRootNode().traverse(counterVisitor);
            System.out.println("\nnumber of nodes with proactive feature traces in association " + association + ":" + counterVisitor.getProactiveConditionCount());

            MistakeCounter mistakeCounter = new MistakeCounter(mistakeCreator);
            association.getRootNode().traverse(mistakeCounter);
            System.out.println("number of faulty traces in association " + association + ":" + mistakeCounter.getMistakeCount());

            EvaluatableNodeCounter evaluatableNodeCounter = new EvaluatableNodeCounter(variantBasePath);
            association.getRootNode().traverse(evaluatableNodeCounter);
            System.out.println("number of evaluatable nodes in association " + association + ":" + evaluatableNodeCounter.getCount());
        }
    }

    @Test
    public void boostedMistakesDisableBoosting() throws Resources.ResourceIOException, IOException, ResourceException {
        ExperimentConfigurationIterator experimentConfig = new ExperimentConfigurationIterator(this.configPath, this.variantBasePath);
        ExperimentIterationConfiguration config = experimentConfig.next();
        VevosFeatureSampler sampler = new VevosFeatureSampler();
        sampler.sample(config);
        config.pickVariants();
        EccoVariantCommitter committer = new EccoVariantCommitter(config);
        committer.commit();
        Repository.Op repo = committer.getRepository();

        MistakeStrategy mistakeStrategy = new SwappedFeature(config.getVevosConfiguration().gatherSampledFeatures());
        MistakeCreator mistakeCreator = new MistakeCreator(mistakeStrategy);
        MemoryListPicker<FeatureTrace> listPicker = new RandomFeatureTracePicker();
        RepositoryPreparator repositoryPreparator = new RepositoryPreparator(mistakeCreator, listPicker);
        GroundTruth groundTruth = new GroundTruth(this.variantBasePath);
        repositoryPreparator.prepareRepository(repo, 1, 100, groundTruth);

        Collection<Association.Op> associations = (Collection<Association.Op>) repo.getAssociations();
        this.analyzeAssociations(associations, mistakeCreator);

        repo.setMaintreeBuildingStrategy(new SerBoostedAssociationMerger());
        repo.buildMainTree();
    }

    @Test
    public void nonConflictingMistakesGetBoosted() throws Resources.ResourceIOException, IOException, ResourceException {
        ExperimentConfigurationIterator experimentConfig = new ExperimentConfigurationIterator(this.configPath, this.variantBasePath);
        ExperimentIterationConfiguration config = experimentConfig.next();
        VevosFeatureSampler sampler = new VevosFeatureSampler();
        sampler.sample(config);
        config.pickVariants();
        EccoVariantCommitter committer = new EccoVariantCommitter(config);
        committer.commit();
        Repository.Op repo = committer.getRepository();

        MistakeStrategy mistakeStrategy = new SwappedFeature(config.getVevosConfiguration().gatherSampledFeatures());
        MistakeCreator mistakeCreator = new MistakeCreator(mistakeStrategy);
        MemoryListPicker<FeatureTrace> listPicker = new SingleAssociationTracePicker();
        RepositoryPreparator repositoryPreparator = new RepositoryPreparator(mistakeCreator, listPicker);
        GroundTruth groundTruth = new GroundTruth(this.variantBasePath);
        repositoryPreparator.prepareRepository(repo, 100, 100, groundTruth);

        Collection<Association.Op> associations = (Collection<Association.Op>) repo.getAssociations();
        this.analyzeAssociations(associations, mistakeCreator);

        repo.setMaintreeBuildingStrategy(new SerBoostedAssociationMerger());
        repo.buildMainTree();
    }
}
