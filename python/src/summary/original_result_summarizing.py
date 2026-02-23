import os
import json
import traceback
import statistics


BASE_PATH = r'C:\Example\Path\reported-results'
SYSTEMS = ['busybox', 'argouml-spl', 'Marlin', 'openvpn', 'vim']

def get_max_system_improvement(system: str, base_path: str, metric: str):
    json_path = os.path.join(base_path, f'experiment_result_{system}.json')
    with open(json_path, 'r') as file:
        raw_data = json.load(file)

    combined_data = []
    for sample_key in ['3 samples', '5 samples', '7 samples']:
        for run, scenarios in raw_data[sample_key].items():
            for scenario, values in scenarios.items():
                combined_data.append({
                    'Sample Set': get_variant_number(sample_key),
                    'Run': run,
                    'Scenario': scenario,
                    metric: values[metric]
                })

    return get_max_median_f1_improvement(combined_data, metric)

def get_max_median_f1_improvement(combined_data, metric: str):
    variant_nums = ['3', '5', '7']
    improvements = []
    for variant_num in variant_nums:
        improvements.append(get_median_f1_improvement(combined_data, variant_num, metric))
    return max(improvements)

def get_median_f1_improvement(combined_data, variant_num: str, metric: str):
    return get_median_f1(combined_data, variant_num, '5%', metric) - get_median_f1(combined_data, variant_num, '0%', metric)

def get_median_f1(combined_data, variant_num: str, percent: str, metric: str):
    scores = [map[metric] for map in combined_data if map['Sample Set'] == variant_num and map['Scenario'] == percent]
    return statistics.median(scores)

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
    #metric = 'precision'
    #metric = 'recall'
    metric = 'f1-score'
    max_improvement = 0.0
    for system in SYSTEMS:
        system_improvement = get_max_system_improvement(system, BASE_PATH, metric)
        if max_improvement < system_improvement:
            max_improvement =system_improvement
    print(max_improvement)

