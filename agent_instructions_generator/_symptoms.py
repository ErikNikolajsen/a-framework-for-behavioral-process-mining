import random
import uuid
import json

def add_repetitiveness_1(petri_net, petri_net_modified, degree):
    #print("*Added repetitive behavior") #test
    repetitive_name_counter = 0
    original_transitions = petri_net.transitions.copy()
    
    # Remove deadend transitions
    for transition in petri_net.transitions:
        #print(transition.name)
        deadend_transition = True
        for arc in petri_net_modified.arcs:
            if arc.source == transition.id:
                for arc2 in petri_net_modified.arcs:
                    if arc.target == arc2.source:
                        deadend_transition = False
        if deadend_transition == True:
            original_transitions.remove(transition)
            #print(f"Removed deadend transition: {transition.name}") #test
        
    random.shuffle(original_transitions)
    for i in range(round(len(original_transitions)*degree)):
        transition = original_transitions[i] # pick random transition
        #print("Picked transition: "+transition.id) # test
        new_transition_id = str(uuid.uuid4()).replace("-", "")[:15]
        petri_net_modified.add_transition(new_transition_id, "rt"+str(repetitive_name_counter), "", 0, 0) # TODO change label to "" f"repeat {transition.name}"
        repetitive_name_counter += 1
        outgoing_arcs = []
        incoming_arcs = []
        for arc in petri_net_modified.arcs:
            if arc.source == transition.id:
                outgoing_arcs.append(arc)
            elif arc.target == transition.id:
                incoming_arcs.append(arc)

        for arc in outgoing_arcs:
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], arc.target, new_transition_id)
        for arc in incoming_arcs:
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], new_transition_id, arc.source)
        
    return petri_net_modified

def add_repetitiveness_2(petri_net, petri_net_modified, degree):
    #print("*Added repetitive behavior") #test
    repetitive_name_counter = 0
    original_transitions = petri_net.transitions.copy()
    
    # Remove deadend transitions
    for transition in petri_net.transitions:
        #print(transition.name)
        deadend_transition = True
        for arc in petri_net_modified.arcs:
            if arc.source == transition.id:
                for arc2 in petri_net_modified.arcs:
                    if arc.target == arc2.source:
                        deadend_transition = False
        if deadend_transition == True:
            original_transitions.remove(transition)
            #print(f"Removed deadend transition: {transition.name}") #test
        
    random.shuffle(original_transitions)
    for i in range(round(len(original_transitions)*degree)):
        transition = original_transitions[i] # pick random transition
        #print("Picked transition: "+transition.id) # test
        new_transition_id = str(uuid.uuid4()).replace("-", "")[:15]
        petri_net_modified.add_transition(new_transition_id, "rt"+str(repetitive_name_counter), transition.label, transition.delay_lower_limit, transition.delay_upper_limit)
        repetitive_name_counter += 1
        #Find all place targets of outgoing arcs from picked transition
        target_places_IDs = []
        for arc in petri_net_modified.arcs:
            if arc.source == transition.id:
                target_places_IDs.append(arc.target)
        #Add new arcs to new transition
        for place_ID in target_places_IDs:
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], place_ID, new_transition_id)
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], new_transition_id, place_ID)
    return petri_net_modified

def add_repetitiveness_3(petri_net, petri_net_modified, degree):
    name_counter = 0
    original_transitions = petri_net.transitions.copy()
        
    random.shuffle(original_transitions)
    for i in range(round(len(original_transitions)*degree)):
        picked_transition = original_transitions[i] # pick random transition sample
        # log original source and target places and remove respectively outgoing and incoming arcs
        source_places = []
        target_places = []
        for arc in petri_net_modified.arcs[:]: #iterate copy
            if arc.target == picked_transition.id:
                source_places.append(arc.source)
                petri_net_modified.arcs.remove(arc)
            if arc.source == picked_transition.id:
                target_places.append(arc.target)
                petri_net_modified.arcs.remove(arc)
        # make two new places
        p3_id = str(uuid.uuid4()).replace("-", "")[:15] #before
        p4_id = str(uuid.uuid4()).replace("-", "")[:15] #after
        petri_net_modified.add_place(p3_id,"rp"+str(name_counter),0)
        name_counter += 1
        petri_net_modified.add_place(p4_id,"rp"+str(name_counter),0)
        # make three new transitions
        t2_id = str(uuid.uuid4()).replace("-", "")[:15] #before 
        t3_id = str(uuid.uuid4()).replace("-", "")[:15] #after
        t4_id = str(uuid.uuid4()).replace("-", "")[:15] #above
        name_counter += 1
        petri_net_modified.add_transition(t2_id, "rt"+str(name_counter), "", 0, 0)
        name_counter += 1
        petri_net_modified.add_transition(t3_id, "rt"+str(name_counter), "", 0, 0)
        name_counter += 1
        petri_net_modified.add_transition(t4_id, "rt"+str(name_counter), "", 0, 0)
        # make new arcs (see sketch for numbering references)
        for source_place in source_places: #1
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], source_place, t2_id)
        for target_place in target_places: #2
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t3_id, target_place)
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t2_id, p3_id) #3
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p3_id, picked_transition.id) #4
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], picked_transition.id, p4_id) #5
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p4_id, t3_id) #6
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p4_id, t4_id) #7
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t4_id, p3_id) #8
    return petri_net_modified

######################################################################################################################################

def add_wandering(petri_net, petri_net_modified, degree, floorplan):
    
    # Load in floorplan to determine walkable coordinates 
    with open(floorplan) as f:
        data = json.load(f)

    # Extract coordinates of passiveSensors' physicalArea where walkable is False
    passive_sensors_coords = []
    for sensor in data.get('passiveSensors', []):
        if not sensor.get('walkable', True):
            passive_sensors_coords.extend([(area['x'], area['y']) for area in sensor.get('physicalArea', [])])

    # Extract coordinates of passiveSensors' physicalArea where walkable is False
    active_sensors_coords = []
    for sensor in data.get('activeSensors', []):
        if not sensor.get('walkable', True):
            active_sensors_coords.extend([(area['x'], area['y']) for area in sensor.get('physicalArea', [])])

    # Extract coordinates of walls
    wall_coordinates = [(wall['x'], wall['y']) for wall in data.get('walls', [])]

    # Coordinates of unreachable tiles (STOPGAP)
    unreachable_tiles = [(0,13),(1,13),(2,13),(3,13),(4,13),(5,13),(0,14),(1,14),(2,14),(3,14),(4,14),(5,14),(0,15),(1,15),(2,15),(3,15),(4,15),(5,15),(0,16),(1,16),(2,16),(3,16),(4,16),(5,16)]

    non_walkable_coordinates = passive_sensors_coords + active_sensors_coords + wall_coordinates + unreachable_tiles

    width = data.get('width')
    height = data.get('height')

    #print(f"Coordinates of passiveSensors' physicalArea where walkable is False: {passive_sensors_coords}") # For testing
    #print(f"Coordinates of passiveSensors' physicalArea where walkable is False: {active_sensors_coords}") # For testing
    #print(f"Coordinates of walls: {wall_coordinates}") # For testing

    # Generate list of all coordinates within the grid
    all_coordinates = [(x, y) for x in range(width) for y in range(height)]

    # Filter walkable coordinates
    walkable_coordinates = [coord for coord in all_coordinates if coord not in non_walkable_coordinates]
    
    # Add wandering behavior
    wandering_name_counter = 0
    
    for i in range(round(len(petri_net.arcs)*degree)):
        random_arc = random.choice(petri_net_modified.arcs)
        source = random_arc.source
        target = random_arc.target 
        for arc in petri_net_modified.arcs:
            if arc.id == random_arc.id:
                petri_net_modified.arcs.remove(arc)
        new_transition_id = str(uuid.uuid4()).replace("-", "")[:15]
        random_walkable_tile = random.choice(walkable_coordinates)
        petri_net_modified.add_transition(new_transition_id, "wt"+str(wandering_name_counter), f"goto({random_walkable_tile[0]},{random_walkable_tile[1]})", 0, 0) # TODO make label go to random walkable tile 
        wandering_name_counter += 1
        new_place_id = str(uuid.uuid4()).replace("-", "")[:15]
        petri_net_modified.add_place(new_place_id, "wp"+str(wandering_name_counter), 0)
        wandering_name_counter += 1

        if petri_net_modified.get_type(source) == "place":
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], source, new_transition_id)
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], new_transition_id, new_place_id)
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], new_place_id, target)
        else:
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], source, new_place_id)
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], new_place_id, new_transition_id)
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], new_transition_id, target)
    return petri_net_modified

def add_wandering_2(petri_net, petri_net_modified, degree, floorplan):
    # Load in floorplan to determine walkable coordinates 
    with open(floorplan) as f:
        data = json.load(f)
    # Extract coordinates of passiveSensors' physicalArea where walkable is False
    passive_sensors_coords = []
    for sensor in data.get('passiveSensors', []):
        if not sensor.get('walkable', True):
            passive_sensors_coords.extend([(area['x'], area['y']) for area in sensor.get('physicalArea', [])])
    # Extract coordinates of passiveSensors' physicalArea where walkable is False
    active_sensors_coords = []
    for sensor in data.get('activeSensors', []):
        if not sensor.get('walkable', True):
            active_sensors_coords.extend([(area['x'], area['y']) for area in sensor.get('physicalArea', [])])
    # Extract coordinates of walls
    wall_coordinates = [(wall['x'], wall['y']) for wall in data.get('walls', [])]
    # Coordinates of unreachable tiles (STOPGAP)
    unreachable_tiles = [(0,13),(1,13),(2,13),(3,13),(4,13),(5,13),(0,14),(1,14),(2,14),(3,14),(4,14),(5,14),(0,15),(1,15),(2,15),(3,15),(4,15),(5,15),(0,16),(1,16),(2,16),(3,16),(4,16),(5,16)]
    # Collect non-walkable tiles in a single list
    non_walkable_coordinates = passive_sensors_coords + active_sensors_coords + wall_coordinates + unreachable_tiles
    # Generate list of all coordinates within the grid
    width = data.get('width')
    height = data.get('height')
    all_coordinates = [(x, y) for x in range(width) for y in range(height)]
    # Filter for walkable coordinates
    walkable_coordinates = [coord for coord in all_coordinates if coord not in non_walkable_coordinates]
    # Add wandering behavior
    name_counter = 0
    original_transitions = petri_net.transitions.copy()
    random.shuffle(original_transitions)
    for i in range(round(len(petri_net.transitions)*degree)):
        picked_transition = original_transitions[i] # pick random transition sample
        # log original source places and remove outgoing arcs
        source_places = []
        for arc in petri_net_modified.arcs[:]: #iterate copy
            if arc.target == picked_transition.id:
                source_places.append(arc.source)
                petri_net_modified.arcs.remove(arc)
        # make one new place
        p3_id = str(uuid.uuid4()).replace("-", "")[:15] #before
        petri_net_modified.add_place(p3_id,"wp"+str(name_counter),0)
        # make one new transitions
        t2_id = str(uuid.uuid4()).replace("-", "")[:15] #before
        name_counter += 1
        random_walkable_tile = random.choice(walkable_coordinates)
        petri_net_modified.add_transition(t2_id, "wt"+str(name_counter), f"goto({random_walkable_tile[0]},{random_walkable_tile[1]})", 0, 0)
        # make new arcs (see sketch for numbering references)
        for source_place in source_places: #1
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], source_place, t2_id)
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t2_id, p3_id) #2
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p3_id, picked_transition.id) #3
    return petri_net_modified

def add_wandering_3(petri_net, petri_net_modified, degree, floorplan):
    # Load in floorplan to determine walkable coordinates 
    with open(floorplan) as f:
        data = json.load(f)
    # Extract coordinates of passiveSensors' physicalArea where walkable is False
    passive_sensors_coords = []
    for sensor in data.get('passiveSensors', []):
        if not sensor.get('walkable', True):
            passive_sensors_coords.extend([(area['x'], area['y']) for area in sensor.get('physicalArea', [])])
    # Extract coordinates of passiveSensors' physicalArea where walkable is False
    active_sensors_coords = []
    for sensor in data.get('activeSensors', []):
        if not sensor.get('walkable', True):
            active_sensors_coords.extend([(area['x'], area['y']) for area in sensor.get('physicalArea', [])])
    # Extract coordinates of walls
    wall_coordinates = [(wall['x'], wall['y']) for wall in data.get('walls', [])]
    # Coordinates of unreachable tiles (STOPGAP)
    unreachable_tiles = [(0,13),(1,13),(2,13),(3,13),(4,13),(5,13),(0,14),(1,14),(2,14),(3,14),(4,14),(5,14),(0,15),(1,15),(2,15),(3,15),(4,15),(5,15),(0,16),(1,16),(2,16),(3,16),(4,16),(5,16)]
    # Collect non-walkable tiles in a single list
    non_walkable_coordinates = passive_sensors_coords + active_sensors_coords + wall_coordinates + unreachable_tiles
    # Generate list of all coordinates within the grid
    width = data.get('width')
    height = data.get('height')
    all_coordinates = [(x, y) for x in range(width) for y in range(height)]
    # Filter for walkable coordinates
    walkable_coordinates = [coord for coord in all_coordinates if coord not in non_walkable_coordinates]
    # Add wandering behavior
    name_counter = 0
    original_transitions = petri_net.transitions.copy()
    random.shuffle(original_transitions)
    for i in range(round(len(petri_net.transitions)*degree)):
        picked_transition = original_transitions[i] # pick random transition sample
        # log original source places and remove outgoing arcs
        source_places = []
        for arc in petri_net_modified.arcs[:]: #iterate copy
            if arc.target == picked_transition.id:
                source_places.append(arc.source)
                petri_net_modified.arcs.remove(arc)
        # make two new places
        p3_id = str(uuid.uuid4()).replace("-", "")[:15]
        p4_id = str(uuid.uuid4()).replace("-", "")[:15]
        petri_net_modified.add_place(p3_id,"wp"+str(name_counter),0)
        name_counter += 1
        petri_net_modified.add_place(p4_id,"wp"+str(name_counter),0)
        # make three new transitions
        t2_id = str(uuid.uuid4()).replace("-", "")[:15]
        t3_id = str(uuid.uuid4()).replace("-", "")[:15]
        t4_id = str(uuid.uuid4()).replace("-", "")[:15]
        name_counter += 1
        petri_net_modified.add_transition(t2_id, "wt"+str(name_counter), "", 0, 0)
        name_counter += 1
        petri_net_modified.add_transition(t3_id, "wt"+str(name_counter), "", 0, 0)
        name_counter += 1
        random_walkable_tile = random.choice(walkable_coordinates)
        petri_net_modified.add_transition(t4_id, "wt"+str(name_counter), f"goto({random_walkable_tile[0]},{random_walkable_tile[1]})", 0, 0)
        # make new arcs (see sketch for numbering references)
        for source_place in source_places: #1
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], source_place, t2_id)
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t2_id, p3_id) #2
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p3_id, t3_id) #3
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p3_id, t4_id) #4
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t3_id, p4_id) #5
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t4_id, p4_id) #6
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p4_id, picked_transition.id) #7
    return petri_net_modified

#################################################################################################################

def add_forgetfulness(petri_net, petri_net_modified, degree):
    original_transitions = petri_net.transitions.copy()
        
    random.shuffle(original_transitions)
    for i in range(round((len(original_transitions)*degree)/4)):
        picked_transition = original_transitions[i] # pick random transition
        #print("Picked transition: "+picked_transition.label) # test
        #transform picked transition into dummy transition
        for transition in petri_net_modified.transitions:
            if transition.id == picked_transition.id:
                transition.label = ""
                transition.delay_lower_limit = 0
                transition.delay_upper_limit = 0
        
    return petri_net_modified

def add_forgetfulness_2(petri_net, petri_net_modified, degree):
    name_counter = 0
    original_transitions = petri_net.transitions.copy()
    random.shuffle(original_transitions)
    for i in range(round(len(original_transitions)*degree)):
        picked_transition = original_transitions[i] # pick random transition sample
        # log original source and target places and remove respectively outgoing and incoming arcs
        source_places = []
        target_places = []
        for arc in petri_net_modified.arcs[:]: #iterate copy
            if arc.target == picked_transition.id:
                source_places.append(arc.source)
                petri_net_modified.arcs.remove(arc)
            if arc.source == picked_transition.id:
                target_places.append(arc.target)
                petri_net_modified.arcs.remove(arc)
        # make two new places
        p3_id = str(uuid.uuid4()).replace("-", "")[:15] #before
        p4_id = str(uuid.uuid4()).replace("-", "")[:15] #after
        petri_net_modified.add_place(p3_id,"fp"+str(name_counter),0)
        name_counter += 1
        petri_net_modified.add_place(p4_id,"fp"+str(name_counter),0)
        # make three new transitions
        t2_id = str(uuid.uuid4()).replace("-", "")[:15] #before 
        t3_id = str(uuid.uuid4()).replace("-", "")[:15] #after
        t4_id = str(uuid.uuid4()).replace("-", "")[:15] #above
        name_counter += 1
        petri_net_modified.add_transition(t2_id, "ft"+str(name_counter), "", 0, 0)
        name_counter += 1
        petri_net_modified.add_transition(t3_id, "ft"+str(name_counter), "", 0, 0)
        name_counter += 1
        petri_net_modified.add_transition(t4_id, "ft"+str(name_counter), "", 0, 0)
        # make new arcs (see sketch for numbering references)
        for source_place in source_places: #1
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], source_place, t2_id)
        for target_place in target_places: #2
            petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t3_id, target_place)
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t2_id, p3_id) #3
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p3_id, picked_transition.id) #4
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], picked_transition.id, p4_id) #5
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p4_id, t3_id) #6
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], p3_id, t4_id) #7
        petri_net_modified.add_arc(str(uuid.uuid4()).replace("-", "")[:15], t4_id, p4_id) #8
    return petri_net_modified


##################################################################################################################

def add_slowness(petri_net, petri_net_modified, degree):
    name_counter = 0
    original_transitions = petri_net.transitions.copy()
    random.shuffle(original_transitions)
    for i in range(round(len(original_transitions)*degree)):
        picked_transition = original_transitions[i] # pick random transition sample
        petri_net_modified.get_transition(picked_transition.id).delay_lower_limit *= 5
        petri_net_modified.get_transition(picked_transition.id).delay_upper_limit *= 5
    return petri_net_modified
