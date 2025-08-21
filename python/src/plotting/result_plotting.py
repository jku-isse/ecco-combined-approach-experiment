import traceback
from typing import List

import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
import pandas as pd
import os

import database_handling
import utils

# general settings
TARGET_PATH = r'/home/user/results/images'
DATABASE_PATH = r'/home/user/java/build/resources/main/database/results.db'
SYSTEMS = ['argouml-spl', 'berkeley-db-libdb', 'busybox', 'dia', 'apache-httpd', 'irssi', 'libssh', 'openvpn', 'vim']
METRICS = ['f1', 'precision', 'recall']
VARIANT_NUMBERS = [3, 5, 7]
# replication experiment settings
TRACE_PERCENTAGES = [0, 5, 10, 15, 20, 25]
# robustness experiment settings
MISTAKE_TYPES = ["SwappedCondition", "ErroneousConjunction", "SwappedFeature"]
MISTAKE_PERCENTAGES = [25, 50, 75, 100] # excluding 0


class ResultPlotter:

    def __init__(self, target_path: str,
                 database_path: str,
                 systems: List[str],
                 metrics: List[str],
                 variant_numbers: List[int],
                 trace_percentages: List[int],
                 mistake_types: List[str],
                 mistake_percentages: List[int]):
        self.target_path = target_path
        self.database_handler = None
        self.database_path = database_path
        self.systems = systems
        self.metrics = metrics
        self.variant_numbers = variant_numbers
        self.trace_percentages = trace_percentages
        self.mistake_types = mistake_types
        mistake_percentages.sort()
        self.mistake_percentages = mistake_percentages

    def create_database_handler(self):
        self.database_handler = database_handling.DatabaseHandler(self.database_path)

    def plot_results(self):
        for system in self.systems:
            self.create_database_handler()
            for metric in self.metrics:
                for boost in [True, False]:
                    self.plot_replication_results(system, metric, boost)
                    self.plot_robustness_results(system, metric, boost)

    def plot_replication_results(self, system: str, metric: str, boost: bool):
        combined_data = []
        for trace_percentage in self.trace_percentages:
            result_entries = self.database_handler.get_ft_experiment_data_all_variant_numbers(system, metric, trace_percentage, boost)
            for result_entry in result_entries:
                combined_data.append({
                    metric: result_entry[0],
                    'variants': result_entry[1],
                    'trace percentage': result_entry[2]
                })

        df = pd.DataFrame(combined_data)

        plt.rcParams['axes.titlesize'] = 20  # Title font size
        plt.rcParams['axes.labelsize'] = 18  # Axis label font size
        plt.rcParams['xtick.labelsize'] = 14  # X-tick label font size
        plt.rcParams['ytick.labelsize'] = 14  # Y-tick label font size
        plt.rcParams['legend.fontsize'] = 15  # Legend font size
        plt.rcParams['grid.linewidth'] = 0.8  # Set grid line width
        plt.rcParams['grid.color'] = 'lightgrey'  # Set grid color to light grey
        plt.grid(True)

        plt.figure(figsize=(14, 8))
        custom_palette = ["#92C5F9", "#AFDC8F", "#F8AE54"]
        sns.boxplot(data=df, x='trace percentage', y=metric, hue='variants', palette=custom_palette)
        plt.title('')

        plt.ylabel(ResultPlotter.get_metric_pretty_name(metric))
        plt.xlabel('Proactive Feature Trace Percentage')
        plt.legend(title='# Variants')
        plt.grid(True, linestyle='--', alpha=0.7)
        plt.ylim(0, 1)

        file_path = os.path.join(self.target_path, f'{system}_combined_{metric}_boost{utils.boolean2int(boost)}.png')
        plt.savefig(file_path, dpi=300)

    @staticmethod
    def get_metric_pretty_name(metric: str):
        match metric:
            case 'f1':
                return 'F1-Score'
            case 'precision':
                return 'Precision'
            case 'recall':
                return 'Recall'
            case _:
                traceback.print_stack()
                raise ValueError(f"The given metric name is invalid: {metric}")

    def plot_robustness_results(self, system: str, metric: str, boost: bool):
        for trace_percentage in self.trace_percentages:
            for variant_number in self.variant_numbers:
                for mistake_type in self.mistake_types:
                    self.plot_robustness_result(system, metric, variant_number, mistake_type, trace_percentage, boost)

    def plot_robustness_result(self, system, metric, variant_number, mistake_type, trace_percentage, boost):
        if trace_percentage == 0:
            return

        df = self.get_robustness_dataframes(system, metric, variant_number, mistake_type, trace_percentage, boost)

        # Create the boxplot with customizations
        fig = plt.figure(figsize=(12, 8))
        boxprops = dict(linestyle='-', linewidth=1.5)  # Slimmer lines for the box edges
        whiskerprops = dict(linestyle='--', linewidth=1)  # Slimmer lines for whiskers

        sns.boxplot(x='dataset', y='value', hue='condition', data=df,
                    palette = 'Set2',
                    width=0.4,  # Narrower boxes
                    boxprops=boxprops,
                    whiskerprops=whiskerprops)

        plt.ylim(0, 1)
        plt.yticks(np.arange(0, 1.1, 0.1))

        plt.xlabel('', fontsize=14)
        plt.ylabel("F1-Score", fontsize=14)
        plt.grid(True, linestyle='--', alpha=0.7)  # Add grid lines

        # Add a legend to differentiate conditions
        plt.legend(title='Total Proactive Trace Percentage', loc='lower left')

        # Add placeholder titles manually using figure coordinates
        fig.text(0.18, 0.055, "Correct Trace %:\nFaulty Trace %:", ha='right', fontsize=12)
        #fig.text(0.24, 0.03, "Faulty Trace %::", ha='right', fontsize=10)

        # Save and show the plot
        file_path = os.path.join(self.target_path, f'{system}_{metric}_{variant_number}variants_boost{utils.boolean2int(boost)}_{trace_percentage}ft_{mistake_type}.png')
        plt.savefig(file_path, dpi=300)
        matplotlib.pyplot.close()

    def get_robustness_dataframes(self, dataset, metric, variant_number, mistake_type, trace_percentage, boost):
        data1 = self.database_handler.get_mistake_experiment_data(dataset, metric, variant_number,0, "NoMistake", 0, boost,
                                                    "anon.ecco.experiment.picker.featuretracepicker.RandomFeatureTracePicker")
        dfs = []

        data1 = [item[0] for item in data1]
        condition1 = ['0'] * len(data1)
        df1 = pd.DataFrame({'value': data1, 'dataset': '0\n0', 'condition': condition1})
        dfs.append(df1)

        data2 = self.database_handler.get_mistake_experiment_data(dataset, metric, variant_number, trace_percentage, "NoMistake", 0, boost,
                                                    "anon.ecco.experiment.picker.featuretracepicker.RandomFeatureTracePicker")
        data2 = [item[0] for item in data2]
        condition2 = [trace_percentage] * len(data2)
        df2 = pd.DataFrame({'value': data2, 'dataset': f'{trace_percentage}\n0', 'condition': condition2})
        dfs.append(df2)

        for mistake_percentage in self.mistake_percentages:
            amount_faulty = trace_percentage * (mistake_percentage / 100)
            amount_correct = trace_percentage - amount_faulty
            data = self.database_handler.get_mistake_experiment_data(dataset, metric, variant_number, trace_percentage, mistake_type, mistake_percentage, boost,
                                                                      "anon.ecco.experiment.picker.featuretracepicker.RandomFeatureTracePicker")
            data = [item[0] for item in data]
            condition = [trace_percentage] * len(data)
            df = pd.DataFrame(
            {'value': data, 'dataset': f'{amount_correct}\n{amount_faulty}', 'condition': condition})
            dfs.append(df)

        return pd.concat(dfs)


if __name__ == "__main__":
    plotter = ResultPlotter(TARGET_PATH, DATABASE_PATH, SYSTEMS, METRICS, VARIANT_NUMBERS, TRACE_PERCENTAGES, MISTAKE_TYPES, MISTAKE_PERCENTAGES)
    plotter.plot_results()
