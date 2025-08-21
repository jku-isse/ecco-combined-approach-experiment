package anon.ecco.experiment.result.persister;

import anon.ecco.experiment.config.Boosting;
import anon.ecco.experiment.config.ExperimentIterationConfiguration;
import anon.ecco.experiment.result.Result;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;


public class ResultDatabasePersister implements ResultPersister{
    private final String databaseURL;
    private final static String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    public ResultDatabasePersister(String databaseFolderPath){
        Path databaseFullPath = (Paths.get(databaseFolderPath)).resolve("results.db");
        this.databaseURL = SQLITE_URL_PREFIX + databaseFullPath;

        if (!this.databaseExists(databaseFullPath.toString())){
            this.createDatabase();
            this.createResultTable();
        }
    }

    private boolean databaseExists(String databasePath){
        File databaseFile = new File(databasePath);
        return databaseFile.exists() && databaseFile.isFile();
    }

    @Override
    public void persist(Result result, ExperimentIterationConfiguration config, boolean boosting) {
        String sql = "INSERT INTO results (dataset, numberOfCommittedVariants, committedVariantConfigurations, " +
                "numberOfSampledFeatures, sampledFeatures, featureTracePercentage, mistakePercentage, " +
                "evaluationStrategy, mistakeType, tp, fp, tn, fn, precision, recall, f1, boost, featureTracePicker) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        List<String> variantPickConfigurations = config.getVariantPick().getVariantPickConfigurations();
        String variantConfigsString = String.join("; ", variantPickConfigurations);
        List<String> features = config.getVevosConfiguration().gatherSampledFeatures();

        try (Connection conn = DriverManager.getConnection(this.databaseURL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, config.getDataset());
            pstmt.setInt(2, variantPickConfigurations.size());
            pstmt.setString(3, variantConfigsString);
            pstmt.setInt(4, features.size());
            pstmt.setString(5, String.join(", ", features));
            pstmt.setInt(6, config.getInputConfiguration().getFeatureTracePercentage());
            pstmt.setInt(7, config.getInputConfiguration().getMistakePercentage());
            pstmt.setString(8, config.getEvaluationConfiguration().getEvaluationStrategy().getStrategyName());
            pstmt.setString(9, config.getInputConfiguration().getMistakeStrategyName());
            pstmt.setInt(10, result.getTp());
            pstmt.setInt(11, result.getFp());
            pstmt.setInt(12, result.getTn());
            pstmt.setInt(13, result.getFn());
            pstmt.setDouble(14, result.getPrecision());
            pstmt.setDouble(15, result.getRecall());
            pstmt.setDouble(16, result.getF1());
            int boost = boosting ? 1 : 0;
            pstmt.setInt(17, boost);
            pstmt.setString(18, config.getEvaluationConfiguration().getListPicker().getClass().getName());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Entering data to database failed: " + e.getMessage());
        }
    }

    @Override
    public void cleanup(ExperimentIterationConfiguration config) {
        if (config.getVariantPick() == null) {
            return;
        }
        String sql = "DELETE FROM results "
                    + "WHERE committedVariantConfigurations = ?";
        try (Connection conn = DriverManager.getConnection(this.databaseURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, config.getVariantPick().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cleaning up database failed for configuration "
                                        + config.getVariantPick().toString() + ": " + e.getMessage());
        }
    }

    @Override
    public boolean iterationIsNecessary(ExperimentIterationConfiguration config) {
        String sql = "SELECT COUNT(*) FROM results WHERE"
                + "	dataset = ? AND"
                + "	numberOfCommittedVariants = ? AND"
                + "	featureTracePercentage = ? AND"
                + "	mistakePercentage = ? AND"
                + "	evaluationStrategy = ? AND"
                + "	mistakeType = ? AND"
                + " featureTracePicker = ?;";
        int count = 0;
        try (Connection conn = DriverManager.getConnection(this.databaseURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, config.getDataset());
            pstmt.setInt(2, config.getInputConfiguration().getNumberOfVariants());
            pstmt.setInt(3, config.getInputConfiguration().getFeatureTracePercentage());
            pstmt.setInt(4, config.getInputConfiguration().getMistakePercentage());
            pstmt.setString(5, config.getEvaluationConfiguration().getEvaluationStrategy().getStrategyName());
            pstmt.setString(6, config.getInputConfiguration().getMistakeStrategyName());
            pstmt.setString(7, config.getEvaluationConfiguration().getListPicker().getClass().getName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Checking iterations in database failed for configuration "
                    + config + "\n: " + e.getMessage());
        }

        int multiplicator = 1;
        if (config.getEvaluationConfiguration().getBoosting() == Boosting.BOTH){
            multiplicator = 2;
        }

        return count < (config.getNumberOfRepetitions() * multiplicator);
    }

    private void createResultTable(){
        String sql = "CREATE TABLE IF NOT EXISTS results ("
                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "	dataset TEXT NOT NULL,"
                + "	numberOfCommittedVariants INTEGER NOT NULL,"
                + "	committedVariantConfigurations TEXT NOT NULL,"
                + "	numberOfSampledFeatures INTEGERS NOT NULL,"
                + "	sampledFeatures TEXT NOT NULL,"
                + "	featureTracePercentage INTEGER NOT NULL,"
                + "	mistakePercentage INTEGER NOT NULL,"
                + "	evaluationStrategy TEXT NOT NULL,"
                + "	mistakeType TEXT NOT NULL,"
                + "	tp INTEGER NOT NULL,"
                + "	fp INTEGER NOT NULL,"
                + "	tn INTEGER NOT NULL,"
                + " fn INTEGER NOT NULL,"
                + "	precision DOUBLE NOT NULL,"
                + "	recall DOUBLE NOT NULL,"
                + "	f1 DOUBLE NOT NULL,"
                + " boost INTEGER NOT NULL,"
                + " featureTracePicker TEXT NOT NULL);";

        try (Connection conn = DriverManager.getConnection(this.databaseURL)){
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Creating result table in database failed: " + e.getMessage());
        }
    }

    private void createDatabase() {
        try {
            DriverManager.getConnection(this.databaseURL);
        } catch (SQLException e) {
            throw new RuntimeException("Creation of sqlite database failed: " + e.getMessage());
        }
    }
}
