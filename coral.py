import argparse
import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import spearmanr

def calculate_spearman(csv_file, col1, col2, start_row, end_row):
    # Read CSV file into a pandas DataFrame
    df = pd.read_csv(csv_file, delim_whitespace=True)

    # Select specified rows and columns
    df_subset = df.iloc[start_row:end_row+1, [col1, col2]]

    # Calculate Spearman's correlation coefficient and p-value
    spearman_corr, p_value = spearmanr(df_subset.iloc[:, 0], df_subset.iloc[:, 1])

    return spearman_corr, p_value, len(df_subset), df_subset

def p_value_negative_correlation(corr_coefficient):
    # One-tailed p-value for negative correlation
    if corr_coefficient < 0:
        p_value_one_tailed = p_value / 2
    else:
        p_value_one_tailed = 1 - (p_value / 2)
    return p_value_one_tailed

def test_statistical_significance(p_value, alpha):
    # Determine statistical significance based on the p-value and alpha level
    if p_value < alpha:
        significance = "true"
    else:
        significance = "false"
    return significance

def spearman_coefficient_interpretation(corr_coefficient):
    # Interpret the strength of Spearman's correlation coefficient based on APA standards
    if -1.00 <= corr_coefficient < -0.50:
        interpretation = "strong"
    elif -0.50 <= corr_coefficient < -0.30:
        interpretation = "moderate"
    elif -0.30 <= corr_coefficient < -0.10:
        interpretation = "weak"
    #elif 0.10 <= corr_coefficient < 0.30:
    #    interpretation = "weak"
    #elif 0.30 <= corr_coefficient < 0.50:
    #    interpretation = "moderate"
    #elif 0.50 <= corr_coefficient <= 1.00:
    #    interpretation = "strong"
    else:
        interpretation = "None"
    return interpretation

if __name__ == "__main__":
    # Define command-line arguments
    parser = argparse.ArgumentParser(description="Calculate Spearman's correlation coefficient for two specified columns in a CSV file.")
    parser.add_argument("csv_file", type=str, help="Path to the CSV file")
    parser.add_argument("col1", type=int, help="Index of the first column (zero-based)")
    parser.add_argument("col2", type=int, help="Index of the second column (zero-based)")
    parser.add_argument("start_row", type=int, help="Index of the starting row (zero-based)")
    parser.add_argument("end_row", type=int, help="Index of the ending row (zero-based)")

    # Parse command-line arguments
    args = parser.parse_args()

    # Calculate Spearman's correlation coefficient
    corr_coefficient, p_value, sample_size, df_subset = calculate_spearman(args.csv_file, args.col1, args.col2, args.start_row, args.end_row)
    one_tailed_p_value = p_value_negative_correlation(corr_coefficient)
    statistical_significance = test_statistical_significance(one_tailed_p_value, 0.05)
    interpretation = spearman_coefficient_interpretation(corr_coefficient) 

    # Testing for negative correlation
    print("""
┏┓      ┓
┃ ┏┓┏┓┏┓┃
┗┛┗┛┛ ┗┻┗""")
    print("Sample size:", sample_size)
    print("Spearman's correlation coefficient:", format(corr_coefficient, ".2f"))
    #print("p-value:", format(p_value, ".3f"))
    print("p-value (one-tailed):", format(one_tailed_p_value, ".3f"))
    print("Statitistically significant:", statistical_significance)
    print("Negative correlation:", interpretation)

    # Plot the dataset as a line graph
    plt.plot(df_subset.iloc[:, 0], df_subset.iloc[:, 1], marker='o', linestyle='-')
    plt.xlabel("Degree of symptom evolution")
    plt.ylabel("Fitness")
    plt.title("Spearman's rank correlation")
    
    # Set x and y axis limits
    plt.xlim(0, 1)
    plt.ylim(0, 1)
    
    # Add annotations to the plot in the upper right corner
    plt.text(0.05, 0.15, f"n: {sample_size}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='left')
    plt.text(0.05, 0.10, f"ρ: {format(corr_coefficient, '.2f')}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='left')
    plt.text(0.05, 0.05, f"p: {format(p_value, '.3f')}", transform=plt.gca().transAxes, fontsize=10, verticalalignment='bottom', horizontalalignment='left')
    
    plt.show()
