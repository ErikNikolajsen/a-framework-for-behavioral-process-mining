import requests
import json
import pm4py
import pandas as pd
import agent_instructions_generator
import os
import random
import warnings
#import copy

for i in range(0,11):
    #EP
    OUTPUT_XES = f"wandering_forgetfulness_repetitiveness_2_100_{i}.xes"
    OUTPUT_MODE = "normal" # possible values: debug, normal, invisible
    EP_SEED = None # None value makes seed based on system time 

    #AIG
    ROUTINE_MODEL = "morning_routine_template_entitysensors_delay_4.0.pnml"
    ITERATIONS = 100
    DEGREE = 0.1*i
    MODE = "invisible" # possible values: debug, normal, invisible
    SYMPTOMS = ["wandering", "forgetfulness", "repetitiveness_2"]
    INSTRUCTIONS_PATH = "rig-output.json"

    #Linac
    ENVIRONMENT_PATH = "morning_routine_floorplan_entitysensors.json" #_presencesensors
    SETTINGS_PATH = "simulator.json"
    CSV_PATH = "linac-backend-main/eventlog.csv"

    # Main code

    if EP_SEED == None:
        EP_SEED = random.randint(0, 2**32 - 1)

    if __name__ == "__main__":
        print(f"""                                                                                    
8888888888 8888888b.  
888        888   Y88b 
888        888    888 
8888888    888   d88P 
888        8888888P"  
888        888        
888        888        
8888888888 888
            
--------- Settings ---------

Experiment Runner
Output:      {OUTPUT_XES}
Mode:        {OUTPUT_MODE}
Seed:        {EP_SEED}
____________________________\n""")

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
                print(f"* Degree {DEGREE} - Trace {x}") #, end='\r'

            # Generate routine instructions by running RIG
            if OUTPUT_MODE in ("debug"):
                print("Generate agent instructions\n")
            agent_instructions_generator.main.run_routine_instruction_generator(ROUTINE_MODEL, 1, random.random(), DEGREE, MODE, SYMPTOMS, INSTRUCTIONS_PATH, ENVIRONMENT_PATH)

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
        dataframe.columns = ['case:concept:name', 'time:timestamp', 'sensor:type', 'sensor:name', 'sensor:reading']
        dataframe['time:timestamp'] = pd.to_datetime(dataframe['time:timestamp'])
        
        # Add columns
        dataframe['start_timestamp'] = dataframe['time:timestamp'].copy()
        """
        # Filter for removing redundant sensor readings
        dataframe['previous_sensor_reading'] = dataframe['sensor:reading'].shift(1) # Add a new column 'previous_sensor_reading' to track the previous sensor reading
        dataframe = dataframe[dataframe['sensor:reading'] != dataframe['previous_sensor_reading']] # Filter out rows where the sensor reading is the same as the previous reading
        dataframe = dataframe.drop(columns='previous_sensor_reading') # Drop the 'previous_sensor_reading' column
        """

        # Filtering of PresenceSensors
        # Filter redundant PresenceSensor events
        '''
        rows_to_delete = []
        previous_sensor_name = None
        for index, row in dataframe.iterrows():
            if row['sensor:type'] == 'PresenceSensor':
                if row['sensor:name'] == previous_sensor_name:
                    rows_to_delete.append(index)
                previous_sensor_name = row['sensor:name']
        dataframe = dataframe.drop(rows_to_delete)
        '''
        """
        # drop all events of sensor type EntitySensor
        for index, row in dataframe.iterrows():
            if row['sensor:type'] == 'EntitySensor':
                dataframe = dataframe.drop(index)
        """
        # Add complete_time column
        dataframe = dataframe.sort_values(by='case:concept:name')
        dataframe['complete_time'] = dataframe['time:timestamp'].shift(-1)
        dataframe['complete_time'] = dataframe['complete_time'].fillna(dataframe['time:timestamp'].iloc[-1]) #ensures that any missing values resulting from the shifting operation in the first statement are replaced with the last known timestamp value


        dataframe = pm4py.format_dataframe(dataframe, case_id='case:concept:name', activity_key='sensor:reading', timestamp_key='time:timestamp', timest_format = '%Y-%m-%dT%H:%M:%S.%f%z')
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            event_log = pm4py.convert_to_event_log(dataframe)
        pm4py.write_xes(event_log, OUTPUT_XES)

        # Cleanup
        os.remove(CSV_PATH)
        os.remove(INSTRUCTIONS_PATH)