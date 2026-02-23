import sys
from pathlib import Path
import json
import csv
import os
from collections import defaultdict

parent_dir = str(Path(__file__).resolve().parent.parent)
if parent_dir not in sys.path:
    sys.path.append(parent_dir)

import utils.utils as utils


JSON_FILE_PATH = r'/home/user/results/significance/significance_results.json'
JSON_FILE_PATH_FLAT = r'/home/user/results/significance/significance_results_flat.json'
TARGET_PATH = r'/home/user/results/significance'

class ResultConverter:

    @staticmethod
    def flatten_results(json_file_path, target_path):
        with open(json_file_path, 'r') as file:
            raw_data = json.load(file)

        converted_data = []
        for entry in raw_data:
            converted_entry = {}
            for key, value in entry['configuration'].items():
                converted_entry[key] = value
            for key, value in entry.items():
                if not key == 'configuration':
                    converted_entry[key] = value
            converted_data.append(converted_entry)

        with open(os.path.join(target_path, 'significance_results_flat.json'), 'w', encoding="utf-8") as file:
            json.dump(converted_data, file, indent=2)

    @staticmethod
    def convert_replication_results(json_file_path: str, target_path: str):
        with open(json_file_path, 'r') as file:
            raw_data = json.load(file)

        filtered_data = [entry for entry in raw_data if not ('mistake_type' in entry)]

        with open(os.path.join(target_path, 'replication_significance_results.csv'), "w", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=filtered_data[0].keys())
            writer.writeheader()
            writer.writerows(filtered_data)

    @staticmethod
    def convert_mistake_results(json_file_path: str, target_path: str):
        with open(json_file_path, 'r') as file:
            raw_data = json.load(file)

        filtered_data = [entry for entry in raw_data if ('mistake_type' in entry)]

        with open(os.path.join(target_path, 'mistake_significance_results.csv'), "w", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=filtered_data[0].keys())
            writer.writeheader()
            writer.writerows(filtered_data)

    @staticmethod
    def convert_replication_results_new_format(json_file_path: str, target_path: str):
        with open(json_file_path, 'r') as file:
            raw_data = json.load(file)

        reordered_data = {}
        reordered_data = defaultdict(lambda: defaultdict(dict))
        for data in raw_data:
            # don't include mistake experiment results
            if 'mistake_type' not in data and data['ft_percentage'] == 5 and data['boost']:
                reordered_data[utils.pretty_dataset_name(data['dataset'])][data['metric']][data['number_of_variants']] = round(data['wilcoxon'], 3)

        new_format_json = []
        for key, value in reordered_data.items():
            new_format_json.append({
                'dataset': key,
                'precision#3': value['precision'][3],
                'precision#5': value['precision'][5],
                'precision#7': value['precision'][7],
                'recall#3': value['recall'][3],
                'recall#5': value['recall'][5],
                'recall#7': value['recall'][7],
                'f1#3': value['f1'][3],
                'f1#5': value['f1'][5],
                'f1#7': value['f1'][7],
            })

        with open(os.path.join(target_path, 'replication_significance_wilcoxon.csv'), "w", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=new_format_json[0].keys())
            writer.writeheader()
            writer.writerows(new_format_json)

    @staticmethod
    def convert_mistake_results_new_format(json_file_path: str, target_path: str):
        with open(json_file_path, 'r') as file:
            raw_data = json.load(file)

        reordered_data = {}
        reordered_data = defaultdict(lambda: defaultdict(lambda: defaultdict(dict)))
        for data in raw_data:
            # don't include mistake experiment results
            if 'mistake_type' in data and data['ft_percentage'] == 5 and data['boost'] and data['mistake_percentage'] == 25:
                reordered_data[utils.pretty_dataset_name(data['dataset'])][data['mistake_type']][data['metric']][data['number_of_variants']] = round(data['wilcoxon'], 3)

        ResultConverter.convert_specific_mistake_result_new_format('SwappedCondition', reordered_data, target_path)
        ResultConverter.convert_specific_mistake_result_new_format('SwappedFeature', reordered_data, target_path)
        ResultConverter.convert_specific_mistake_result_new_format('ErroneousConjunction', reordered_data, target_path)

    @staticmethod
    def convert_specific_mistake_result_new_format(mistake_type: str, reordered_data, target_path: str):
        new_format_json = []
        for key, value in reordered_data.items():
            new_format_json.append({
                'dataset': key,
                'precision#3': value[mistake_type]['precision'][3],
                'precision#5': value[mistake_type]['precision'][5],
                'precision#7': value[mistake_type]['precision'][7],
                'recall#3': value[mistake_type]['recall'][3],
                'recall#5': value[mistake_type]['recall'][5],
                'recall#7': value[mistake_type]['recall'][7],
                'f1#3': value[mistake_type]['f1'][3],
                'f1#5': value[mistake_type]['f1'][5],
                'f1#7': value[mistake_type]['f1'][7],
            })

        with open(os.path.join(target_path, f'mistake_significance_{mistake_type}_wilcoxon.csv'), "w", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=new_format_json[0].keys())
            writer.writeheader()
            writer.writerows(new_format_json)


if __name__ == '__main__':
    ResultConverter.convert_replication_results_new_format(JSON_FILE_PATH_FLAT, TARGET_PATH)
    #ResultConverter.convert_mistake_results_new_format(JSON_FILE_PATH_FLAT, TARGET_PATH)
    #ResultConverter.convert_mistake_results(JSON_FILE_PATH_FLAT, TARGET_PATH)
    #ResultConverter.flatten_results(JSON_FILE_PATH, TARGET_PATH)
