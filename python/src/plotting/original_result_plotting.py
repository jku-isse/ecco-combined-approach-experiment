import os
import pandas as pd
import matplotlib.pyplot as plt
import json
import seaborn as sns
import traceback
import sys
from pathlib import Path

parent_dir = str(Path(__file__).resolve().parent.parent)
if parent_dir not in sys.path:
    sys.path.append(parent_dir)
    
import config.config as config

BASE_PATH = r'C:\Example\Path'


class OriginalResultPlotter:

    @staticmethod
    def plot_system_results(system: str, base_path: str):
        json_path = os.path.join(base_path, f'experiment_result_{system}.json')
        with open(json_path, 'r') as file:
            raw_data = json.load(file)
        OriginalResultPlotter.plot_boxplot(system, raw_data)

    @staticmethod
    def plot_boxplot(system, raw_data):
        combined_data = []
        for sample_key in ['3 samples', '5 samples', '7 samples']:
            for run, scenarios in raw_data[sample_key].items():
                for scenario, values in scenarios.items():
                    combined_data.append({
                        'Sample Set': OriginalResultPlotter.get_variant_number(sample_key),
                        'Run': run,
                        'Scenario': scenario,
                        'F1-Score': values['f1-score']
                    })
        df = pd.DataFrame(combined_data)

        plt.rcParams['axes.titlesize'] = 20
        plt.rcParams['axes.labelsize'] = 18
        plt.rcParams['xtick.labelsize'] = 14
        plt.rcParams['ytick.labelsize'] = 14
        plt.rcParams['legend.fontsize'] = 15
        plt.rcParams['grid.linewidth'] = 0.8
        plt.rcParams['grid.color'] = 'lightgrey'
        plt.grid(True)

        plt.figure(figsize=(14, 8))
        custom_palette = ["#92C5F9", "#AFDC8F", "#F8AE54"]
        sns.boxplot(data=df, x='Scenario', y='F1-Score', hue='Sample Set', palette=custom_palette)
        plt.title('')
        plt.ylabel('F1-Score')
        plt.xlabel('Proactive Feature Trace Percentage')
        plt.legend(title='# Variants')
        plt.grid(True, linestyle='--', alpha=0.7)
        plt.ylim(0, 1)

        plt.savefig(f'{system}_comparison_combined.png', dpi=300)

    @staticmethod
    def get_variant_number(sample_number_description: str):
        match sample_number_description:
            case '3 samples':
                return '3'
            case '5 samples':
                return '5'
            case '7 samples':
                return '7'
            case _:
                traceback.print_stack()
                raise ValueError(f"The given sample-number-text is invalid: {sample_number_description}")


if __name__ == "__main__":
    for system in config.SYSTEMS:
        OriginalResultPlotter.plot_system_results(system, BASE_PATH)
