package sample;

import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.config.VevosConfiguration;
import anon.ecco.experiment.sample.VevosFeatureSampler;
import anon.ecco.experiment.utils.vevos.VevosUtils;
import anon.ecco.experiment.utils.directory.DirectoryUtils;
import anon.ecco.experiment.utils.resource.ResourceException;
import anon.ecco.experiment.utils.resource.ResourceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.variantsync.vevos.simulation.io.Resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class RecreatingSampleTest {
    
    String REPO_NAME = "openvpn";
    Path VEVOS_GROUND_TRUTH_BASE = Paths.get("home/user/ground-truth");
    Path VEVOS_GROUND_TRUTH = VEVOS_GROUND_TRUTH_BASE.resolve("openvpn");
    Path VEVOS_REPO = VEVOS_GROUND_TRUTH_BASE.resolve("REPOS\\openvpn");

    @Mock
    private ExperimentIterationConfiguration config;
    @Mock
    private VevosConfiguration vevosConfiguration;

    private final Path creationSamplePath = ResourceUtils.getResourceFolderPath("sample_creation");
    private final Path recreationSamplePath = ResourceUtils.getResourceFolderPath("sample_recreation");

    public RecreatingSampleTest() throws ResourceException {
    }

    @BeforeEach
    public void setup() throws IOException {
        try (AutoCloseable openMocks = MockitoAnnotations.openMocks(this)) {
            Files.createDirectories(this.creationSamplePath);
            Files.createDirectories(this.recreationSamplePath);
            when(config.getVevosConfiguration()).thenReturn(vevosConfiguration);
            when(vevosConfiguration.getMinVariantFeatures()).thenReturn(5);
            when(vevosConfiguration.getMaxVariantFeatures()).thenReturn(10);
            when(vevosConfiguration.getVariantsBaseDir()).thenReturn(this.creationSamplePath);
            when(vevosConfiguration.getVevosGroundTruthDatasetPath()).thenReturn(VEVOS_GROUND_TRUTH);
            when(vevosConfiguration.getVevosSplRepositoryBasePath()).thenReturn(VEVOS_REPO);
            when(config.getDataset()).thenReturn(REPO_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void teardown() throws Exception {
        DirectoryUtils.deleteFolderIfItExists(this.creationSamplePath);
        DirectoryUtils.deleteFolderIfItExists(this.recreationSamplePath);
    }

    @Test
    public void recreatedSampleIsIdenticalToOriginal() throws Resources.ResourceIOException, IOException {
        VevosFeatureSampler sampler = new VevosFeatureSampler();

        // create sample
        sampler.sample(this.config);

        // recreate sample
        List<Path> variantPaths = VevosUtils.getVariantFolders(creationSamplePath);
        List<String> configList = new LinkedList<>();
        for (Path variantPath : variantPaths){
            configList.add(VevosUtils.variantPathToConfigString(variantPath));
        }
        List<List<String>> featureLists = configList.stream().map(variantConfiguration -> {
            String[] featureArray = variantConfiguration.split(",");
            List<String> featureNames = new java.util.ArrayList<>(Arrays.stream(featureArray).map(String::trim).toList());
            featureNames.remove("BASE");
            return featureNames;
        }).toList();

        sampler.createSampleVariants(VEVOS_GROUND_TRUTH_BASE, REPO_NAME, recreationSamplePath, featureLists);

        File creationFolder = new File(creationSamplePath.toUri());
        File recreationFolder = new File(recreationSamplePath.toUri());

        // compare original with recreation
        assertTrue(DirectoryUtils.foldersAreEqual(creationFolder.toPath(), recreationFolder.toPath()));
    }
}
