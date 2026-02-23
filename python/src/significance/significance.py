import json
from scipy import stats
import sys
from pathlib import Path

parent_dir = str(Path(__file__).resolve().parent.parent)
if parent_dir not in sys.path:
    sys.path.append(parent_dir)

import config.config as config
import database.database_handling as database_handling

OUTPUT_PATH = r'/home/user/results/significance/significance_results.json'

class SignificanceTester:

    def __init__(self):
        self.significance_results = []
        self.database_handler = database_handling.DatabaseHandler(config.DATABASE_PATH)

    def perform_significance_tests(self):
        for dataset in config.SYSTEMS:
            for metric in config.METRICS:
                for number_of_variants in config.VARIANT_NUMBERS:
                    for ft_percentage in config.TRACE_PERCENTAGES:
                        if ft_percentage == 0:
                            continue
                        for boost in [False, True]:
                            configuration = {
                                'dataset': dataset,
                                'metric': metric,
                                'number_of_variants': number_of_variants,
                                'ft_percentage': ft_percentage,
                                'boost': boost,
                            }


                            baseline_data = self.database_handler.get_significance_base_data(configuration)
                            baseline_data = sorted(baseline_data, key=lambda x: x[1])

                            result_data = self.database_handler.get_significance_data_replication(configuration)
                            result_data = sorted(result_data, key=lambda x: x[1])

                            self.perform_significance_test(baseline_data, result_data, configuration)

                            for mistake_type in config.MISTAKE_TYPES:
                                for mistake_percentage in config.MISTAKE_PERCENTAGES:
                                    configuration['mistake_type'] = mistake_type
                                    configuration['mistake_percentage'] = mistake_percentage

                                    result_data = self.database_handler.get_significance_data_mistake(configuration)
                                    result_data = sorted(result_data, key=lambda x: x[1])

                                    self.perform_significance_test(baseline_data, result_data, configuration)

        with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
            json.dump(self.significance_results, f, indent=2)

    def perform_significance_test(self, baseline_data, result_data, configuration):
        # validate data
        for baseline, result in zip(baseline_data, result_data):
            if baseline[1] != result[1]:
                raise Exception('committed configurations in lists are not sorted equally for configuration: ' + configuration)    
        if len(baseline_data) != 30 or len(result_data) != 30:
            raise Exception()
        
        baseline_metrics = [t[0] for t in baseline_data]
        result_metrics = [t[0] for t in result_data]
        metric_differences = [t1[0] - t2[0] for t1, t2 in zip(result_data, baseline_data)]

        result = {}
        result['configuration'] = configuration.copy()
        stat, p_not_normal = stats.shapiro(metric_differences)
        result['shapiro'] = p_not_normal
        result['normally_distributed'] = bool(p_not_normal > 0.05)
        stat, p = stats.ttest_rel(result_metrics, baseline_metrics, alternative="greater")
        result['ttest'] = p
        result['ttest_significant'] = bool(p < 0.05)
        stat, p = stats.wilcoxon(result_metrics, baseline_metrics, alternative="greater")
        result['wilcoxon'] = p
        result['wilcoxon_significant'] = bool(p < 0.05)

        self.significance_results.append(result)


if __name__ == '__main__':
    tester = SignificanceTester()
    tester.perform_significance_tests()


