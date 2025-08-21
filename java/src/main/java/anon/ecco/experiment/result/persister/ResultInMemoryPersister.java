package anon.ecco.experiment.result.persister;

import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.result.Result;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;


@Getter
public class ResultInMemoryPersister implements ResultPersister {

    private final Collection<Result> results;

    public ResultInMemoryPersister(){
        this.results = new LinkedList<>();
    }

    @Override
    public void persist(Result result, ExperimentIterationConfiguration config, boolean boosting) {
        this.results.add(result);
    }

    @Override
    public void cleanup(ExperimentIterationConfiguration config) {
        // nothing to do here
    }

    @Override
    public boolean iterationIsNecessary(ExperimentIterationConfiguration config) {
        return true;
    }
}
