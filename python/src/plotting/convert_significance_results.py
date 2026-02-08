import json
import csv
import os

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



if __name__ == '__main__':
    ResultConverter.convert_replication_results(JSON_FILE_PATH_FLAT, TARGET_PATH)
    #ResultConverter.convert_mistake_results(JSON_FILE_PATH_FLAT, TARGET_PATH)
    #ResultConverter.flatten_results(JSON_FILE_PATH, TARGET_PATH)
