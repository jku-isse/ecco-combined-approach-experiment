package runner;

import anon.ecco.experiment.config.*;
import anon.ecco.experiment.mistake.strategy.*;

import anon.ecco.experiment.picker.featuretracepicker.RandomFeatureTracePicker;
import anon.ecco.experiment.picker.variantspick.VariantPick;
import anon.ecco.experiment.result.Result;
import anon.ecco.experiment.result.persister.ResultInMemoryPersister;
import anon.ecco.experiment.runner.ExperimentRunner;
import anon.ecco.experiment.runner.ExperimentRunnerInterface;
import anon.ecco.experiment.committer.EccoVariantCommitter;
import anon.ecco.experiment.committer.EccoVariantCommitterInterface;
import anon.ecco.experiment.utils.directory.DirectoryException;
import anon.ecco.experiment.utils.directory.DirectoryUtils;
import anon.ecco.experiment.utils.resource.ResourceException;
import anon.ecco.experiment.utils.resource.ResourceUtils;
import at.jku.isse.ecco.featuretrace.evaluation.EvaluationStrategy;
import at.jku.isse.ecco.storage.ser.featuretrace.evaluation.SerRetroactiveBasedEvaluation;
import at.jku.isse.ecco.storage.ser.featuretrace.evaluation.SerProactiveBasedEvaluation;
import at.jku.isse.ecco.repository.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExperimentRunnerTest {

    @Mock
    ExperimentIterationConfiguration config;
    @Mock
    EvaluationConfiguration evaluationConfiguration;
    @Mock
    InputConfiguration inputConfiguration;
    @Mock
    VevosConfiguration vevosConfiguration;
    @Mock
    VariantPick variantPick;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(config.getEvaluationConfiguration()).thenReturn(evaluationConfiguration);
        when(config.getInputConfiguration()).thenReturn(inputConfiguration);
        when(config.getVevosConfiguration()).thenReturn(vevosConfiguration);
        when(config.getVariantPick()).thenReturn(variantPick);

        when(evaluationConfiguration.getListPicker()).thenReturn(new RandomFeatureTracePicker());
        when(vevosConfiguration.getMinVariantFeatures()).thenReturn(0);
        when(vevosConfiguration.getMaxVariantFeatures()).thenReturn(10);
        when(config.getNumberOfRepetitions()).thenReturn(1);
        when(config.getDataset()).thenReturn("");
    }

    @BeforeEach
    @AfterEach
    public void deleteRepo() throws ResourceException {
        Path repoPath = ResourceUtils.getResourceFolderPath("repo");

        try{
            DirectoryUtils.deleteFolderIfItExists(repoPath);
            Files.createDirectories(repoPath);
        } catch (DirectoryException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Repository.Op prepareRepository(ExperimentIterationConfiguration config) throws ResourceException {
        EccoVariantCommitterInterface committer = new EccoVariantCommitter(config);
        committer.commit();
        return committer.getRepository();
    }

    private static ExperimentIterationConfiguration getDefaultConfigMock1() throws ResourceException {
        ExperimentIterationConfiguration configMock = mock(ExperimentIterationConfiguration.class);
        when(configMock.getNumberOfRepetitions()).thenReturn(1);
        when(configMock.getDataset()).thenReturn("");
        EvaluationConfiguration evaluationConfigMock = mock(EvaluationConfiguration.class);
        InputConfiguration inputConfigMock = mock(InputConfiguration.class);
        VevosConfiguration vevosConfigMock = mock(VevosConfiguration.class);
        VariantPick variantPickMock = mock(VariantPick.class);
        when(configMock.getEvaluationConfiguration()).thenReturn(evaluationConfigMock);
        when(configMock.getInputConfiguration()).thenReturn(inputConfigMock);
        when(configMock.getVevosConfiguration()).thenReturn(vevosConfigMock);
        when(configMock.getVariantPick()).thenReturn(variantPickMock);

        when(evaluationConfigMock.getListPicker()).thenReturn(new RandomFeatureTracePicker());
        when(evaluationConfigMock.getBoosting()).thenReturn(Boosting.ENABLED);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfigMock.getEvaluationStrategy()).thenReturn(evaluationStrategy);

        when(vevosConfigMock.getMinVariantFeatures()).thenReturn(0);
        when(vevosConfigMock.getMaxVariantFeatures()).thenReturn(10);
        when(vevosConfigMock.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfigMock.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_2");
        when(vevosConfigMock.getVariantsBaseDir()).thenReturn(variantBasePath);
        when(vevosConfigMock.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB", "BASE"));

        when(inputConfigMock.getFeatureTracePercentage()).thenReturn(0);
        when(inputConfigMock.getMistakePercentage()).thenReturn(0);
        when(inputConfigMock.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfigMock.getMistakeStrategy(List.of("FEATUREA", "FEATUREB"))).thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));

        when(variantPickMock.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREA"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        when(variantPickMock.getVariantPickPaths()).thenReturn(variantPicks);

        return configMock;
    }

    private static ExperimentIterationConfiguration getDefaultConfigMock2() throws ResourceException {
        ExperimentIterationConfiguration configMock = mock(ExperimentIterationConfiguration.class);
        when(configMock.getNumberOfRepetitions()).thenReturn(1);
        when(configMock.getDataset()).thenReturn("");
        EvaluationConfiguration evaluationConfigMock = mock(EvaluationConfiguration.class);
        InputConfiguration inputConfigMock = mock(InputConfiguration.class);
        VevosConfiguration vevosConfigMock = mock(VevosConfiguration.class);
        VariantPick variantPickMock = mock(VariantPick.class);
        when(configMock.getEvaluationConfiguration()).thenReturn(evaluationConfigMock);
        when(configMock.getInputConfiguration()).thenReturn(inputConfigMock);
        when(configMock.getVevosConfiguration()).thenReturn(vevosConfigMock);
        when(configMock.getVariantPick()).thenReturn(variantPickMock);

        when(evaluationConfigMock.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfigMock.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfigMock.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfigMock.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB", "BASE"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfigMock.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfigMock.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(evaluationConfigMock.getListPicker()).thenReturn(new RandomFeatureTracePicker());
        when(inputConfigMock.getMistakePercentage()).thenReturn(100);
        when(inputConfigMock.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfigMock.getMistakeStrategy(List.of("FEATUREA", "FEATUREB"))).thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPickMock.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPickMock.getVariantPickPaths()).thenReturn(variantPicks);

        return configMock;
    }

    @Test
    public void experimentRunsWithoutException() throws ResourceException {
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_1");
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("NoMistake");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB"))).thenReturn(new NoMistake());
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("FEATUREA", "FEATUREB", "BASE"));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);
        
        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();
        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);
        assertFalse(persister.getResults().isEmpty());
    }
    
    @Test
    public void mistakesCanBeBoosted() throws ResourceException {
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_7");
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(10);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(100);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB"))).thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();
        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);
        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(0.75, result.getF1());
    }
    
    @Test
    public void experimentWithBoostRunsWithoutException() throws ResourceException {
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_1");
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);
        
        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();
        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);
        assertFalse(persister.getResults().isEmpty());
    }

    @Test
    public void perfectScoreWhenAllVariantsAreCommitted() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());
    }

    @Test
    public void perfectScoreWithBoostWhenAllVariantsAreCommitted() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase())
                .thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations())
                .thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());
    }

    @Test
    public void using100PercentFeatureTracesResultsInPerfectScore() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_1");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase())
                .thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());
    }

    @Test
    public void using100PercentFeatureTracesAndBoostResultsInPerfectScore() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_1");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase())
                .thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());
    }

    @Test
    public void mistakesDontChangeResultForZeroPercentMistakes() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase())
                .thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());
    }

    @Test
    public void mistakesDontChangeResultForZeroPercentMistakesAndBoost() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase())
                .thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());
    }


    @Test
    public void experimentCreatesTheCorrectNumberOfAtomicResults() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("Base", "FEATUREA", "FEATUREB", "BASE"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());

        // number of artifacts: 10 lines
        // 2 Features ==> 4 Combinations
        // 10 x 4 = 40 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        assertEquals(20, result.getTp());
        assertEquals(20, result.getTn());
    }

    @Test
    public void experimentCreatesTheCorrectNumberOfAtomicResultsWithBoost() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());
        assertEquals(1.0, result.getF1());

        // number of artifacts: 10 lines
        // 2 Features ==> 4 Combinations
        // 10 x 4 = 40 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        assertEquals(20, result.getTp());
        assertEquals(20, result.getTn());
    }


    @Test
    public void featureARepoCreatesCorrectResults() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_2");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREA"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 4 + 6 lines ==> 10 Artifacts (that are not BASE-Artifacts)
        // 2 Features ==> 4 Combinations
        // 10 x 4 = 40 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        // one variant with the features BASE and A will be committed.
        // all artifacts will therefore have the condition "A || BASE".

        // BASE             tp:    fp:10  tn:    fn:
        // BASE && A        tp:10  fp:    tn:    fn:
        // BASE && B        tp:5   fp:5   tn:    fn:
        // BASE && A && B   tp:10  fp:    tn:    fn:
        //                  tp:25  fp:15  tn:0   fn:0
        assertEquals(25, result.getTp());
        assertEquals(15, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureARepoCreatesCorrectResultsWithBoost() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_2");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREA"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 4 + 6 lines ==> 10 Artifacts (that are not BASE-Artifacts)
        // 2 Features ==> 4 Combinations
        // 10 x 4 = 40 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        // one variant with the features BASE and A will be committed.
        // all artifacts will therefore have the condition "A || BASE".

        // BASE             tp:    fp:10  tn:    fn:
        // BASE && A        tp:10  fp:    tn:    fn:
        // BASE && B        tp:5   fp:5   tn:    fn:
        // BASE && A && B   tp:10  fp:    tn:    fn:
        //                  tp:25  fp:15  tn:0   fn:0
        assertEquals(25, result.getTp());
        assertEquals(15, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureARepoCreatesCorrectForMultipleFtPercentagesResultsWithBoost() throws ResourceException {
        List<ExperimentIterationConfiguration> configBatch = new LinkedList<>();
        ExperimentIterationConfiguration config = getDefaultConfigMock1();
        configBatch.add(config);
        config = getDefaultConfigMock1();
        InputConfiguration inputConfig = config.getInputConfiguration();
        when(inputConfig.getFeatureTracePercentage()).thenReturn(100);
        configBatch.add(config);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        configBatch.forEach(runner::performExperimentIteration);

        assertEquals(2, persister.getResults().size());

        Iterator<Result> iterator = persister.getResults().iterator();
        Result result= iterator.next();
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        assertEquals(25, result.getTp());
        assertEquals(15, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());

        result= iterator.next();
        atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        assertEquals(25, result.getTp());
        assertEquals(0, result.getFp());
        assertEquals(15, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureABRepoCreatesCorrectResults() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_3");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 8 + 12 lines ==> 20 Artifacts (that are not BASE-Artifacts)
        // 2 Features ==> 4 Combinations
        // 20 x 4 = 80 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(80, atomicResults);

        // BASE             tp:    fp:20  tn:    fn:
        // BASE && A        tp:10  fp:10  tn:    fn:
        // BASE && B        tp:10  fp:10  tn:    fn:
        // BASE && A && B   tp:20  fp:    tn:    fn:
        //                  tp:40  fp:40  tn:0   fn:0
        assertEquals(40, result.getTp());
        assertEquals(40, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureABRepoCreatesCorrectResultsWithBoost() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_3");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 8 + 12 lines ==> 20 Artifacts (that are not BASE-Artifacts)
        // 2 Features ==> 4 Combinations
        // 20 x 4 = 80 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(80, atomicResults);

        // BASE             tp:    fp:20  tn:    fn:
        // BASE && A        tp:10  fp:10  tn:    fn:
        // BASE && B        tp:10  fp:10  tn:    fn:
        // BASE && A && B   tp:20  fp:    tn:    fn:
        //                  tp:40  fp:40  tn:0   fn:0
        assertEquals(40, result.getTp());
        assertEquals(40, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureBRepoCreatesCorrectResults() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB", "BASE"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_4");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 6 + 9 lines ==> 15 Artifacts (that are not BASE-Artifacts) (B, ~A, A||B)
        // 2 Features ==> 4 Combinations
        // 15 x 4 = 60 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(60, atomicResults);

        // BASE             tp:5   fp:10  tn:    fn:
        // BASE && A        tp:5   fp:10  tn:    fn:
        // BASE && B        tp:15  fp:    tn:    fn:
        // BASE && A && B   tp:10  fp:5   tn:    fn:
        //                  tp:35  fp:25  tn:0   fn:0
        assertEquals(35, result.getTp());
        assertEquals(25, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureBRepoCreatesCorrectResultsWithBoost() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_4");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 6 + 9 lines ==> 15 Artifacts (that are not BASE-Artifacts) (B, ~A, A||B)
        // 2 Features ==> 4 Combinations
        // 15 x 4 = 60 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(60, atomicResults);

        // BASE             tp:5   fp:10  tn:    fn:
        // BASE && A        tp:5   fp:10  tn:    fn:
        // BASE && B        tp:15  fp:    tn:    fn:
        // BASE && A && B   tp:10  fp:5   tn:    fn:
        //                  tp:35  fp:25  tn:0   fn:0
        assertEquals(35, result.getTp());
        assertEquals(25, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureBASERepoCreatesCorrectResults() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_5");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 5 Artifacts (that are not BASE-Artifacts) (~A)
        // 2 Features ==> 4 Combinations
        // 5 x 4 = 20 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(20, atomicResults);

        // BASE             tp:5   fp:    tn:    fn:
        // BASE && A        tp:    fp:5   tn:    fn:
        // BASE && B        tp:5   fp:    tn:    fn:
        // BASE && A && B   tp:    fp:5   tn:    fn:
        //                  tp:10  fp:10  tn:0   fn:0
        assertEquals(10, result.getTp());
        assertEquals(10, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void featureBASERepoCreatesCorrectResultsWithBoost() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(0);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(vevosConfiguration.gatherSampledFeaturesIncludingBase()).thenReturn(List.of("BASE", "FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_5");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerRetroactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 5 Artifacts (that are not BASE-Artifacts) (~A)
        // 2 Features ==> 4 Combinations
        // 5 x 4 = 20 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(20, atomicResults);

        // BASE             tp:5   fp:    tn:    fn:
        // BASE && A        tp:    fp:5   tn:    fn:
        // BASE && B        tp:5   fp:    tn:    fn:
        // BASE && A && B   tp:    fp:5   tn:    fn:
        //                  tp:10  fp:10  tn:0   fn:0
        assertEquals(10, result.getTp());
        assertEquals(10, result.getFp());
        assertEquals(0, result.getTn());
        assertEquals(0, result.getFn());
    }

    @Test
    public void allFeatureTracesCreatePerfectScoreDespiteFlawedRetroactiveConditions() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_5");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 5 Artifacts (that are not BASE-Artifacts) (~A)
        // 2 Features ==> 4 Combinations
        // 5 x 4 = 20 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(20, atomicResults);
        assertEquals(10, result.getTp());
        assertEquals(10, result.getTn());
    }

    @Test
    public void allFeatureTracesAndBoostCreatePerfectScoreDespiteFlawedRetroactiveConditions() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_5");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        // number of artifacts: 5 Artifacts (that are not BASE-Artifacts) (~A)
        // 2 Features ==> 4 Combinations
        // 5 x 4 = 20 atomic results
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(20, atomicResults);
        assertEquals(10, result.getTp());
        assertEquals(10, result.getTn());
    }

    @Test
    public void mistakesWorsenResultUsingSwappedCondition() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_1");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(50);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedCondition");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedCondition());
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(100, atomicResults);
        assertTrue(result.getF1() < 1.0);
    }

    @Test
    public void mistakesWorsenResultUsingErroneousConjunction() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(50);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("ErroneousConjunction");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new ErroneousConjunction(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        assertTrue(result.getF1() < 1.0);
    }

    @Test
    public void mistakesWorsenResultUsingSwappedFeature() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_6");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(50);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(40, atomicResults);
        assertTrue(result.getF1() < 1.0);
    }

    @Test
    public void mistakesWorsenResultUsingSwappedOperator() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.DISABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(100);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_1");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(40);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedOperator");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedOperator());
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE", "BASE, FEATUREA", "BASE, FEATUREB", "BASE, FEATUREA, FEATUREB"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        variantPicks.add(variantBasePath.resolve("Variant_AB"));
        variantPicks.add(variantBasePath.resolve("Variant_B"));
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        assertEquals(1, persister.getResults().size());

        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(100, atomicResults);
        assertTrue(result.getF1() < 1.0);
    }

    @Test
    public void mistakesMustNotPersist() throws ResourceException {
        List<ExperimentIterationConfiguration> configBatch = new LinkedList<>();
        ExperimentIterationConfiguration config = getDefaultConfigMock2();
        configBatch.add(config);
        config = getDefaultConfigMock2();
        InputConfiguration inputConfig = config.getInputConfiguration();
        when(inputConfig.getMistakePercentage()).thenReturn(0);
        configBatch.add(config);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        configBatch.forEach(runner::performExperimentIteration);

        Collection<Result> results= persister.getResults();
        Iterator<Result> resultIterator = results.iterator();
        assertTrue(resultIterator.next().getF1() < 1.0);
        assertEquals(1.0, resultIterator.next().getF1());
    }

    @Test
    public void mistakesMustNotPersistWithBoost() throws ResourceException {
        List<ExperimentIterationConfiguration> configBatch = new LinkedList<>();
        ExperimentIterationConfiguration config = getDefaultConfigMock2();
        EvaluationConfiguration evaluationConfig = config.getEvaluationConfiguration();
        when(evaluationConfig.getBoosting()).thenReturn(Boosting.ENABLED);
        configBatch.add(config);
        config = getDefaultConfigMock2();
        evaluationConfig = config.getEvaluationConfiguration();
        when(evaluationConfig.getBoosting()).thenReturn(Boosting.ENABLED);
        InputConfiguration inputConfig = config.getInputConfiguration();
        when(inputConfig.getMistakePercentage()).thenReturn(0);
        configBatch.add(config);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        configBatch.forEach(runner::performExperimentIteration);

        Collection<Result> results= persister.getResults();
        Iterator<Result> resultIterator = results.iterator();
        assertTrue(resultIterator.next().getF1() < 1.0);
        assertEquals(1.0, resultIterator.next().getF1());
    }

    @Test
    public void boostingCreatesPerfectScoreTest() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(50);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_5");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result= persister.getResults().iterator().next();
        int atomicResults = result.getFn() + result.getFp() + result.getTp() + result.getTn();
        assertEquals(20, atomicResults);
        assertEquals(10, result.getTp());
        assertEquals(10, result.getTn());
    }

    @Test
    public void boostingDoesNotHappenForContradictingTraces() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(50);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_2");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(0);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_A"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result = persister.getResults().iterator().next();
        assertTrue(result.getF1() < 1.0);
    }

    @Test
    public void boostingDoesNotHappenBecauseOfMistake() throws ResourceException {
        when(evaluationConfiguration.getBoosting()).thenReturn(Boosting.ENABLED);
        when(inputConfiguration.getFeatureTracePercentage()).thenReturn(40);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_5");
        when(vevosConfiguration.getVariantsBaseDir()).thenReturn(variantBasePath);
        EvaluationStrategy evaluationStrategy = new SerProactiveBasedEvaluation();
        when(evaluationConfiguration.getEvaluationStrategy()).thenReturn(evaluationStrategy);
        when(vevosConfiguration.gatherSampledFeatures()).thenReturn(List.of("FEATUREA", "FEATUREB"));
        when(inputConfiguration.getMistakePercentage()).thenReturn(50);
        when(inputConfiguration.getMistakeStrategyName()).thenReturn("SwappedFeature");
        when(inputConfiguration.getMistakeStrategy(List.of("FEATUREA", "FEATUREB")))
                .thenReturn(new SwappedFeature(List.of("FEATUREA", "FEATUREB")));
        when(variantPick.getVariantPickConfigurations()).thenReturn(List.of("BASE"));
        List<Path> variantPicks = new LinkedList<>();
        variantPicks.add(variantBasePath.resolve("Variant_Null"));
        when(variantPick.getVariantPickPaths()).thenReturn(variantPicks);

        Repository.Op repo = prepareRepository(config);
        ResultInMemoryPersister persister = new ResultInMemoryPersister();

        ExperimentRunnerInterface runner = new ExperimentRunner(repo, persister);
        runner.performExperimentIteration(config);

        Result result = persister.getResults().iterator().next();
        assertTrue(result.getF1() < 1.0);
    }
}

