package configuration;

import anon.ecco.experiment.config.ExperimentConfigurationIterator;
import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.result.persister.ResultInMemoryPersister;
import anon.ecco.experiment.utils.resource.ResourceException;
import anon.ecco.experiment.utils.resource.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExperimentConfigurationIteratorTest {

    @Test
    public void propertiesFileCanBeRead() throws ResourceException {
        String propertiesFilePath = ResourceUtils.getResourceFolderPathAsString("configs/test_experiment.properties");
        Path variantBasePath = ResourceUtils.getResourceFolderPath("sample");
        ExperimentConfigurationIterator config = new ExperimentConfigurationIterator(propertiesFilePath, variantBasePath);
    }

    @Test
    public void numbersOfVariantsAreIterated() throws ResourceException {
        String propertiesFilePath = ResourceUtils.getResourceFolderPathAsString("configs/test_experiment.properties");
        Path variantBasePath = ResourceUtils.getResourceFolderPath("sample");
        ExperimentConfigurationIterator configIterator = new ExperimentConfigurationIterator(propertiesFilePath, variantBasePath);
        List<ExperimentIterationConfiguration> configBatch = new LinkedList<>();
        for (int i = 1; i <= 3; i++){
            configIterator.getNextConfigurationBatch();
        }
        assertThrows(NullPointerException.class, configIterator::getNextConfigurationBatch);
        assertFalse(configIterator.hasNext());
    }

    @Test
    public void iteratorCreatesCorrectBatches() throws ResourceException {
        String propertiesFilePath = ResourceUtils.getResourceFolderPathAsString("configs/iterator_test.properties");
        Path variantBasePath = ResourceUtils.getResourceFolderPath("sample");
        ExperimentConfigurationIterator configIterator = new ExperimentConfigurationIterator(propertiesFilePath, variantBasePath);
        List<ExperimentIterationConfiguration> configBatch;
        for (int i = 1; i <= 3; i++){
            configBatch = configIterator.getNextConfigurationBatch();
            assertEquals(15, configBatch.size());
        }
        assertThrows(NullPointerException.class, configIterator::getNextConfigurationBatch);
        assertFalse(configIterator.hasNext());
    }
}
