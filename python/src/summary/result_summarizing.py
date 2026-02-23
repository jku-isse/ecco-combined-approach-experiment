import json
import os
import statistics
import sys
from pathlib import Path

parent_dir = str(Path(__file__).resolve().parent.parent)
if parent_dir not in sys.path:
    sys.path.append(parent_dir)

import utils.utils as utils
from database.database_handling import DatabaseHandler

DATASETS = ['argouml-spl', 'berkeley-db-libdb', 'busybox', 'dia', 'apache-httpd', 'irssi', 'libssh', 'openvpn', 'vim']
FT_PERCENTAGE = 5
METRIC = 'f1'
NUMBERS_OF_VARIANTS = [3, 5, 7]
DB_BASE_PATH = r'C:\Example\Path\results\databases'
RESULT_FILE_PATH = r'C:\Example\Path\results\improvements\improvements.json'
BOOST = 1

# for each system: compute median for 0% ft and median for 5% ft (and their difference)

def create_result_summary():
    improvement_map = {}
    for dataset in DATASETS:
        improvement_map[dataset] = get_dataset_improvements(dataset)
    add_overall_stats(improvement_map)

    with open(RESULT_FILE_PATH, 'w') as json_file:
        json.dump(improvement_map, json_file, indent=4)

def get_dataset_improvements(dataset: str):
    database_handler = DatabaseHandler(os.path.join(DB_BASE_PATH, f'results_{utils.dataset_to_file_name(dataset)}.db'))
    variants_map = {}
    for number_of_variants in NUMBERS_OF_VARIANTS:
        stats = {}
        f1_scores_tuples_0 = database_handler.get_ft_experiment_data(dataset, "f1", number_of_variants, 0, 1)
        f1_scores_0 = [element for (element,) in f1_scores_tuples_0]
        f1_median_0 = statistics.median(f1_scores_0)
        stats['Median_0%FT'] = f1_median_0
        f1_scores_tuples_5 = database_handler.get_ft_experiment_data(dataset, "f1", number_of_variants, 5, 1)
        f1_scores_5 = [element for (element,) in f1_scores_tuples_5]
        f1_median_5 = statistics.median(f1_scores_5)
        stats['Median_5%FT'] = f1_median_5
        stats['Median_Improvement'] = f1_median_5 - f1_median_0
        variants_map[number_of_variants] = stats
    return variants_map

def check_result_number(results):
    if len(results) != 30:
        raise Exception("number of results is wrong!")

def add_overall_stats(improvement_map):
    improvements = []
    for dataset, dataset_map in improvement_map.items():
        for number_of_variants, stats in dataset_map.items():
            improvements.append(stats['Median_Improvement'])
    improvement_map['Overall MIN'] = min(improvements)
    improvement_map['Overall MAX'] = max(improvements)


if __name__ == "__main__":
    create_result_summary()
