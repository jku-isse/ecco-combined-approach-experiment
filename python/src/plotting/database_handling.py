import sqlite3

import utils

VALID_METRICS = ['f1', 'precision', 'recall']

class DatabaseHandler:

    def __init__(self, db_path):
        self.db_path = db_path

    def get_ft_experiment_data(self, dataset, metric, number_of_variants, ft_percentage, boost):
        DatabaseHandler.check_metric(metric)
        prepared_statement = (
            f"SELECT {metric} FROM results WHERE dataset=? AND numberOfCommittedVariants=? AND featureTracePercentage=? AND boost=? AND mistakeType=\'NoMistake\';")
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute(prepared_statement, (dataset, number_of_variants, ft_percentage, utils.boolean2int(boost)))
            results = cursor.fetchall()
            return results

    def get_ft_experiment_data_all_variant_numbers(self, dataset, metric, ft_percentage, boost):
        DatabaseHandler.check_metric(metric)
        prepared_statement = (
            f"SELECT {metric}, numberOfCommittedVariants, featureTracePercentage FROM results WHERE dataset=? AND featureTracePercentage=? AND boost=? AND mistakeType=\'NoMistake\';")
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute(prepared_statement, (dataset, ft_percentage, utils.boolean2int(boost)))
            results = cursor.fetchall()
            return results

    def get_mistake_experiment_data(self, dataset, metric, number_of_variants, ft_percentage, mistake_type, mistake_percentage, boost, feature_trace_picker):
        DatabaseHandler.check_metric(metric)
        prepared_statement = (
            f"SELECT {metric} FROM results WHERE dataset=? AND numberOfCommittedVariants=? AND featureTracePercentage=? AND mistakeType=? AND mistakePercentage=? AND boost=? AND featureTracePicker=?")
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute(prepared_statement, (dataset, number_of_variants, ft_percentage, mistake_type, mistake_percentage, utils.boolean2int(boost), feature_trace_picker))
            results = cursor.fetchall()
            return results

    @staticmethod
    def check_metric(metric):
        if metric not in VALID_METRICS:
            raise ValueError(f"Invalid metric: {metric}")
