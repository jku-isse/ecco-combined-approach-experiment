package experiment;

import anon.ecco.experiment.Experiment;
import anon.ecco.experiment.config.ExperimentConfigurationIterator;
import anon.ecco.experiment.result.Result;
import anon.ecco.experiment.result.persister.ResultDatabasePersister;
import anon.ecco.experiment.result.persister.ResultInMemoryPersister;
import anon.ecco.experiment.result.persister.ResultPersister;
import anon.ecco.experiment.utils.directory.DirectoryException;
import anon.ecco.experiment.utils.directory.DirectoryUtils;
import anon.ecco.experiment.utils.resource.ResourceException;
import anon.ecco.experiment.utils.resource.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.DatabaseResultUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ExperimentTest {

    @BeforeEach
    @AfterEach
    public void cleanUp() throws IOException, ResourceException, DirectoryException {
        Path samplePath = ResourceUtils.getResourceFolderPath("sample");
        Path repoPath = ResourceUtils.getResourceFolderPath("repo");
        DirectoryUtils.deleteFolderIfItExists(repoPath);
        Files.createDirectories(repoPath);
        DirectoryUtils.deleteFolderIfItExists(samplePath);
        Files.createDirectories(samplePath);
    }

    @AfterAll
    public static void cleanUpDatabase() throws ResourceException, DirectoryException {
        Path databasePath = ResourceUtils.getResourceFolderPath("database");
        if (DatabaseResultUtils.databaseExists()) {
            DirectoryUtils.deleteFolderIfItExists(databasePath);
        }
    }

    @Test
    public void experimentRunsWithoutException() throws ResourceException {
        String databasePath = ResourceUtils.getResourceFolderPathAsString("database");
        ResultPersister persister = new ResultDatabasePersister(databasePath);
        Experiment experiment = new Experiment(false, persister);

        String configPath = ResourceUtils.getResourceFolderPathAsString("configs/test_config2.properties");
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_2");
        ExperimentConfigurationIterator experimentConfig = new ExperimentConfigurationIterator(configPath, variantBasePath);
        experiment.runExperiment(experimentConfig);
    }

    @Test
    public void configurationCreatesCorrectNumberOfResults() throws ResourceException {
        ResultInMemoryPersister persister = new ResultInMemoryPersister();
        Experiment experiment = new Experiment(false, persister);

        String configPath = ResourceUtils.getResourceFolderPathAsString("configs/test_config2.properties");
        Path variantBasePath = ResourceUtils.getResourceFolderPath("Sampling_Base_2");
        ExperimentConfigurationIterator experimentConfig = new ExperimentConfigurationIterator(configPath, variantBasePath);
        experiment.runExperiment(experimentConfig);

        assertEquals(10, persister.getResults().size());
    }
}
