package anon.ecco.experiment.config;

import anon.ecco.experiment.picker.FeatureTraceMemoryListPicker;
import anon.ecco.experiment.picker.featuretracepicker.RandomFeatureTracePicker;
import anon.ecco.experiment.utils.property.PropertyUtils;
import at.jku.isse.ecco.featuretrace.evaluation.EvaluationStrategy;
import lombok.Getter;

import java.nio.file.Path;
import java.util.*;

public class ExperimentConfigurationIterator implements Iterator<ExperimentIterationConfiguration> {

    private VevosConfiguration vevosConfiguration;
    private Properties configProperties;
    @Getter
    private int numberOfRepetitions;
    private LinkedList<ExperimentIterationConfiguration> iterationConfigs;

    public ExperimentConfigurationIterator(String configurationPath, Path variantBasePath) {
        this.vevosConfiguration = new VevosConfiguration(configurationPath, variantBasePath);
        this.configProperties = PropertyUtils.loadProperties(configurationPath);
        this.numberOfRepetitions = PropertyUtils.loadInteger(this.configProperties, "numberOfRepetitions");
        this.createExperimentIterationConfigurations();
    }

    /**
     * Returns a list of experiment iteration configurations that can all be applied to the same prepared repository.
     */
    public List<ExperimentIterationConfiguration> getNextConfigurationBatch(){
        LinkedList<ExperimentIterationConfiguration> configBatch = new LinkedList<>();
        ExperimentIterationConfiguration config = this.iterationConfigs.peek();
        int numberOfVariants = config.getInputConfiguration().getNumberOfVariants();
        String dataset = config.getDataset();
        while(dataset.equals(config.getDataset())
                && numberOfVariants == config.getInputConfiguration().getNumberOfVariants()){
            configBatch.add(this.iterationConfigs.pop());
            if (!this.iterationConfigs.isEmpty()) {
                config = this.iterationConfigs.peek();
            } else {
                break;
            }
        }
        return configBatch;
    }

    private void createExperimentIterationConfigurations(){
        int numberOfRepetitions = PropertyUtils.loadInteger(this.configProperties, "numberOfRepetitions");
        ExperimentIterationConfiguration templateIterationConfiguration = new ExperimentIterationConfiguration(numberOfRepetitions, vevosConfiguration);
        LinkedList<ExperimentIterationConfiguration> experimentIterationConfigurations = this.createDatasetIterations(templateIterationConfiguration);
        this.iterationConfigs = experimentIterationConfigurations;
    }

    private LinkedList<ExperimentIterationConfiguration> createDatasetIterations(ExperimentIterationConfiguration templateIterationConfiguration){
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<String> datasets = (LinkedList<String>) PropertyUtils.loadStringList(this.configProperties, "datasets");
        for (String dataset : datasets){
            ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
            configClone.getVevosConfiguration().finishVevosPaths(dataset);
            configClone.setDataset(dataset);
            configs.addAll(this.createNumberOfVariantIterations(configClone));
        }
        return configs;
    }

    private LinkedList<ExperimentIterationConfiguration> createNumberOfVariantIterations(ExperimentIterationConfiguration templateIterationConfiguration){
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<Integer> numbersOfVariants = (LinkedList<Integer>) PropertyUtils.loadIntegerList(this.configProperties, "numbersOfVariants");
        for (Integer numberOfVariants : numbersOfVariants){
            ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
            InputConfiguration inputConfiguration = new InputConfiguration();
            inputConfiguration.setNumberOfVariants(numberOfVariants);
            configClone.setInputConfiguration(inputConfiguration);
            configs.addAll(this.createListPickerIterations(configClone));
        }
        return configs;
    }

    private LinkedList<ExperimentIterationConfiguration> createListPickerIterations(ExperimentIterationConfiguration templateIterationConfiguration) {
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<FeatureTraceMemoryListPicker> listPickers = (LinkedList<FeatureTraceMemoryListPicker>) PropertyUtils.loadInstances(this.configProperties, "tracePickers", FeatureTraceMemoryListPicker.class);
        if (listPickers.isEmpty()){
            listPickers.add(new RandomFeatureTracePicker());
        }
        for (FeatureTraceMemoryListPicker listPicker : listPickers){
            ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
            EvaluationConfiguration evaluationConfiguration = new EvaluationConfiguration();
            evaluationConfiguration.setListPicker(listPicker);
            configClone.setEvaluationConfiguration(evaluationConfiguration);
            configs.addAll(this.createEvaluationStrategyIterations(configClone));
        }
        return configs;
    }

    private LinkedList<ExperimentIterationConfiguration> createEvaluationStrategyIterations(ExperimentIterationConfiguration templateIterationConfiguration) {
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<EvaluationStrategy> evaluationStrategies = (LinkedList<EvaluationStrategy>) PropertyUtils.loadInstances(this.configProperties, "evaluationStrategies", EvaluationStrategy.class);
        for (EvaluationStrategy evaluationStrategy : evaluationStrategies){
            ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
            EvaluationConfiguration evaluationConfiguration = configClone.getEvaluationConfiguration();
            evaluationConfiguration.setEvaluationStrategy(evaluationStrategy);
            configs.addAll(this.createFeatureTracePercentageIterations(configClone));
        }
        return configs;
    }

    private LinkedList<ExperimentIterationConfiguration> createFeatureTracePercentageIterations(ExperimentIterationConfiguration templateIterationConfiguration) {
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<Integer> featureTracePercentages = (LinkedList<Integer>) PropertyUtils.loadIntegerList(this.configProperties, "featureTracePercentages");
        for (Integer featureTracePercentage : featureTracePercentages){
            ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
            InputConfiguration inputConfiguration = configClone.getInputConfiguration();
            inputConfiguration.setFeatureTracePercentage(featureTracePercentage);
            configs.addAll(this.createMistakePercentageIterations(configClone));
        }
        return configs;
    }

    private LinkedList<ExperimentIterationConfiguration> createMistakePercentageIterations(ExperimentIterationConfiguration templateIterationConfiguration){
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<Integer> mistakePercentages = (LinkedList<Integer>) PropertyUtils.loadIntegerList(this.configProperties, "mistakePercentages");
        InputConfiguration inputConfiguration = templateIterationConfiguration.getInputConfiguration();
        int featureTracePercentage = inputConfiguration.getFeatureTracePercentage();
        if (featureTracePercentage == 0){
            inputConfiguration.setMistakePercentage(0);
            configs.addAll(this.createMistakeStrategyIterations(templateIterationConfiguration));
        } else {
            for (Integer mistakePercentage : mistakePercentages){
                ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
                inputConfiguration = configClone.getInputConfiguration();
                inputConfiguration.setMistakePercentage(mistakePercentage);
                configs.addAll(this.createMistakeStrategyIterations(configClone));
            }
        }
        return configs;
    }

    private LinkedList<ExperimentIterationConfiguration> createMistakeStrategyIterations(ExperimentIterationConfiguration templateIterationConfiguration) {
        LinkedList<ExperimentIterationConfiguration> configs = new LinkedList<>();
        LinkedList<String> mistakeStrategies = (LinkedList<String>) PropertyUtils.loadStringList(this.configProperties, "mistakeStrategies");
        InputConfiguration inputConfiguration = templateIterationConfiguration.getInputConfiguration();
        int mistakePercentage = inputConfiguration.getMistakePercentage();
        Boosting boosting = Boosting.valueOf(this.configProperties.getProperty("boosting"));
        if (mistakePercentage == 0){
            inputConfiguration.setMistakeStrategyName("NoMistake");
            EvaluationConfiguration evaluationConfiguration = templateIterationConfiguration.getEvaluationConfiguration();
            evaluationConfiguration.setBoosting(boosting);
            configs.add(templateIterationConfiguration);
        } else {
            for (String mistakeStrategy : mistakeStrategies) {
                ExperimentIterationConfiguration configClone = templateIterationConfiguration.clone();
                inputConfiguration = configClone.getInputConfiguration();
                inputConfiguration.setMistakeStrategyName(mistakeStrategy);
                EvaluationConfiguration evaluationConfiguration = configClone.getEvaluationConfiguration();
                evaluationConfiguration.setBoosting(boosting);
                configs.add(configClone);
            }
        }
        return configs;
    }

    public String toString(){
        return null;
    }

    @Override
    public boolean hasNext() {
        return !this.iterationConfigs.isEmpty();
    }

    @Override
    public ExperimentIterationConfiguration next() {
        return this.iterationConfigs.pop();
    }
}
