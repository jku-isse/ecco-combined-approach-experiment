package anon.ecco.experiment.config;

import anon.ecco.experiment.picker.variantspick.VariantPick;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ExperimentIterationConfiguration implements Cloneable{

    private final int numberOfRepetitions;
    private VevosConfiguration vevosConfiguration;
    @Setter
    private String dataset;
    @Setter
    private InputConfiguration inputConfiguration;
    @Setter
    private EvaluationConfiguration evaluationConfiguration;
    @Setter
    private VariantPick variantPick;

    public ExperimentIterationConfiguration(int numberOfRepetitions, VevosConfiguration vevosConfiguration){
        this.numberOfRepetitions = numberOfRepetitions;
        this.vevosConfiguration = vevosConfiguration;
    }

    public void pickVariants(){
        this.variantPick = new VariantPick(this.vevosConfiguration.getVariantsBaseDir(),
                this.inputConfiguration.getNumberOfVariants());
    }

    @Override
    public String toString(){
        return String.format("""
                Experiment Iteration Configuration:
                \tNumber of Iteration Repetitions: %d
                \tDataset: %s
                
                \t%s
                \t%s
                \t%s
                \t%s
                """,
                this.numberOfRepetitions,
                this.dataset,
                this.inputConfiguration,
                this.evaluationConfiguration,
                this.vevosConfiguration,
                this.variantPick);
    }

    @Override
    public ExperimentIterationConfiguration clone() {
        try {
            ExperimentIterationConfiguration clone = (ExperimentIterationConfiguration) super.clone();
            if (this.vevosConfiguration != null) {
                clone.vevosConfiguration = this.vevosConfiguration.clone();
            }
            if (this.evaluationConfiguration != null) {
                clone.evaluationConfiguration = this.evaluationConfiguration.clone();
            }
            if (this.inputConfiguration != null) {
                clone.inputConfiguration = this.inputConfiguration.clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
