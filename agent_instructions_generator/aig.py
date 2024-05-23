# Author: Erik Ravn Nikolajsen 
# Copyright (c) 2023 Erik Ravn Nikolajsen
# License: MIT License

# Import statements
import sys # for halting the program
import argparse # for parsing arguments in the command line interface
import random # for randomization
import xml.etree.ElementTree as ET # datastructure for storing petri nets
import json # for exporting SHAIS input files for Linac
from . import _petri_net
from . import _symptoms
import copy


# Function definitions
## Function to parse a PNML file and extract Petri net information
def import_petri_net_from_pnml(file_path):
    try:
        tree = ET.parse(file_path)
        return tree
    
    except FileNotFoundError:
        print(f"File not found: {file_path}")
        print("Halting program\n")
        sys.exit(1)
    
    except ET.ParseError:
        print(f"Error parsing the PNML file: {file_path}")
        print("Halting program\n")
        sys.exit(1)

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

def print_petri_net(net):
    print("\nPlaces: \n(id, name, tokens)")
    for place in net.places:
        print(place.id+", "+place.name+", "+str(place.tokens))
    print("\nTransitions: \n(id, name, delay_lower, delay_upper, label)")
    for transition in net.transitions:
        print(transition.id+", "+transition.name+", "+str(transition.delay_lower_limit)+", "+str(transition.delay_upper_limit)+", "+transition.label)
    print("\nArcs: \n(id, source, target)")
    for arc in net.arcs:
        print(arc.id+", "+arc.source+", "+arc.target)
    print("")

def run_routine_instruction_generator(model, iterations, seed, degree, mode, symptoms, output, floorplan):
    # Process arguments
    random.seed(seed) # Set random seed value
    symptoms_string = "None"
    if symptoms != None:
        symptoms_string = ",\n            ".join(symptoms)
        for symptom in symptoms:
            if symptom not in ("wandering", "repetitiveness", "forgetfulness"):
                print(f"Error: the symptom '{symptom}' is not an option")
                print("Halting program\n")
                sys.exit(1)
    if mode not in ("debug", "normal", "fast", "invisible"):
        print(f"Error: the mode '{mode}' is not an option")
        print("Halting program\n")
        sys.exit(1)
    if not 0.0 <= degree <= 1.0:
        print(f"Error: the degree '{degree}' is not within the interval [0.0, 1.0]")
        print("Halting program\n")
        sys.exit(1)
    if not iterations >= 1:
        print(f"Error: the iteration '{iterations}' is less than 1")
        print("Halting program\n")
        sys.exit(1)
    if "wandering" in symptoms:
        if floorplan == None:
            print(f"Error: a floorplan needs to be specified with the specified symptoms")
            print("Halting program\n")
            sys.exit(1)

    
    if mode != "invisible":
        print(f"""
       d8888 8888888 .d8888b.  
      d88888   888  d88P  Y88b 
     d88P888   888  888    888 
    d88P 888   888  888        
   d88P  888   888  888  88888 
  d88P   888   888  888    888 
 d8888888888   888  Y88b  d88P 
d88P     888 8888888 "Y8888P88

---------- Settings ----------
          
Model:      {model}
Output:     {output}
Degree:     {degree}
Iterations: {iterations}
Seed:       {seed}
Mode:       {mode}
Symptoms:   {symptoms_string}
______________________________\n""")

    # Import and convert tina pnml into internal petri net model
    tree = import_petri_net_from_pnml(model)
    root = tree.getroot()
    namespace = {'ns': 'http://www.pnml.org/version-2009/grammar/pnml'} # Define the namespace of the pnml files generated via tina
    namespace_delay = {'mathml': 'http://www.w3.org/1998/Math/MathML'} # Define the namespace of the pnml files generated via tina
    petri_net = _petri_net.PetriNet()
    for element in root.find("./ns:net/ns:page", namespaces=namespace):
            if element.tag.endswith("place"):
                if element.find("./ns:initialMarking", namespaces=namespace) is not None: # if place contains tokens
                    petri_net.add_place(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, int(element.find("./ns:initialMarking/ns:text", namespaces=namespace).text))
                else: # if place does not contain tokens
                    petri_net.add_place(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, 0)
            elif element.tag.endswith("transition"):
                if element.find("./ns:delay", namespaces=namespace) is not None: # if transition contains delay
                    delay_limits = element.findall('.//mathml:cn', namespaces=namespace_delay)
                    petri_net.add_transition(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, element.find("./ns:label/ns:text", namespaces=namespace).text, int(delay_limits[0].text), int(delay_limits[1].text))
                else: # if transition does not contain delay
                    petri_net.add_transition(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, element.find("./ns:label/ns:text", namespaces=namespace).text, 0, 0)
            elif element.tag.endswith("arc"):
                petri_net.add_arc(element.get("id"), element.get("source"), element.get("target"))
    
    if mode in ("debug"):
        print("***Loading PNML into internal Petri net model:")
        print_petri_net(petri_net)

    # Instantiate list of agent instruction
    instruction_list = []

    # Start iteration:
    for iteration in range(1,iterations+1):
        petri_net_modified = copy.deepcopy(petri_net)
        petri_net_temporary = copy.deepcopy(petri_net)

        # Express symptoms in the routine model    
        if symptoms != None:
            if "wandering" in symptoms:
                petri_net_modified = _symptoms.add_wandering_3(petri_net, petri_net_modified, degree, floorplan)
                petri_net_temporary = copy.deepcopy(petri_net_modified)
            if "forgetfulness" in symptoms:
                petri_net_modified = _symptoms.add_forgetfulness_2(petri_net, petri_net_modified, degree)
                petri_net_temporary = copy.deepcopy(petri_net_modified)
            if "repetitiveness" in symptoms:
                petri_net_modified = _symptoms.add_repetitiveness_3(petri_net, petri_net_modified, degree)
                petri_net_temporary = copy.deepcopy(petri_net_modified)
            
            
            

        if mode == "debug":
            print("***Petri net after symptoms have been added:")
            print_petri_net(petri_net_modified)
        
        ## simulation logic loop
        if mode in ("debug", "normal"):
            print(f"\n***Simulation {iteration}")  
        elif mode == "fast":
            print(f"***Simulation {iteration}")
        if iterations > 1:
            case_id = "case-"+str(iteration)
            instruction_list.append(case_id)

        if mode == "debug":
            print("\nState:")
            for place in petri_net_modified.places:
                print(place.name+" "+str(place.tokens))

        exit = False
        while exit == False:
            exit = True
            random.shuffle(petri_net_modified.transitions)
            for transition in petri_net_modified.transitions:
                if petri_net_modified.is_enabled(transition.id):
                    instruction = petri_net_modified.fire_transition(transition.id)
                    instruction_list.append(instruction)
                    delay = random.randint(transition.delay_lower_limit, transition.delay_upper_limit)
                    if (delay != 0):
                        instruction_list.append("wait("+str(delay)+")")
                    exit = False

                    if mode == "debug":
                        print(f"\nFire: {instruction}\n")
                        print("State:")
                        for place in petri_net_modified.places:
                            print(place.name+" "+str(place.tokens))
                    elif mode == "normal":
                        print(instruction)
                        if (delay != 0):
                            print("wait("+str(delay)+")")
    
    # Export SHAIS file for Linac 
    instruction_string = "agent(Agent1){ "+"; ".join(instruction_list)+" }"
    data = {
        "input": instruction_string
    }

    # Write the data dictionary to the JSON file
    with open(output, "w") as json_file:
        json.dump(data, json_file, indent=4)

    if mode != "invisible": 
        print(f"\n***Generated output file: {output}\n") 

        
# Main code
if __name__ == "__main__":

    # CLI arguments
    parser = argparse.ArgumentParser(description="A simulator for creating instances of agent instructions for Linac from routine models and behavioral symptoms")
    parser.add_argument("model", type=str, help="Path to routine model") #parser.add_argument("-m", "--model", type=str, help="Path to routine model", required=True)
    parser.add_argument("-i", "--iterations", type=int, default=1, help="Number of iterations simulated (default: 1)")
    parser.add_argument("-s", "--seed", default=random.randint(0, 2**32 - 1), help="Random seed (default: random 32-bit integer)")
    parser.add_argument("-d", "--degree", type=float, default=0.0, help="Degree of symptoms expressed between 0 and 1 (default: 0.0)")
    parser.add_argument("-m", "--mode", type=str, default="normal", help="Output mode (options: debug, normal, fast, invisible) (default: normal)")
    parser.add_argument("-sy", "--symptoms", nargs="+", type=str, help="The symptoms expressed in the routine (options: wandering, repetitiveness, forgetfulness)")
    parser.add_argument("-o", "--output", type=str, default="rig-output.json", help="The name of the resulting file (default: rig-output)")
    parser.add_argument("-f", "--floorplan", type=str, default=None, help="The path of the floorplan file")
    args = parser.parse_args()

    run_routine_instruction_generator(args.model, args.iterations, args.seed, args.degree, args.mode, args.symptoms, args.output, args.floorplan)


    


