package anon.ecco.experiment;

import anon.ecco.experiment.config.ExperimentConfigurationIterator;
import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.exception.ExperimentException;
import anon.ecco.experiment.result.persister.ResultDatabasePersister;
import anon.ecco.experiment.result.persister.ResultPersister;
import anon.ecco.experiment.runner.ExperimentRunner;
import anon.ecco.experiment.runner.ExperimentRunnerInterface;
import anon.ecco.experiment.sample.VevosFeatureSampler;
import anon.ecco.experiment.committer.EccoVariantCommitterInterface;
import anon.ecco.experiment.committer.EccoVariantCommitter;
import anon.ecco.experiment.utils.resource.ResourceException;
import anon.ecco.experiment.utils.resource.ResourceUtils;
import org.variantsync.vevos.simulation.io.Resources.ResourceIOException;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;


public class Experiment {

    private final boolean sample;
    private final ResultPersister resultPersister;

    public Experiment(boolean sample, ResultPersister resultPersister){
        this.sample = sample;
        this.resultPersister = resultPersister;
    }

    public static void main(String[] args) throws ResourceIOException, IOException, URISyntaxException, ResourceException {
        String databasePath = ResourceUtils.getResourceFolderPathAsString("database");
        ResultPersister persister = new ResultDatabasePersister(databasePath);
        Experiment experiment = new Experiment(true, persister);

        String configPath = ResourceUtils.getResourceFolderPathAsString("configuration/experiment.properties");
        Path variantBasePath = ResourceUtils.getResourceFolderPath("sample");
        ExperimentConfigurationIterator experimentConfig = new ExperimentConfigurationIterator(persister, configPath, variantBasePath);
        experiment.runExperiment(experimentConfig);
    }

    public void runExperiment(ExperimentConfigurationIterator configIterator){
        Logger.info("Running Experiment...\n");
        int numberOfRepetitions = configIterator.getNumberOfRepetitions();
        while(configIterator.hasNext()){
            List<ExperimentIterationConfiguration> configBatch = configIterator.getNextConfigurationBatch();
            for (int i = 1; i <= numberOfRepetitions; i++) {
                attemptExperimentRun(configBatch);
            }
        }
    }

    private void attemptExperimentRun(List<ExperimentIterationConfiguration> configBatch){
        try {
            processConfigBatch(configBatch);
        } catch (ExperimentException e){
            // try new sample if mistakes are not possible to introduce after many tries
            attemptExperimentRun(configBatch);
        }
    }

    private void processConfigBatch(List<ExperimentIterationConfiguration> configBatch) {
        VevosFeatureSampler sampler = new VevosFeatureSampler();
        EccoVariantCommitterInterface committer = null;
        ExperimentIterationConfiguration firstConfig = configBatch.getFirst();
        try {
            if (this.sample) {
                sampler.sample(firstConfig);
            }
            this.pickBatchVariants(configBatch);
            committer = new EccoVariantCommitter(firstConfig);
            committer.commit();
            ExperimentRunnerInterface runner = new ExperimentRunner(committer.getRepository(), this.resultPersister);
            for (ExperimentIterationConfiguration config : configBatch){
                this.performExperimentIteration(runner, config);
            }
        } catch (ExperimentException e) {
            throw new ExperimentException(e);
        }  catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (committer != null) {
                committer.cleanUp();
            }
            if (this.sample) {
                sampler.cleanUp();
            }
        }
    }

    private void pickBatchVariants(List<ExperimentIterationConfiguration> configBatch){
        ExperimentIterationConfiguration firstConfig = configBatch.getFirst();
        firstConfig.pickVariants();
        configBatch.forEach(config -> config.setVariantPick(firstConfig.getVariantPick()));
    }

    private void performExperimentIteration(ExperimentRunnerInterface runner, ExperimentIterationConfiguration config){
        try {
            if (this.resultPersister.iterationIsNecessary(config)) {
                Logger.info("Running Experiment Iteration:\n" + config);
                runner.performExperimentIteration(config);
            } else {
                Logger.info("Experiment iteration not necessary, as enough results already exist:\n" + config);
            }
        } catch (ExperimentException e){
            this.resultPersister.cleanup(config);
            throw new ExperimentException(e);
        }
    }
}
