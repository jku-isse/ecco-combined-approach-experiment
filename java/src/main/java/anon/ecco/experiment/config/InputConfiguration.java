package anon.ecco.experiment.config;

import anon.ecco.experiment.mistake.strategy.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InputConfiguration implements Cloneable{

    private int numberOfVariants;
    private int featureTracePercentage;
    private int mistakePercentage;
    private String mistakeStrategyName;

    public MistakeStrategy getMistakeStrategy(List<String> features){
        return switch (this.mistakeStrategyName) {
            case "NoMistake" -> new NoMistake();
            case "SwappedCondition" -> new SwappedCondition();
            case "ErroneousConjunction" -> new ErroneousConjunction(features);
            case "SwappedFeature" -> new SwappedFeature(features);
            case "SwappedOperator" -> new SwappedOperator();
            case "MissingConjunction" -> new MissingConjunction();
            default -> throw new IllegalArgumentException("Unsupported mistake strategy type: " + mistakeStrategyName);
        };
    }

    @Override
    public String toString(){
        return String.format("""
                        Input Configuration:
                        \tNumber Of Variants To Commit: %d
                        \tProactive Feature Trace Percentage: %d
                        \tFaulty Feature Trace Percentage: %s
                        \tMistake Type: %s
                        """,
                this.numberOfVariants,
                this.featureTracePercentage,
                this.mistakePercentage,
                this.mistakeStrategyName);
    }

    @Override
    public InputConfiguration clone() {
        try {
            return (InputConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
