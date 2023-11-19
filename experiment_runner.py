import requests
import json
import pm4py
import pandas as pd
import routine_instruction_generator
import os
import random
import warnings
for i in range(1):
    #EP
    OUTPUT_XES = f"repetitiveness_100_0{i}.xes"
    OUTPUT_MODE = "normal" # possible values: debug, normal, invisible
    EP_SEED = None # None value makes it based on system time 

    #RIG
    ROUTINE_MODEL = "morning_routine_template_entitysensors.pnml"
    ITERATIONS = 100
    DEGREE = 0.1
    MODE = "invisible"
    SYMPTOMS = ["repetitiveness"]
    INSTRUCTIONS_PATH = "rig-output.json"

    #Linac
    ENVIRONMENT_PATH = "morning_routine_floorplan_entitysensors.json"
    SETTINGS_PATH = "simulator.json"
    CSV_PATH = "linac-backend-main/eventlog.csv"
    """
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
        print("Sending environment:")
        floorplanreq = requests.post(url1, json = floorplan)
        print(floorplanreq.text)

        # Send agent instructions
        f = open(agent_instructions)
        inputFile = json.load(f)
        f.close()
        url2 = "http://localhost:8080/api/simulation/input"
        print("Sending agent instructions:")
        inputreq = requests.post(url2, json = inputFile)
        print(inputreq.text)

        # Send simulator settings
        f = open(simulator_settings)
        simulator = json.load(f)
        f.close()
        url3 = "http://localhost:8080/api/simulation/simulator"
        print("Sending simulator settings:")
        simulatorreq = requests.post(url3, json = simulator)
        print(simulatorreq.text)
    """
    # Main code
    if __name__ == "__main__":
        print("""                                                                                    
    8888888888 8888888b.  
    888        888   Y88b 
    888        888    888 
    8888888    888   d88P 
    888        8888888P"  
    888        888        
    888        888        
    8888888888 888
            
    Experiment Platform""")

        # Cleanup
        if os.path.exists(CSV_PATH):
            os.remove(CSV_PATH) # In case of previous experiment not completing

        # Set seed
        random.seed(EP_SEED)

        # Send environment
        f = open(ENVIRONMENT_PATH)
        floorplan = json.load(f)
        f.close()
        url1 = "http://localhost:8080/api/roomConfig/floorplan"
        if OUTPUT_MODE in ("debug"):
            print("\nSending floorplan")
        floorplanreq = requests.post(url1, json = floorplan)
        if OUTPUT_MODE in ("debug"):
            print(floorplanreq.text+"\n")


        for x in range(1,ITERATIONS+1):
            if OUTPUT_MODE in ("debug", "normal"):
                print(f"* Iteration {x}")

            # Generate routine instructions by running RIG
            if OUTPUT_MODE in ("debug"):
                print("Generate agent instructions\n")
            routine_instruction_generator.main.run_routine_instruction_generator(ROUTINE_MODEL, 1, random.random(), DEGREE, MODE, SYMPTOMS, INSTRUCTIONS_PATH)

            # Send agent instructions
            f = open(INSTRUCTIONS_PATH)
            inputFile = json.load(f)
            f.close()
            url2 = "http://localhost:8080/api/simulation/input"
            if OUTPUT_MODE in ("debug"):
                print("Sending agent instructions")
            inputreq = requests.post(url2, json = inputFile)
            if OUTPUT_MODE in ("debug"):
                print(inputreq.text+"\n")

            # change caseID and send simulator settings
            f = open(SETTINGS_PATH)
            simulator = json.load(f)
            f.close()
            simulator["caseID"] = x
            if OUTPUT_MODE in ("debug"):
                print("sending simulator settings")
            simulatorreq = requests.post("http://localhost:8080/api/simulation/simulator", json = simulator)
            if OUTPUT_MODE in ("debug"):
                print(simulatorreq.text+"\n")

        # Convert CSV to XES
        dataframe = pd.read_csv(CSV_PATH, sep=',', header=None)
        dataframe.columns = ['case:concept:name', 'time:timestamp', 'concept:name', 'sensor:name', 'sensor:reading']
        dataframe['time:timestamp'] = pd.to_datetime(dataframe['time:timestamp'])
        dataframe['case_id'] = dataframe['case:concept:name']
        dataframe['activity'] = dataframe['concept:name']
        dataframe = pm4py.format_dataframe(dataframe, case_id='case:concept:name', activity_key='sensor:name', timestamp_key='time:timestamp', timest_format = '%Y-%m-%dT%H:%M:%S.%f%z')
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            event_log = pm4py.convert_to_event_log(dataframe)
        pm4py.write_xes(event_log, OUTPUT_XES)

        # Cleanup
        os.remove(CSV_PATH)
        os.remove(INSTRUCTIONS_PATH)