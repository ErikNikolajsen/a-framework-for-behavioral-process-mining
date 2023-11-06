class PetriNet():

    def __init__(self):
        self.places = []
        self.transitions = []
        self.arcs = []

    def add_place(self, id, name, tokens):
        self.places.append(Place(id, name, tokens))
        return self

    def add_transition(self, id, name, label):
        self.transitions.append(Transition(id, name, label))
        return self

    def add_arc(self, id, source, target):
        self.arcs.append(Arc(id, source,target))
        return self

    def get_place(self, id):
        for place in self.places:
            if place.id == id:
                return place
            
    def get_transition(self, id):
        for transition in self.transitions:
            if transition.id == id:
                return transition
            
    def get_arc(self, id):
        for arc in self.arcs:
            if arc.id == id:
                return arc

    def get_tokens(self, place_id):
        for place in self.places:
            if place.id == place_id:
                return place.tokens

    def is_enabled(self, transition_id):
        for arc in self.arcs:
            if arc.target == transition_id:
                if self.get_tokens(arc.source) < 1:
                    return False
        return True

    def add_marking(self, place_id):
        for place in self.places:
            if place.id == place_id:
                place.tokens += 1
                return 
        
    def fire_transition(self, transition_id):
        if self.is_enabled(transition_id):
            for arc in self.arcs:
                if arc.target == transition_id:
                    for i in self.places:
                        if i.id == arc.source:
                            i.tokens -= 1
                if arc.source == transition_id:
                    for i in self.places:
                        if i.id == arc.target:
                            i.tokens += 1
            return self.get_transition(transition_id).label
        
    def get_type(self, id):
        for place in self.places:
            if place.id == id:
                return "place"
        for transition in self.transitions:
            if transition.id == id:
                return "transition"
        for arc in self.arcs:
            if arc.id == id:
                return "place"
   


class Place():
    def __init__(self, id, name, tokens):
        self.id = id
        self.name = name
        self.tokens = tokens

class Transition():
    def __init__(self, id, name, label):
        self.id = id
        self.name = name
        self.label = label

class Arc():
    def __init__(self, id, source, target):
        self.id = id
        self.source = source
        self.target = target
        
