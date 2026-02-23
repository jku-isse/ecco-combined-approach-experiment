import pandas as pd
import sys
from pathlib import Path

parent_dir = str(Path(__file__).resolve().parent.parent)
if parent_dir not in sys.path:
    sys.path.append(parent_dir)

import utils.utils as utils

CSV_PATH = r'/home/user/results/significance/mistake_significance_results.csv'
OUTPUT_PATH = r'/home/user/results/significance/mistake_significance_results_filtered.csv'

# Load the data
df = pd.read_csv(CSV_PATH)

columns_to_remove = ["normally_distributed", "ttest_significant", "wilcoxon_significant"]
df = df.drop(columns=columns_to_remove, errors='ignore')  # 'errors=ignore' prevents errors if columns don't exist

df = df[df['boost']]
df = df.drop(columns=["boost"], errors='ignore')

df = df[df['metric'] != 'recall']
df = df[df['ft_percentage'] == 5]
df = df.drop(columns=["ft_percentage"], errors='ignore')

df = df[df['mistake_percentage'] == 25]
df = df.drop(columns=["mistake_percentage"], errors='ignore')

df['shapiro'] = df['shapiro'].round(3)
df['ttest'] = df['ttest'].round(3)
df['wilcoxon'] = df['wilcoxon'].round(3)

df['dataset'] = df['dataset'].apply(lambda x: utils.pretty_dataset_name(x))

# Save modified DataFrame as CSV file
df.to_csv(OUTPUT_PATH, index=False)
