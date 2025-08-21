package anon.ecco.experiment.committer;

import at.jku.isse.ecco.repository.Repository;

public interface EccoVariantCommitterInterface {
    void commit();
    void cleanUp();
    Repository.Op getRepository();
}
