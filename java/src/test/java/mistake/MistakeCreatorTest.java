package mistake;

import anon.ecco.experiment.mistake.strategy.*;
import at.jku.isse.ecco.core.Association;
import at.jku.isse.ecco.core.Commit;
import at.jku.isse.ecco.core.Variant;
import at.jku.isse.ecco.dao.EntityFactory;
import anon.ecco.experiment.mistake.MistakeCreator;
import anon.ecco.experiment.mistake.MistakeException;
import at.jku.isse.ecco.feature.Configuration;
import at.jku.isse.ecco.feature.Feature;
import at.jku.isse.ecco.featuretrace.FeatureTrace;
import at.jku.isse.ecco.featuretrace.LogicUtils;
import at.jku.isse.ecco.featuretrace.evaluation.EvaluationStrategy;
import at.jku.isse.ecco.logic.FormulaFactoryProvider;
import at.jku.isse.ecco.maintree.building.MainTreeBuildingStrategy;
import at.jku.isse.ecco.module.Module;
import at.jku.isse.ecco.repository.Repository;
import at.jku.isse.ecco.tree.Node;
import mistake.stubs.FeatureTraceStub;
import org.junit.jupiter.api.Test;
import org.logicng.formulas.Formula;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MistakeCreatorTest {

    String FAULTY_CONDITION = "faulty condition";
    String CORRECT_CONDITION = "correct condition";
    String DIFFERENT_CORRECT_CONDITION = "different correct condition";

    @Mock
    Repository.Op repositoryMock;

    MistakeStrategy mistakeStrategyStub = new MistakeStrategy() {
        @Override
        protected String createNewMistake(FeatureTrace trace) {
            trace.setProactiveCondition(FAULTY_CONDITION);
            return FAULTY_CONDITION;
        }
    };

    @Test
    public void originalConditionsAreRestored(){
        FeatureTrace firstTrace = new FeatureTraceStub(CORRECT_CONDITION);
        FeatureTrace secondTrace = new FeatureTraceStub(CORRECT_CONDITION);
        Collection<FeatureTrace> traceCollection = new ArrayList<>(2);
        traceCollection.add(firstTrace);
        traceCollection.add(secondTrace);

        MistakeCreator mistakeCreator = new MistakeCreator(mistakeStrategyStub);
        mistakeCreator.createMistakePercentage(repositoryMock, traceCollection, 100);
        mistakeCreator.restoreOriginalConditions();

        assertEquals(2, traceCollection.stream().filter(trace -> trace.getProactiveConditionString().equals(CORRECT_CONDITION)).toList().size());
    }

    @Test
    public void originalConditionsAreRestoredForSameMistakesAndDifferentOriginalConditions(){
        FeatureTrace firstTrace = new FeatureTraceStub(CORRECT_CONDITION);
        FeatureTrace secondTrace = new FeatureTraceStub(DIFFERENT_CORRECT_CONDITION);
        Collection<FeatureTrace> traceCollection = new ArrayList<>(2);
        traceCollection.add(firstTrace);
        traceCollection.add(secondTrace);

        MistakeCreator mistakeCreator = new MistakeCreator(mistakeStrategyStub);
        mistakeCreator.createMistakePercentage(repositoryMock, traceCollection, 100);
        mistakeCreator.restoreOriginalConditions();

        assertEquals(CORRECT_CONDITION, firstTrace.getProactiveConditionString());
        assertEquals(DIFFERENT_CORRECT_CONDITION, secondTrace.getProactiveConditionString());
    }

    @Test
    public void formulaSyntaxTest(){
        Formula simpleFormula = LogicUtils.parseString("A");
        System.out.println(simpleFormula.toString());

        Formula conjunction = LogicUtils.parseString("A & B");
        System.out.println(conjunction.toString());

        Formula disjunction = LogicUtils.parseString("A | B");
        System.out.println(disjunction.toString());

        Formula negation = LogicUtils.parseString("~A");
        System.out.println(negation.toString());

        Formula tautology = LogicUtils.parseString("$true");
        System.out.println(tautology.toString());
    }

    @Test
    public void formulaConjunctionTest(){
        Formula disjunction = LogicUtils.parseString("A | B");
        Formula simpleFormula = LogicUtils.parseString("C");
        Formula conjunction = FormulaFactoryProvider.getFormulaFactory().and(disjunction, simpleFormula);
        System.out.println(conjunction);
    }


    @Test
    public void swappedConditionSwapsCondition(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A | B");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);

        SwappedCondition swappedCondition = new SwappedCondition();
        swappedCondition.init(repo);

        swappedCondition.createMistake(trace1);
        assertNotEquals("A & B", trace1.getProactiveConditionString());
    }

    @Test
    public void swappedConditionCantSwapIfAllConditionsAreTheSame(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);

        SwappedCondition swappedCondition = new SwappedCondition();
        swappedCondition.init(repo);

        assertThrows(RuntimeException.class, () -> swappedCondition.createMistake(trace1));
    }

    @Test
    public void swappedConditionThrowsExceptionWhenTryingToCreateMistakeWithoutInit(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        SwappedCondition swappedCondition = new SwappedCondition();
        assertThrows(RuntimeException.class, () -> swappedCondition.createMistake(trace1));
    }


    @Test
    public void erroneousConjunctionCreatesBiggerConjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        String[] features = {"A", "B", "C"};
        ErroneousConjunction erroneousConjunction = new ErroneousConjunction(List.of(features));
        erroneousConjunction.init(null);
        erroneousConjunction.createMistake(trace1);
        assertEquals("A & B & C", trace1.getProactiveConditionString());
    }

    @Test
    public void erroneousConjunctionCreatesConjunctionFromDisjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A | B");
        String[] features = {"A", "B", "C"};
        ErroneousConjunction erroneousConjunction = new ErroneousConjunction(List.of(features));
        erroneousConjunction.init(null);
        erroneousConjunction.createMistake(trace1);
        assertEquals("(A | B) & C", trace1.getProactiveConditionString());
    }

    @Test
    public void erroneousConjunctionWorksWithoutInit(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("~A");
        String[] features = {"A", "B"};
        ErroneousConjunction erroneousConjunction = new ErroneousConjunction(List.of(features));
        erroneousConjunction.createMistake(trace1);
        assertEquals("~A & B", trace1.getProactiveConditionString());
    }

    @Test
    public void erroneousConjunctionThrowsExceptionIfThereAreNotEnoughFeatures(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A | B");
        String[] features = {"A", "B"};
        ErroneousConjunction erroneousConjunction = new ErroneousConjunction(List.of(features));
        erroneousConjunction.init(null);
        assertThrows(RuntimeException.class, () -> erroneousConjunction.createMistake(trace1));
    }

    @Test
    public void swappedFeatureSwapsFeature(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        String[] features = {"A", "B"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        swappedFeature.init(null);
        swappedFeature.createMistake(trace1);
        assertEquals("B", trace1.getProactiveConditionString());
    }

    @Test
    public void swappedFeatureSwapsFeatureInNegation(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("~A");
        String[] features = {"A", "B"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        swappedFeature.init(null);
        swappedFeature.createMistake(trace1);
        assertEquals("~B", trace1.getProactiveConditionString());
    }

    @Test
    public void swappedFeatureWorksWithoutInit(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        String[] features = {"A", "B", "C"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        swappedFeature.createMistake(trace1);
        assertTrue(trace1.getProactiveConditionString().equals("C & B") || trace1.getProactiveConditionString().equals("A & C"));
    }

    @Test
    public void swappedFeatureThrowsExceptionIfThereAreNoOtherFeatures(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        String[] features = {"A", "B"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        assertThrows(RuntimeException.class, () -> swappedFeature.createMistake(trace1));
    }

    @Test
    public void swappedFeatureThrowsExceptionIfThereAreNoFeaturesInTheCondition(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("$true");
        String[] features = {"A", "B"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        assertThrows(RuntimeException.class, () -> swappedFeature.createMistake(trace1));
    }

    @Test
    public void swappedOperatorSwapsConjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        SwappedOperator swappedOperator = new SwappedOperator();
        swappedOperator.init(null);
        swappedOperator.createMistake(trace1);
        assertEquals("A | B", trace1.getProactiveConditionString());
    }

    @Test
    public void swappedOperatorSwapsDisjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A | B");
        SwappedOperator swappedOperator = new SwappedOperator();
        swappedOperator.init(null);
        swappedOperator.createMistake(trace1);
        assertEquals("A & B", trace1.getProactiveConditionString());
    }

    @Test
    public void swappedOperatorWorksWithoutInit(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A | B | C");
        SwappedOperator swappedOperator = new SwappedOperator();
        swappedOperator.createMistake(trace1);
        assertEquals("A & B | C", trace1.getProactiveConditionString());
    }

    @Test
    public void swappedOperatorThrowsExceptionWhenThereAreNoOperators(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("~A");
        SwappedOperator swappedOperator = new SwappedOperator();
        assertThrows(RuntimeException.class, () -> swappedOperator.createMistake(trace1));
    }

    @Test
    public void missingConjunctionRemovesConjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        MissingConjunction missingConjunction = new MissingConjunction();
        missingConjunction.init(null);
        missingConjunction.createMistake(trace1);
        assertTrue(trace1.getProactiveConditionString().equals("A & $true") || trace1.getProactiveConditionString().equals("$true & B"));
    }

    @Test
    public void missingConjunctionWorksWithoutInit(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B");
        MissingConjunction missingConjunction = new MissingConjunction();
        missingConjunction.createMistake(trace1);
        assertTrue(trace1.getProactiveConditionString().equals("A & $true") || trace1.getProactiveConditionString().equals("$true & B"));
    }

    @Test
    public void missingConjunctionThrowsExceptionForNegation(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & ~B");
        MissingConjunction missingConjunction = new MissingConjunction();
        missingConjunction.init(null);
        assertThrows(RuntimeException.class, () -> missingConjunction.createMistake(trace1));
    }

    @Test
    public void missingConjunctionThrowsExceptionForDisjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A & B | C");
        MissingConjunction missingConjunction = new MissingConjunction();
        missingConjunction.init(null);
        assertThrows(RuntimeException.class, () -> missingConjunction.createMistake(trace1));
    }

    @Test
    public void missingConjunctionThrowsExceptionForMissingConjunction(){
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A | C");
        MissingConjunction missingConjunction = new MissingConjunction();
        missingConjunction.init(null);
        assertThrows(RuntimeException.class, () -> missingConjunction.createMistake(trace1));
    }

    @Test
    public void mistakeCreatorCreatesCorrectPercentage() {
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace4 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace5 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        traceCollection.add(trace4);
        traceCollection.add(trace5);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);
        String[] features = {"A", "B"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        MistakeCreator mistakeCreator = new MistakeCreator(swappedFeature);
        mistakeCreator.createMistakePercentage(repo, repo.getFeatureTraces(), 40);

        Collection<String> conditions = repo.getFeatureTraces().stream().map(FeatureTrace::getProactiveConditionString).toList();
        int unchanged = (int) conditions.stream().filter(s -> s.equals("A")).count();
        int changed = (int) conditions.stream().filter(s -> s.equals("B")).count();
        assertEquals(3, unchanged);
        assertEquals(2, changed);
    }

    @Test
    public void mistakeCreatorRoundsDown() {
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace4 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace5 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        traceCollection.add(trace4);
        traceCollection.add(trace5);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);
        String[] features = {"A", "B"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        MistakeCreator mistakeCreator = new MistakeCreator(swappedFeature);
        mistakeCreator.createMistakePercentage(repo, repo.getFeatureTraces(), 70);

        Collection<String> conditions = repo.getFeatureTraces().stream().map(FeatureTrace::getProactiveConditionString).toList();
        int unchanged = (int) conditions.stream().filter(s -> s.equals("A")).count();
        int changed = (int) conditions.stream().filter(s -> s.equals("B")).count();
        assertEquals(2, unchanged);
        assertEquals(3, changed);
    }

    @Test
    public void mistakeCreatorThrowsExceptionIfPercentageIsNotPossible() {
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace4 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace5 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        traceCollection.add(trace4);
        traceCollection.add(trace5);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);
        String[] features = {"A"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        MistakeCreator mistakeCreator = new MistakeCreator(swappedFeature);
        assertThrows(MistakeException.class, () -> mistakeCreator.createMistakePercentage(repo, repo.getFeatureTraces(), 20));
    }

    @Test
    public void mistakeCreatorThrowsExceptionIfPercentageSmallerZero() {
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace4 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace5 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        traceCollection.add(trace4);
        traceCollection.add(trace5);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);
        String[] features = {"A"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        MistakeCreator mistakeCreator = new MistakeCreator(swappedFeature);
        assertThrows(RuntimeException.class, () -> mistakeCreator.createMistakePercentage(repo, repo.getFeatureTraces(), -20));
    }

    @Test
    public void mistakeCreatorThrowsExceptionIfPercentageBiggerHundred() {
        mistake.MistakeCreatorTest.MockFeatureTrace trace1 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace2 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace3 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace4 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        mistake.MistakeCreatorTest.MockFeatureTrace trace5 = new mistake.MistakeCreatorTest.MockFeatureTrace("A");
        Collection<FeatureTrace> traceCollection = new LinkedList<>();
        traceCollection.add(trace1);
        traceCollection.add(trace2);
        traceCollection.add(trace3);
        traceCollection.add(trace4);
        traceCollection.add(trace5);
        mistake.MistakeCreatorTest.MockRepository repo = new mistake.MistakeCreatorTest.MockRepository(traceCollection);
        String[] features = {"A"};
        SwappedFeature swappedFeature = new SwappedFeature(List.of(features));
        MistakeCreator mistakeCreator = new MistakeCreator(swappedFeature);
        assertThrows(RuntimeException.class, () -> mistakeCreator.createMistakePercentage(repo, repo.getFeatureTraces(), 150));
    }

    static class MockFeatureTrace implements FeatureTrace {
        private String proactiveCondition;
        public MockFeatureTrace(String proactiveCondition){
            this.proactiveCondition = proactiveCondition;
        }

        @Override
        public boolean holds(Configuration configuration, EvaluationStrategy evaluationStrategy) {
            return false;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public void setNode(Node node) {

        }

        @Override
        public boolean containsProactiveCondition() {
            return false;
        }

        @Override
        public void setRetroactiveCondition(String retroactiveConditionString) {

        }

        @Override
        public void setProactiveCondition(String proactiveConditionString) {
            this.proactiveCondition = proactiveConditionString;
        }

        @Override
        public void addProactiveCondition(String proactiveCondition) {

        }

        @Override
        public void removeProactiveCondition() {

        }

        @Override
        public void addRetroactiveCondition(String retroactiveCondition) {

        }

        @Override
        public void buildProactiveConditionConjunction(String newCondition) {

        }

        @Override
        public String getProactiveConditionString() {
            return this.proactiveCondition;
        }

        @Override
        public String getRetroactiveConditionString() {
            return null;
        }

        @Override
        public void fuseFeatureTrace(FeatureTrace featureTrace) {

        }

        @Override
        public String getOverallConditionString(EvaluationStrategy evaluationStrategy) {
            return null;
        }
    }

    static class MockRepository implements Repository.Op {

        Collection<FeatureTrace> traces;

        public MockRepository(Collection<FeatureTrace> traces){
            this.traces = traces;
        }

        @Override
        public ArrayList<Variant> getVariants() {
            return null;
        }

        @Override
        public Variant getVariant(Configuration configuration) {
            return null;
        }

        @Override
        public Variant getVariant(String id) {
            return null;
        }

        @Override
        public Association getAssociation(String id) {
            return null;
        }

        @Override
        public ArrayList<Feature> getFeature() {
            return null;
        }

        @Override
        public void updateVariant(Variant variant, Configuration configuration, String name) {

        }

        @Override
        public Collection<Commit> getCommits() {
            return null;
        }

        @Override
        public void setCommits(Collection<Commit> commits) {

        }

        @Override
        public void addCommit(Commit commit) {

        }

        @Override
        public Collection<FeatureTrace> getFeatureTraces() {
            return this.traces;
        }

        @Override
        public void setMaintreeBuildingStrategy(MainTreeBuildingStrategy mainTreeBuildingStrategy) {

        }

        @Override
        public MainTreeBuildingStrategy getMainTreeBuildingStrategy() {
            return null;
        }

        @Override
        public void setEvaluationStrategy(EvaluationStrategy evaluationStrategy) {

        }

        @Override
        public EvaluationStrategy getEvaluationStrategy() {
            return null;
        }

        @Override
        public Collection<? extends Feature> getFeatures() {
            return null;
        }

        @Override
        public Collection<? extends Association.Op> getAssociations() {
            return null;
        }

        @Override
        public Collection<? extends at.jku.isse.ecco.module.Module> getModules(int order) {
            return null;
        }

        @Override
        public Feature getFeature(String id) {
            return null;
        }

        @Override
        public Feature getOrphanedFeature(String id, String name) {
            return null;
        }

        @Override
        public Feature addFeature(String id, String name) {
            return null;
        }

        @Override
        public void addVariant(Variant variant) {

        }

        @Override
        public void addAssociation(Association.Op association) {

        }

        @Override
        public void removeVariant(Variant variant) {

        }

        @Override
        public void removeAssociation(Association.Op association) {

        }

        @Override
        public at.jku.isse.ecco.module.Module getOrphanedModule(Feature[] posFeatures, Feature[] neg) {
            return null;
        }

        @Override
        public int getMaxOrder() {
            return 0;
        }

        @Override
        public void setMaxOrder(int maxOrder) {

        }

        @Override
        public EntityFactory getEntityFactory() {
            return null;
        }

        @Override
        public void buildMainTree() {

        }

        @Override
        public Node.Op getMainTree() {
            return null;
        }

        @Override
        public at.jku.isse.ecco.module.Module getModule(Feature[] pos, Feature[] neg) {
            return null;
        }

        @Override
        public Module addModule(Feature[] pos, Feature[] neg) {
            return null;
        }
    }
}
