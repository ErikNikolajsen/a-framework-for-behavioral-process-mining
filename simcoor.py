import requests
import json
import pm4py
import pandas as pd
import agent_instructions_generator
import os
import random
import warnings
#import copy
import sys # used for pm4py print suppression
import io # used for pm4py print suppression



# USER SETTINGS ########################################################################################################################################

NUMBER_OF_EVENT_LOGS = 100 # INPUT: The number of symptomatic event logs produced (integer >=1)
NUMBER_OF_CASES = 1000 # INPUT: The number of cases in each event log (integer >=1)
SIMCOOR_SEED = None # INPUT: SimCoor seed (Integer or None value (None value makes seed based on system time))
OUTPUT_XES = "dementia" # OUTPUT: prefix-naming of the produced event logs (without file format extension)
ROUTINE_MODEL = "morning_routine_template_entitysensors_delay_4.0.pnml" # INPUT: The asymptomatic routine model (PNML)
SYMPTOMS = ["wandering", "forgetfulness", "repetitiveness_2"] # INPUT: The set of symptoms that one wants to inject (see available options in _Symptoms.py)
FLOORPLAN_PATH = "morning_routine_floorplan_entitysensors.json" # INPUT: Linac floorplan (JSON)
LINAC_SETTINGS_PATH = "simulator.json" # INPUT: Linac simulation settings (JSON)

# DEVELOPER SETTINGS ###################################################################################################################################

CSV_PATH = "linac-backend/eventlog.csv" # INTERMEDIARY-FILE: Linac sensor readings (CSV)
AGENT_INSTRUCTIONS_PATH = "aig-output.json" # INTERMEDIARY-FILE: agent instructions output by the AIG and used as input for Linac (JSON)
AIG_MODE = "invisible" # INPUT: Possible values: debug, normal, fast, invisible
SIMCOOR_MODE = "normal" # INPUT: SimCoor print options (possible values: debug, normal, invisible)

# MAIN CODE ############################################################################################################################################

# Seed
if SIMCOOR_SEED == None: # Make random seed if none is specified
    SIMCOOR_SEED = random.randint(0, 2**32 - 1)
random.seed(SIMCOOR_SEED) # Set random seed value

# Derive degree increments
degree_increment = 1 / NUMBER_OF_EVENT_LOGS

# Initial print
print(f"""                                                                              
  __          _            
 (_  o ._ _  /   _   _  ._ 
 __) | | | | \_ (_) (_) |
            
--------- Settings --------
Event logs: {NUMBER_OF_EVENT_LOGS+1}
Cases: {NUMBER_OF_CASES}
Seed: {SIMCOOR_SEED}
Event log prefix: {OUTPUT_XES}
___________________________\n""")

### EVENT LOGIC LOOP
current_event_log = 1
degree = 0
while (degree <= 1):  

    # Prints when starting the creation of a new event log
    print(f"""*** Event log {current_event_log} of {NUMBER_OF_EVENT_LOGS+1}""")

    # Cleanup old intermediary files in case of previous SimCoor run halting
    if os.path.exists(CSV_PATH):
        os.remove(CSV_PATH) # Remove intermediate Linac output
    if os.path.exists(AGENT_INSTRUCTIONS_PATH):
        os.remove(AGENT_INSTRUCTIONS_PATH) # Remove intermediate AIG output

    # Send floorplan to Linac backend
    f = open(FLOORPLAN_PATH)
    floorplan = json.load(f)
    f.close()
    url1 = "http://localhost:8080/api/roomConfig/floorplan"
    if SIMCOOR_MODE in ("debug"):
        print("\nSending floorplan")
    floorplanreq = requests.post(url1, json = floorplan)
    if SIMCOOR_MODE in ("debug"):
        print(floorplanreq.text+"\n")

    ### CASE LOGIC LOOP
    for case in range(1,NUMBER_OF_CASES+1):
        if SIMCOOR_MODE in ("debug", "normal"):
            print(f"* Case {case} of {NUMBER_OF_CASES}", end="\r") #, end='\r'

        # Generate routine instructions by running the AIG
        if SIMCOOR_MODE in ("debug"):
            print("Generate agent instructions\n")
        agent_instructions_generator.aig.run_routine_instruction_generator(ROUTINE_MODEL, 1, random.random(), degree, AIG_MODE, SYMPTOMS, AGENT_INSTRUCTIONS_PATH, FLOORPLAN_PATH)

        # Send agent instructions to Linac
        f = open(AGENT_INSTRUCTIONS_PATH)
        inputFile = json.load(f)
        f.close()
        url2 = "http://localhost:8080/api/simulation/input"
        if SIMCOOR_MODE in ("debug"):
            print("Sending agent instructions")
        inputreq = requests.post(url2, json = inputFile)
        if SIMCOOR_MODE in ("debug"):
            print(inputreq.text+"\n")

        # Change caseID and seed value before sending simulator settings to Linac
        f = open(LINAC_SETTINGS_PATH)
        simulator = json.load(f)
        f.close()
        simulator["caseID"] = case
        simulator["seed"] = random.random()
        if SIMCOOR_MODE in ("debug"):
            print("sending simulator settings")
        simulatorreq = requests.post("http://localhost:8080/api/simulation/simulator", json = simulator)
        if SIMCOOR_MODE in ("debug"):
            print(simulatorreq.text+"\n")

    ### EVENT LOG CONVERSION

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


    original_stderr = sys.stderr # used for suppressing pm4py output
    sys.stderr = io.StringIO() # used for suppressing pm4py output
    pm4py.write_xes(event_log, OUTPUT_XES+f"_({degree}).xes")
    sys.stderr = original_stderr # used for suppressing pm4py output

    if SIMCOOR_MODE in ("debug", "normal"):
        print(f"Exported event log: \"{OUTPUT_XES}_(C{NUMBER_OF_CASES}-D{degree:.{3}f}).xes\"\n")

    ### EVENT LOG LOOP END OPERATIONS

    # Cleanup
    os.remove(CSV_PATH) # Remove intermediate Linac output
    os.remove(AGENT_INSTRUCTIONS_PATH) # Remove intermediate AIG output

    # update of loop variables
    current_event_log += 1
    degree += degree_increment
    degree = round(degree, 6)