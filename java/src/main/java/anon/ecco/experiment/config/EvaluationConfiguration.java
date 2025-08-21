package anon.ecco.experiment.config;

import anon.ecco.experiment.picker.FeatureTraceMemoryListPicker;
import at.jku.isse.ecco.featuretrace.evaluation.EvaluationStrategy;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EvaluationConfiguration implements Cloneable{

    private FeatureTraceMemoryListPicker listPicker;
    private EvaluationStrategy evaluationStrategy;
    private Boosting boosting;

    @Override
    public String toString(){
        return String.format("""
                Evaluation Configuration:
                \tFeature Trace Picker: %s
                \tFeature Trace Evaluation Strategy: %s
                \tBoosting: %s
                """,
                this.listPicker.getClass(),
                this.evaluationStrategy,
                this.boosting);
    }

    @Override
    public EvaluationConfiguration clone() {
        try {
            return (EvaluationConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
