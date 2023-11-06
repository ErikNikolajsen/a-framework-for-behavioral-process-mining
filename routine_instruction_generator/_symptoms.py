import random
import uuid

def add_repetitive_behavior(petri_net, petri_net_modified, degree,):
    #print("*Added repetitive behavior") #test
    repetitive_name_counter = 0
    original_transitions = petri_net.transitions.copy()
    
    # Remove deadend transitions
    for transition in original_transitions:
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
        petri_net_modified.add_transition(new_transition_id, "rt"+str(repetitive_name_counter), f"repeat {transition.name}") # TODO change label to ""
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

def add_wandering_behavior(petri_net, petri_net_modified, degree):
    wandering_name_counter = 0


    for i in range(round(len(petri_net.arcs)*degree)):
        random_arc = random.choice(petri_net_modified.arcs)
        source = random_arc.source
        target = random_arc.target 
        for arc in petri_net_modified.arcs:
            if arc.id == random_arc.id:
                petri_net_modified.arcs.remove(arc)
        new_transition_id = str(uuid.uuid4()).replace("-", "")[:15]
        petri_net_modified.add_transition(new_transition_id, "wt"+str(wandering_name_counter), f"goto({random.randint(0, 10)},{random.randint(0, 10)})") # TODO make label go to random walkable tile 
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