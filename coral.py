import argparse
import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import spearmanr

def calculate_spearman(csv_file, col1, col2, start_row, end_row):
    # Read CSV file into a pandas DataFrame
    df = pd.read_csv(csv_file, delim_whitespace=True)

    # Adjust inputs to zero-based indexing
    col1 -= 1
    col2 -= 1
    start_row -= 1

    # Select specified rows and columns
    df_subset = df.iloc[start_row:end_row, [col1, col2]]

    # Calculate Spearman's correlation coefficient and p-value
    spearman_corr, p_value = spearmanr(df_subset.iloc[:, 0], df_subset.iloc[:, 1])

    return spearman_corr, p_value, len(df_subset), df_subset

def test_statistical_significance(p_value, alpha=0.05):
    # Determine statistical significance based on the p-value and alpha level
    if p_value < alpha:
        significance = "statistically significant"
    else:
        significance = "not statistically significant"

    return significance

if __name__ == "__main__":
    # Define command-line arguments
    parser = argparse.ArgumentParser(description="Calculate Spearman's correlation coefficient for two specified columns in a CSV file.")
    parser.add_argument("csv_file", type=str, help="Path to the CSV file")
    parser.add_argument("col1", type=int, help="Index of the first column (non zero-based)")
    parser.add_argument("col2", type=int, help="Index of the second column (non zero-based)")
    parser.add_argument("start_row", type=int, help="Index of the starting row (non zero-based)")
    parser.add_argument("end_row", type=int, help="Index of the ending row (non zero-based)")

    # Parse command-line arguments
    args = parser.parse_args()

    # Calculate Spearman's correlation coefficient
    corr_coefficient, p_value, sample_size, df_subset = calculate_spearman(args.csv_file, args.col1, args.col2, args.start_row, args.end_row)

    # Testing for negative correlation
    if corr_coefficient < 0:
        print("Sample size:", sample_size)
        print("Spearman's correlation coefficient:", format(corr_coefficient, ".2f"))
        print("p-value:", format(p_value, ".3f"))
        print("Result is", test_statistical_significance(p_value))

        # Plot the dataset as a scatter plot
        plt.scatter(df_subset.iloc[:, 0], df_subset.iloc[:, 1])
        plt.xlabel("Degree of symptom evolution")
        plt.ylabel("Fitness")
        plt.title("Spearman's rank correlation")
        
        # Add annotations to the plot in the upper right corner
        plt.text(0.95, 0.95, f"Sample size: {sample_size}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='top', horizontalalignment='right')
        plt.text(0.95, 0.9, f"Spearman's correlation coefficient: {format(corr_coefficient, '.2f')}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='top', horizontalalignment='right')
        plt.text(0.95, 0.85, f"p-value: {format(p_value, '.3f')}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='top', horizontalalignment='right')
        plt.text(0.95, 0.8, f"Result is {test_statistical_significance(p_value)}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='top', horizontalalignment='right')
        
        plt.show()
    else:
        print("No significant negative correlation found.")
