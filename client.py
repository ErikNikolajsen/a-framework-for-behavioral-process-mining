# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import requests
import json
import datetime
import random
print("Proof for the independence of the backend.\n")
response = requests.get("http://localhost:8080/api/system/ping")

print(response.text)

f = open('../floorplan(4).json',)
floorplan = json.load(f)
f.close()

url1 = "http://localhost:8080/api/roomConfig/floorplan"
url2 = "http://localhost:8080/api/simulation/input"
url3 = "http://localhost:8080/api/simulation/simulator"

floorplanreq = requests.post(url1, json = floorplan)
print("\nsending floorplan: ")
print(floorplanreq.text)

date = datetime.datetime(2022, 12, 31, 18, 0, 0, 0)

for x in range(90):
    name = "input/input" + str(x) + ".json"
    f = open(name,)
    inputFile = json.load(f)   
    f.close()
    inputreq = requests.post(url2, json = inputFile)
    print("\nsending inputs: ")
    print(inputreq.text)

    f = open('simulation.json',)
    simulator = json.load(f)
    f.close()
    date += datetime.timedelta(days=1)

    random_hour = random.uniform(0, 0.17) #0.17 = 10 minutes 
    new_time = date + datetime.timedelta(hours=random_hour)

    a = new_time.strftime('%Y-%m-%dT%H:%M:%S.%f')
    simulator["clock"] = a

    simulatorreq = requests.post(url3, json = simulator)
    print(simulatorreq)
    print("\nsending simulator info: ")
    print(simulatorreq.text)
    
    









