import requests
import json
import pm4py
import pandas as pd
import os

ENVIRONMENT_PATH = "floorplan.json"
INSTRUCTIONS_PATH = "input.json"
SETTINGS_PATH = "simulator.json"

def run_simulation(environment, agent_instructions, simulator_settings):
    # Test connection 
    print("Sending ping for connection test:")
    response = requests.get("http://localhost:8080/api/system/ping")
    print(response.text)

    # Send environment
    f = open(environment)
    floorplan = json.load(f)
    f.close()
    url1 = "http://localhost:8080/api/roomConfig/floorplan"
    print("\nSending environment:")
    floorplanreq = requests.post(url1, json = floorplan)
    print(floorplanreq.text)

    # Send agent instructions
    f = open(agent_instructions)
    inputFile = json.load(f)
    f.close()
    url2 = "http://localhost:8080/api/simulation/input"
    print("\nSending agent instructions:")
    inputreq = requests.post(url2, json = inputFile)
    print(inputreq.text)

    # Send simulator settings
    f = open(simulator_settings)
    simulator = json.load(f)
    f.close()
    url3 = "http://localhost:8080/api/simulation/simulator"
    
    print("\nSending simulator settings:")
    simulatorreq = requests.post(url3, json = simulator)
    print(simulatorreq.text)

# Main code
if __name__ == "__main__":
    # Remove old CSV
    parent_directory = os.path.abspath(os.path.join(os.getcwd(), os.pardir))
    file_path = os.path.join(parent_directory, 'eventlog.csv')
    CSV_PATH = file_path
    if os.path.exists(CSV_PATH):
        os.remove(CSV_PATH)

    # Run simulation
    run_simulation(ENVIRONMENT_PATH, INSTRUCTIONS_PATH, SETTINGS_PATH)

    
    
    """
    # Load CSV log
    dataframe = pd.read_csv(CSV_PATH, sep=',')
    dataframe.columns = ['case:concept:name', 'time:timestamp', 'sensor:type', 'sensor:name', 'sensor:reading']
    event_log = pm4py.convert_to_event_log(dataframe)
    pm4py.write_xes(event_log, 'exported.xes')
    """
