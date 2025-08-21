package anon.ecco.experiment.runner;

import anon.ecco.experiment.config.ExperimentIterationConfiguration;

public interface ExperimentRunnerInterface {
    void performExperimentIteration(ExperimentIterationConfiguration config);
}
