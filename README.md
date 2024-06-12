# A Framework for Behavioral Process Mining


## Instructions for Running the Validation Framework Pipeline
The individual tools are run on a Windows machine as follows.

### TINA

1.  Open the NetDraw editor found in `\tina-3.7.5\bin\nd.exe`

2.  Model your routine model.

3.  Export as PNML via `File > export > net to pnml`.

4.  Save the PNML file to the root directory of the supplied repository.

### WoPeD

**[Optional]** - How to ensure that a modeled routine is a
sound workflow net via WoPeD.

1.  Install the program via `\WoPeD\WoPeD-install-windows-3.8.0.exe`

2.  Run WoPeD.

3.  Open the routine model via `File > Open`.

4.  Analyze via `Analyze > Semantical analysis`.

5.  See results in right-hand sidebar under `Wizard` and `Expert`.

### Linac Frontend

1.  Have Node.js installed - it has been confirmed to work with version
    14.16.0.

2.  Open a CLI in with the current working directory being
    `\linac-frontend\`

3.  Install project dependencies via:\
    `npm install`

4.  Run the project via:\
    `npm run serve`

5.  Open a web browser and go to:\
    <http://localhost:8081/>

6.  Design a floorplan.

7.  Download the floorplan to the root directory of the supplied
    repository.

### Agent Instructions Generator

**[Optional]** - Instructions for how to run the Agent
Instructions Generator as a CLI application separately from SimCoor.

1.  Have Python installed - it has been confirmed to work with version
    3.11.0.

2.  Open a CLI in with the current working directory being the root of
    the supplied repository.

3.  To see the interface options type:\
    `python -m agent_instructions_generator.aig –help`

4.  An example of a command could be:\
    `python -m agent_instructions_generator.aig routine_model.pnml –output aig-output.json –degree 0.5 –iterations 1 –seed 2803371398 –mode normal –symptoms repetitiveness forgetfulness`

5.  The resulting output file should be located in the current working
    directory.

### Linac Backend

**[Optional]** - Instructions for how to run the Linac
backend separately from SimCoor.

1.  Have Java installed - it has been confirmed to work with version
    17.0.10.

2.  Have Apache Maven installed - it has been confirmed to work with
    version 3.9.6.

3.  Open a CLI in with the current working directory being
    `\linac-backend\`

4.  Compile the application via:\
    `mvn compile`

5.  Execute the Java class via:\
    `mvn exec:java`

6.  Start the application via the command:\
    `mvn spring-boot:run`

7.  Open another CLI in with the current working directory being\
    `\linac-backend\python-client\`

8.  Place your input files here with the filenames being:\
    `floorplan.json`\
    `input.json`\
    `simulator.json`

9.  Run the simulation via:\
    `python client.py`

10. The resulting CSV file is located in `\linac-backend\`.

### SimCoor

1.  Start the Linac backend.

    1.  Have Java installed - it has been confirmed to work with version
        17.0.10.

    2.  Have Apache Maven installed - it has been confirmed to work with
        version 3.9.6.

    3.  Open a CLI in with the current working directory being
        `\linac-backend\`

    4.  Compile the application via:\
        `mvn compile`

    5.  Execute the Java class via:\
        `mvn exec:java`

    6.  Start the application via the command:\
        `mvn spring-boot:run`

2.  Run SimCoor.

    1.  Have Python installed - it has been confirmed to work with
        version 3.11.0.

    2.  Have the pm4py package installed via:\
        `pip install pm4py`

    3.  Open a CLI in with the current working directory being the root
        of the supplied repository.

    4.  Open the file `simcoor.py` and edit it to specify what
        simulations should be run.

    5.  Run the simulations via:\
        `python simcoor.py`

    6.  The resulting XES files should appear in the current working
        directory.

### Coral

1.  Have Python installed - it has been confirmed to work with version
    3.11.0.

2.  Open a CLI in with the current working directory being the root of
    the supplied repository.

3.  To see the positional arguments type:\
    `python coral.py –help`

4.  An example of a command could be:\
    `python coral.py coral-data.csv 0 38 0 6`
