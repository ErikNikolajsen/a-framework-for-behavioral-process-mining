import argparse
import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import spearmanr
from scipy.stats import kendalltau
import numpy as np

METHOD = "tau"

def calculate_kendall(csv_file, col1, col2, start_row, end_row):
    # Read CSV file into a pandas DataFrame
    df = pd.read_csv(csv_file, delim_whitespace=False)

    # Select specified rows and columns
    df_subset = df.iloc[start_row:end_row, [col1, col2]]

    # Calculate Kendall's correlation coefficient and p-value
    tau, p_value = kendalltau(df_subset.iloc[:, 0], df_subset.iloc[:, 1])

    return tau, p_value, len(df_subset), df_subset

def calculate_spearman(csv_file, col1, col2, start_row, end_row):
    # Read CSV file into a pandas DataFrame
    df = pd.read_csv(csv_file, delim_whitespace=False)

    # Select specified rows and columns
    df_subset = df.iloc[start_row:end_row, [col1, col2]]

    # Calculate Spearman's correlation coefficient and p-value
    spearman_corr, p_value = spearmanr(df_subset.iloc[:, 0], df_subset.iloc[:, 1])

    return spearman_corr, p_value, len(df_subset), df_subset

def test_statistical_significance(p_value, alpha):
    # Determine statistical significance based on the p-value and alpha level
    if p_value < alpha:
        significance = "true"
    else:
        significance = "false"
    return significance

def spearman_coefficient_interpretation(corr_coefficient):
    if -1.00 <= corr_coefficient < -0.90:
        interpretation = "strong"
    elif -0.50 <= corr_coefficient < -0.80:
        interpretation = "moderate"
    elif -0.30 <= corr_coefficient < -0.70:
        interpretation = "weak"
    elif 0.10 <= corr_coefficient < 0.70:
        interpretation = "weak"
    elif 0.30 <= corr_coefficient < 0.80:
        interpretation = "moderate"
    elif 0.50 <= corr_coefficient <= 1.00:
        interpretation = "strong"
    else:
        interpretation = "None"
    return interpretation

if __name__ == "__main__":
    # Define command-line arguments
    parser = argparse.ArgumentParser(description="Calculate Spearman's correlation coefficient for two specified columns in a CSV file.")
    parser.add_argument("csv_file", type=str, help="Path to the CSV file")
    parser.add_argument("start_row", type=int, help="Index of the starting row (zero-based)")
    parser.add_argument("end_row", type=int, help="Index of the ending row (zero-based)")
    parser.add_argument("col1", type=int, help="Index of the first column (zero-based)")
    parser.add_argument("col2", type=int, help="Index of the second column (zero-based)")

    parser.add_argument("-sy", "--symptoms", nargs="+", type=str, help="The symptoms expressed in the routine (options: wandering, repetitiveness)")

    # Parse command-line arguments
    args = parser.parse_args()

    # Print header
    print("""
┏┓      ┓
┃ ┏┓┏┓┏┓┃
┗┛┗┛┛ ┗┻┗""")
    for column in range(1,args.col2+1):
        if METHOD == "rho":
            corr_coefficient, p_value, sample_size, df_subset = calculate_spearman(args.csv_file, args.col1, column, args.start_row, args.end_row)
        elif METHOD == "tau":
            corr_coefficient, p_value, sample_size, df_subset = calculate_kendall(args.csv_file, args.col1, column, args.start_row, args.end_row)
        statistical_significance = test_statistical_significance(p_value, 0.05)
        interpretation = spearman_coefficient_interpretation(corr_coefficient) 

        if METHOD == "rho":
            print(f"Col: {df_subset.columns[1]}, ρ: {corr_coefficient:.2f}, n: {sample_size}, p: {p_value:.3f}, sig: {statistical_significance}")
        elif METHOD == "tau":
            print(f"Col: {df_subset.columns[1]}, τ: {corr_coefficient:.2f}, n: {sample_size}, p: {p_value:.3f}, sig: {statistical_significance}")


        # Plot the dataset as a line graph
        plt.plot(df_subset.iloc[:, 0], df_subset.iloc[:, 1], marker='', linestyle='-', label=df_subset.columns[1])
    
    plt.xlabel("Degree of symptom evolution δ")
    plt.ylabel("Fitness φ")
    #plt.title("Title")
    plt.legend(loc='lower center', ncol=3)
    plt.xlim(0, 1)
    plt.ylim(0, 1)
    plt.show()
