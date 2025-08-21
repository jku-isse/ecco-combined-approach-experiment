package anon.ecco.experiment.config;

import anon.ecco.experiment.utils.property.PropertyUtils;
import anon.ecco.experiment.utils.vevos.ConfigTransformer;
import lombok.Getter;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Getter
public class VevosConfiguration implements Cloneable{

    private final int minVariantFeatures;
    private final int maxVariantFeatures;
    private Path vevosSplRepositoryBasePath;
    private Path vevosGroundTruthDatasetPath;
    private final Path variantsBaseDir;

    public VevosConfiguration(String configFilePath, Path variantBasePath){
        Properties configProperties = PropertyUtils.loadProperties(configFilePath);
        this.minVariantFeatures = PropertyUtils.loadInteger(configProperties, "minVariantFeatures");
        this.maxVariantFeatures = PropertyUtils.loadInteger(configProperties, "maxVariantFeatures");
        this.vevosGroundTruthDatasetPath = PropertyUtils.loadPath(configProperties, "vevosGroundTruthDatasetPath");
        this.vevosSplRepositoryBasePath = this.vevosGroundTruthDatasetPath.resolve("REPOS");
        this.variantsBaseDir = variantBasePath;
    }

    public List<String> gatherSampledFeatures(){
        Path variantConfigsPath = this.variantsBaseDir.resolve("configs");
        return List.of(ConfigTransformer.gatherConfigFeatures(variantConfigsPath, maxVariantFeatures));
    }

    public List<String> gatherSampledFeaturesIncludingBase(){
        List<String> literalsIncludingBase = new LinkedList<>(this.gatherSampledFeatures());
        literalsIncludingBase.add("BASE");
        return literalsIncludingBase;
    }

    public void finishVevosPaths(String dataset){
        this.vevosSplRepositoryBasePath = this.vevosSplRepositoryBasePath.resolve(dataset);
        this.vevosGroundTruthDatasetPath = this.vevosGroundTruthDatasetPath.resolve(dataset);
    }

    @Override
    public String toString(){
        return String.format("""
                        Vevos Configuration:
                        \tMinimum Sampled Features: %d
                        \tMaximum Sampled Features: %d
                        \tSPL Repository Path: %s
                        \tGround Truth Path: %s
                        \tSample Path: %s
                        """,
                this.minVariantFeatures,
                this.maxVariantFeatures,
                this.vevosSplRepositoryBasePath,
                this.vevosGroundTruthDatasetPath,
                this.variantsBaseDir);
    }

    @Override
    public VevosConfiguration clone() {
        try {
            return (VevosConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
