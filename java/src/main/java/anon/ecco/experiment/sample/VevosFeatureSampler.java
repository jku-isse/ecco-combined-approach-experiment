package anon.ecco.experiment.sample;

import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.utils.vevos.ConfigTransformer;
import anon.ecco.experiment.utils.vevos.VevosUtils;

import anon.ecco.experiment.utils.directory.DirectoryUtils;
import anon.ecco.experiment.utils.directory.DirectoryException;
import de.ovgu.featureide.fm.core.base.IFeature;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;
import org.variantsync.functjonal.Lazy;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.list.NonEmptyList;
import org.variantsync.vevos.simulation.VEVOS;
import org.variantsync.vevos.simulation.feature.Variant;
import org.variantsync.vevos.simulation.feature.SimpleFeature;
import org.variantsync.vevos.simulation.feature.config.SimpleConfiguration;
import org.variantsync.vevos.simulation.feature.sampling.Sample;
import org.variantsync.vevos.simulation.feature.sampling.SimpleSampler;
import org.variantsync.vevos.simulation.io.Resources;
import org.variantsync.vevos.simulation.repository.SPLRepository;
import org.variantsync.vevos.simulation.util.io.CaseSensitivePath;
import org.variantsync.vevos.simulation.util.names.NumericNameGenerator;
import org.variantsync.vevos.simulation.variability.SPLCommit;
import org.variantsync.vevos.simulation.variability.VariabilityDataset;
import org.variantsync.vevos.simulation.variability.VariabilityHistory;
import org.variantsync.vevos.simulation.variability.pc.Artefact;
import org.variantsync.vevos.simulation.variability.pc.SourceCodeFile;
import org.variantsync.vevos.simulation.variability.pc.groundtruth.GroundTruth;
import org.variantsync.vevos.simulation.variability.pc.options.ArtefactFilter;
import org.variantsync.vevos.simulation.variability.pc.options.VariantGenerationOptions;
import org.variantsync.vevos.simulation.variability.sequenceextraction.LongestNonOverlappingSequences;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class VevosFeatureSampler {

    private ExperimentIterationConfiguration config;
    private Path vevosSplRepositoryBasePath;
    private Path vevosGroundTruthDatasetPath;

    public VevosFeatureSampler(){
        VEVOS.Initialize();
    }

    public void sample(ExperimentIterationConfiguration config) throws Resources.ResourceIOException, IOException {
        Logger.info("Sampling variants...");
        this.config = config;
        this.vevosGroundTruthDatasetPath = config.getVevosConfiguration().getVevosGroundTruthDatasetPath();
        this.vevosSplRepositoryBasePath = config.getVevosConfiguration().getVevosSplRepositoryBasePath();
        this.sample(config.getInputConfiguration().getNumberOfVariants());
    }

    private void sample(int noVariants) throws Resources.ResourceIOException, IOException {
        int minVariantFeatures = config.getVevosConfiguration().getMinVariantFeatures();
        int maxVariantFeatures = config.getVevosConfiguration().getMaxVariantFeatures();
        SimpleSampler variantsSampler = SimpleSampler.CreateRandomSampler(noVariants, minVariantFeatures, maxVariantFeatures);
        this.sample(variantsSampler);
    }

    private void sample(SimpleSampler sampler) throws IOException, Resources.ResourceIOException {
        VariabilityDataset dataset = Resources.Instance().load(VariabilityDataset.class, this.vevosGroundTruthDatasetPath);
        VariabilityHistory history = dataset.getVariabilityHistory(new LongestNonOverlappingSequences());
        SPLRepository splRepository = new SPLRepository(this.vevosSplRepositoryBasePath);
        NonEmptyList<SPLCommit> subhistory = history.commitSequences().iterator().next();
        SPLCommit splCommit = subhistory.iterator().next();

        if (history.commitSequences().size() > 1 || subhistory.size() > 1){
            throw new RuntimeException("Repository does not have exactly one commit.");
        }

        Lazy<Optional<Artefact>> loadPresenceConditions = splCommit.presenceConditionsFallback();
        Artefact pcs = loadPresenceConditions.run().orElseThrow();
        List<IFeature> features = Files.readAllLines(splCommit.getFeatureModelPath()).stream().map(SimpleFeature::new)
                .collect(Collectors.toList());
        Sample variants = sampler.sample(features);

        ArtefactFilter<SourceCodeFile> artefactFilter = ArtefactFilter.KeepAll();
        VariantGenerationOptions generationOptions = VariantGenerationOptions.ExitOnError(false, artefactFilter);

        try {
            splRepository.checkoutCommit(splCommit);
        } catch (final GitAPIException | IOException e) {
            throw new RuntimeException("Failed to checkout commit " + splCommit.id() + " of "
                    + splRepository.getPath() + "!", e);
        }

        Path configsPath = config.getVevosConfiguration().getVariantsBaseDir().resolve("configs");
        Files.createDirectories(configsPath);

        for (Variant variant : variants) {
            this.createVariant(variant, config.getVevosConfiguration().getVariantsBaseDir(), pcs, generationOptions, configsPath, this.vevosSplRepositoryBasePath);
        }

        List<String> featureNames = List.of(ConfigTransformer.gatherConfigFeatures(this.config.getVevosConfiguration().getVariantsBaseDir().resolve("configs"), this.config.getVevosConfiguration().getMaxVariantFeatures()));
        eccoSampleExperimentPreparation(this.config.getVevosConfiguration().getVariantsBaseDir(), featureNames);
    }

    /**
     *
     * @param variantConfigurations List of List of feature-names. Each List of feature-names represents the
     *                              configuration of a variant to be created.
     */
    public void createSampleVariants(Path vevosGroundTruthBasePath, String repository, Path sampleBasePath,
                                     List<List<String>> variantConfigurations) throws Resources.ResourceIOException, IOException {
        Path vevosGroundTruthPath = vevosGroundTruthBasePath.resolve(repository);
        Path vevosSplRepositoryPath = vevosGroundTruthBasePath.resolve("REPOS").resolve(repository);
        Path configsPath = sampleBasePath.resolve("configs");

        VariabilityDataset dataset = Resources.Instance().load(VariabilityDataset.class, vevosGroundTruthPath);
        VariabilityHistory history = dataset.getVariabilityHistory(new LongestNonOverlappingSequences());
        NonEmptyList<SPLCommit> subhistory = history.commitSequences().iterator().next();
        SPLCommit splCommit = subhistory.iterator().next();
        Lazy<Optional<Artefact>> loadPresenceConditions = splCommit.presenceConditionsFallback();
        Artefact pcs = loadPresenceConditions.run().orElseThrow();
        NumericNameGenerator nameGenerator = new NumericNameGenerator("Variant");
        ArtefactFilter<SourceCodeFile> artefactFilter = ArtefactFilter.KeepAll();
        VariantGenerationOptions generationOptions = VariantGenerationOptions.ExitOnError(false, artefactFilter);
        final AtomicInteger variantNo = new AtomicInteger();

        Files.createDirectories(configsPath);

        for (List<String> featureNames : variantConfigurations){
            Variant variant = new Variant(nameGenerator.getNameAtIndex(variantNo.getAndIncrement()), new SimpleConfiguration(featureNames));
            this.createVariant(variant, sampleBasePath, pcs, generationOptions, configsPath, vevosSplRepositoryPath);
        }

        Set<String> features = new HashSet<>();
        variantConfigurations.forEach(features::addAll);
        this.eccoSampleExperimentPreparation(sampleBasePath, features.stream().toList());
    }

    private void createVariant(Variant variant, Path sampleBasePath, Artefact pcs, VariantGenerationOptions generationOptions, Path configsPath, Path vevosSplRepositoryPath) throws Resources.ResourceIOException, IOException {
        Path variantPath = sampleBasePath.resolve(variant.getName());
        CaseSensitivePath caseSensitiveVariantDir = CaseSensitivePath.of(variantPath.toString());
        CaseSensitivePath caseSensitiveSplRepositoryPath = CaseSensitivePath.of(vevosSplRepositoryPath.toString());

        Result<GroundTruth, Exception> result = pcs.generateVariant(variant,
                caseSensitiveSplRepositoryPath, caseSensitiveVariantDir, generationOptions);

        if (!result.isSuccess()) {
            throw new RuntimeException("Error upon generation of variant " + variant.getName());
        }

        GroundTruth groundTruth = result.getSuccess();
        Artefact presenceConditionsOfVariant = groundTruth.variant();
        Resources.Instance().write(Artefact.class, presenceConditionsOfVariant,
                variantPath.resolve("pcs.variant.csv"));

        String configFileName = variant.getName() + ".config";
        Path configFilePath = configsPath.resolve(configFileName);
        File configFile = new File(configFilePath.toUri());
        FileWriter fileWriter = new FileWriter(configFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(variant.getConfiguration().toString());
        printWriter.close();
    }

    private void eccoSampleExperimentPreparation(Path sampleBasePath, List<String> featuresWithoutBase){
        VevosUtils.sanitizeVevosConfigFiles(sampleBasePath);
        ConfigTransformer.transformConfigurations(sampleBasePath);
        VevosUtils.sanitizeVevosFiles(sampleBasePath, featuresWithoutBase);
    }

    public void cleanUp(){
        try {
            DirectoryUtils.deleteAndCreateFolder(this.config.getVevosConfiguration().getVariantsBaseDir());
        } catch (DirectoryException e) {
            throw new RuntimeException(e);
        }
    }
}

