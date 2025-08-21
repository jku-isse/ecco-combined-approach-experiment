package anon.ecco.experiment.result.persister;

import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.result.Result;


public interface ResultPersister {
    void persist(Result result, ExperimentIterationConfiguration config, boolean boosting);
    void cleanup(ExperimentIterationConfiguration config);
    boolean iterationIsNecessary(ExperimentIterationConfiguration config);
}
