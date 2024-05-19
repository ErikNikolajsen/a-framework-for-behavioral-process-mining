import requests
import json
import pm4py
import pandas as pd
import agent_instructions_generator
import os
import random
import warnings
#import copy


# USER SETTINGS ########################################################################################################################################

NUMBER_OF_EVENT_LOGS = 11                                                       # INPUT: The number of symptomatic event logs produced (integer >=1)
EVENT_LOGS = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]            # INPUT: Each element represent the creation of an event log with the value of the element representing the degree of evolution
                                                     
#SimCoor
SIMCOOR_MODE = "normal"                                                     # INPUT: SimCoor print options (possible values: debug, normal, invisible)
SIMCOOR_SEED = None                                                          # INPUT: SimCoor seed - None value makes seed based on system time 
OUTPUT_XES = "wandering_forgetfulness_repetitiveness_2_100"        # OUTPUT: naming of the produced event logs (without file format extension)

#AIG
ROUTINE_MODEL = "morning_routine_template_entitysensors_delay_4.0.pnml"     # INPUT: The asymptomatic routine model (PNML)
NUMBER_OF_CASES = 2                                                             # INPUT: The number of cases in each event log (integer >=1)
SYMPTOMS = ["wandering", "forgetfulness", "repetitiveness_2"]               # INPUT: The set of symptoms that one wants to inject (see available options in _Symptoms.py)

# Linac input file paths
FLOORPLAN_PATH = "morning_routine_floorplan_entitysensors.json"             # INPUT: Linac floorplan (JSON)
LINAC_SETTINGS_PATH = "simulator.json"                                            # INPUT: Linac simulation settings (JSON)
    

# MAIN CODE ############################################################################################################################################

# Developer settings
CSV_PATH = "linac-backend/eventlog.csv"                                     # INTERMEDIARY-FILE: Linac sensor readings (CSV)
AGENT_INSTRUCTIONS_PATH = "aig-output.json"                                 # INTERMEDIARY-FILE: agent instructions output by the AIG and used as input for Linac (JSON)
AIG_MODE = "invisible"                                                      # INPUT: Possible values: debug, normal, fast, invisible

# Make random seed if none is specified
if SIMCOOR_SEED == None:
    SIMCOOR_SEED = random.randint(0, 2**32 - 1)

# Set SimCoor seed
random.seed(SIMCOOR_SEED)

# Initial print
print(f"""                                                                              
  __          _            
 (_  o ._ _  /   _   _  ._ 
 __) | | | | \_ (_) (_) |
            
--------- Settings --------
Mode: {SIMCOOR_MODE}
Seed: {SIMCOOR_SEED}
___________________________\n""")

for i in range(0,NUMBER_OF_EVENT_LOGS):  

    DEGREE = 0.1*i                                                              # INPUT: Degree of symptom evolution

    # Prints when starting the creation of a new event log
    print(f"""*** Event log {i} of {NUMBER_OF_EVENT_LOGS}""")

    # Cleanup old Linac output CSV file in case of previous SimCoor run halting
    if os.path.exists(CSV_PATH):
        os.remove(CSV_PATH) # Remove intermediate Linac output
    if os.path.exists(AGENT_INSTRUCTIONS_PATH):
        os.remove(AGENT_INSTRUCTIONS_PATH) # Remove intermediate AIG output

    # Send environment
    f = open(FLOORPLAN_PATH)
    floorplan = json.load(f)
    f.close()
    url1 = "http://localhost:8080/api/roomConfig/floorplan"
    if SIMCOOR_MODE in ("debug"):
        print("\nSending floorplan")
    floorplanreq = requests.post(url1, json = floorplan)
    if SIMCOOR_MODE in ("debug"):
        print(floorplanreq.text+"\n")


    for x in range(1,NUMBER_OF_CASES+1):
        if SIMCOOR_MODE in ("debug", "normal"):
            print(f"* Degree {DEGREE} - Trace {x}") #, end='\r'

        # Generate routine instructions by running the AIG
        if SIMCOOR_MODE in ("debug"):
            print("Generate agent instructions\n")
        agent_instructions_generator.aig.run_routine_instruction_generator(ROUTINE_MODEL, 1, random.random(), DEGREE, AIG_MODE, SYMPTOMS, AGENT_INSTRUCTIONS_PATH, FLOORPLAN_PATH)

        # Send agent instructions
        f = open(AGENT_INSTRUCTIONS_PATH)
        inputFile = json.load(f)
        f.close()
        url2 = "http://localhost:8080/api/simulation/input"
        if SIMCOOR_MODE in ("debug"):
            print("Sending agent instructions")
        inputreq = requests.post(url2, json = inputFile)
        if SIMCOOR_MODE in ("debug"):
            print(inputreq.text+"\n")

        # change caseID and send simulator settings
        f = open(LINAC_SETTINGS_PATH)
        simulator = json.load(f)
        f.close()
        simulator["caseID"] = x
        if SIMCOOR_MODE in ("debug"):
            print("sending simulator settings")
        simulatorreq = requests.post("http://localhost:8080/api/simulation/simulator", json = simulator)
        if SIMCOOR_MODE in ("debug"):
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
    pm4py.write_xes(event_log, OUTPUT_XES+f"_({i}).xes")

    if SIMCOOR_MODE in ("debug", "normal"):
            print(f"Event log exported: {OUTPUT_XES}_({i}).xes\n") #, end='\r'


    # Cleanup
    os.remove(CSV_PATH) # Remove intermediate Linac output
    os.remove(AGENT_INSTRUCTIONS_PATH) # Remove intermediate AIG output