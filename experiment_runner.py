import requests
import json
import pm4py
import pandas as pd
import routine_instruction_generator
import os
import random

ENVIRONMENT_PATH = "morning_routine_floorplan.json"
INSTRUCTIONS_PATH = "rig-output.json"
SETTINGS_PATH = "simulator.json"
CSV_PATH = "linac-backend-main/eventlog.csv"
ROUTINE_MODEL = "morning_routine_template.pnml"
ITERATIONS = 1

def run_linac_simulation(environment, agent_instructions, simulator_settings):
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
    #run_linac_simulation(ENVIRONMENT_PATH, INSTRUCTIONS_PATH, SETTINGS_PATH)

    # Send environment
    f = open(ENVIRONMENT_PATH)
    floorplan = json.load(f)
    f.close()
    url1 = "http://localhost:8080/api/roomConfig/floorplan"
    print("\nSending environment:")
    floorplanreq = requests.post(url1, json = floorplan)
    print(floorplanreq.text)


    for x in range(ITERATIONS):

        # Generate routine instructions by running RIG
        routine_instruction_generator.main.run_routine_instruction_generator(ROUTINE_MODEL, 1, random.random(), 0.0, "invisible", ["repetitiveness"], "rig-output.json")

        # Send agent instructions
        f = open(INSTRUCTIONS_PATH)
        inputFile = json.load(f)
        f.close()
        url2 = "http://localhost:8080/api/simulation/input"
        print("\nSending agent instructions:")
        inputreq = requests.post(url2, json = inputFile)
        print(inputreq.text)

        # change caseID and send simulator settings
        f = open(SETTINGS_PATH)
        simulator = json.load(f)
        f.close()

        simulator["caseID"] = x

        simulatorreq = requests.post("http://localhost:8080/api/simulation/simulator", json = simulator)
        print(simulatorreq)
        print("\nsending simulator settings: ")
        print(simulatorreq.text+"\n")

    # Convert CSV to XES
    dataframe = pd.read_csv(CSV_PATH, sep=',', header=None)
    dataframe.columns = ['case:concept:name', 'time:timestamp', 'concept:name', 'sensor:name', 'sensor:reading']
    dataframe['case_id'] = dataframe['case:concept:name']
    dataframe['activity'] = dataframe['concept:name']
    dataframe = pm4py.format_dataframe(dataframe, case_id='case:concept:name', activity_key='concept:name', timestamp_key='time:timestamp')
    event_log = pm4py.convert_to_event_log(dataframe)
    pm4py.write_xes(event_log, 'exported.xes')

    # Cleanup
    os.remove(CSV_PATH)