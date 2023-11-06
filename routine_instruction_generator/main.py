# Author: Erik Ravn Nikolajsen 
# Copyright (c) 2023 Erik Ravn Nikolajsen
# License: MIT License

# Import statements
import sys # for halting the program
import argparse # for parsing arguments in the command line interface
import random # for randomization
import xml.etree.ElementTree as ET # datastructure for storing petri nets
import json # for exporting SHAIS input files for Linac
from _petri_net import PetriNet
import _symptoms
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
    print("\nTransitions: \n(id, name, label)")
    for transition in net.transitions:
        print(transition.id+", "+transition.name+", "+transition.label)
    print("\nArcs: \n(id, source, target)")
    for arc in net.arcs:
        print(arc.id+", "+arc.source+", "+arc.target)
    print("")
        
# Main code
if __name__ == "__main__":

    # CLI arguments
    parser = argparse.ArgumentParser(description="A simulator for creating instances of agent instructions for Linac from routine models and behavioral symptoms")
    parser.add_argument("model", type=str, help="Path to routine model") #parser.add_argument("-m", "--model", type=str, help="Path to routine model", required=True)
    parser.add_argument("-i", "--iterations", type=int, default=1, help="Number of iterations simulated (default: 1)")
    parser.add_argument("-s", "--seed", default=random.randint(0, 2**32 - 1), help="Random seed (default: random 32-bit integer)")
    parser.add_argument("-d", "--degree", type=float, default=0.0, help="Degree of symptoms expressed between 0 and 1 (default: 0.0)")
    parser.add_argument("-m", "--mode", type=str, default="normal", help="Output mode (options: debug, normal, production) (default: normal)")
    parser.add_argument("-sy", "--symptoms", nargs="+", type=str, help="The symptoms expressed in the routine (options: wandering, repetitiveness)")
    args = parser.parse_args()

    # Process CLI arguments
    random.seed(args.seed) # Set random seed value
    symptoms = ",\n            ".join(args.symptoms)
    if args.mode not in ("debug", "normal", "fast"):
        print(f"Error: the mode '{args.mode}' is not an option")
        print("Halting program\n")
        sys.exit(1)
    for symptom in args.symptoms:
        if symptom not in ("wandering", "repetitiveness"):
            print(f"Error: the symptom '{symptom}' is not an option")
            print("Halting program\n")
            sys.exit(1)
    if not 0.0 <= args.degree <= 1.0:
        print(f"Error: the degree '{args.degree}' is not within the interval [0.0, 1.0]")
        print("Halting program\n")
        sys.exit(1)
    if not args.iterations >= 1:
        print(f"Error: the iteration '{args.iterations}' is less than 1")
        print("Halting program\n")
        sys.exit(1)
    
    
    print(f"""
8888888b.  8888888 .d8888b.  
888   Y88b   888  d88P  Y88b 
888    888   888  888    888 
888   d88P   888  888        
8888888P"    888  888  88888 
888 T88b     888  888    888 
888  T88b    888  Y88b  d88P 
888   T88b 8888888 "Y8888P88

--------- Settings ---------
          
Model:      {args.model}
Degree:     {args.degree}
Iterations: {args.iterations}
Seed:       {args.seed}
Mode:       {args.mode}
Symptoms:   {symptoms}
____________________________\n""")

    # Import and convert tina pnml into internal petri net model
    tree = import_petri_net_from_pnml(args.model)
    root = tree.getroot()
    namespace = {'ns': 'http://www.pnml.org/version-2009/grammar/pnml'} # Define the namespace of the pnml files generated via tina
    petri_net = PetriNet()
    for element in root.find("./ns:net/ns:page", namespaces=namespace):
            if element.tag.endswith("place"):
                if element.find("./ns:initialMarking", namespaces=namespace) is not None:
                    petri_net.add_place(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, int(element.find("./ns:initialMarking/ns:text", namespaces=namespace).text))
                else:
                    petri_net.add_place(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, 0)
            elif element.tag.endswith("transition"):
                petri_net.add_transition(element.get("id"), element.find("./ns:name/ns:text", namespaces=namespace).text, element.find("./ns:label/ns:text", namespaces=namespace).text)
            elif element.tag.endswith("arc"):
                petri_net.add_arc(element.get("id"), element.get("source"), element.get("target"))
    
    if args.mode in ("debug", "normal"):
        print("***Loading PNML into internal Petri net model:")
        print_petri_net(petri_net)

    # Instantiate list of agent instruction
    instruction_list = []

    # Start iteration:
    for iteration in range(1,args.iterations+1):
        petri_net_modified = copy.deepcopy(petri_net)

        # Express symptoms in the routine model    
        if args.symptoms != None:
            if "repetitiveness" in args.symptoms:
                petri_net_modified = _symptoms.add_repetitive_behavior(petri_net, petri_net_modified, args.degree)
            if "wandering" in args.symptoms:
                petri_net_modified = _symptoms.add_wandering_behavior(petri_net, petri_net_modified, args.degree)

        if args.mode == "debug":
            print("***Petri net after symptoms have been added:")
            print_petri_net(petri_net_modified)
        
        ## simulation logic loop
        print(f"\n***Simulation {iteration} commenced:") if args.mode in ("debug", "normal") else print(f"***Simulation {iteration} commenced")
        case_id = "case-"+str(iteration)
        instruction_list.append(case_id)
        if args.mode in ("debug", "normal"): print(case_id)

        if args.mode == "debug":
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
                    exit = False

                    if args.mode == "debug":
                        print(f"\nFire: {instruction}\n")
                        print("State:")
                        for place in petri_net_modified.places:
                            print(place.name+" "+str(place.tokens))
                    elif args.mode == "normal":
                        print(instruction)
    
    # Export SHAIS file for Linac 
    instruction_string = "; ".join(instruction_list)
    data = {
        "input": instruction_string
    }
    
    # Specify the file path for the JSON file
    json_file_path = "shais-input.json"

    # Write the data dictionary to the JSON file
    with open(json_file_path, "w") as json_file:
        json.dump(data, json_file, indent=4)

    print(f"\n***Generated Linac input: {json_file_path}\n")
    


