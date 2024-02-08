package dk.dtu;

import plg.generator.ProgressAdapter;
import plg.generator.log.LogGenerator;
import plg.generator.log.SimulationConfiguration;
import plg.generator.log.noise.NoiseConfiguration;
import plg.generator.process.ProcessGenerator;
import plg.generator.process.RandomizationConfiguration;
import plg.io.exporter.GraphvizBPMNExporter;
import plg.io.exporter.PLGExporter;
import plg.model.Process;
import plg.model.gateway.ExclusiveGateway;
import plg.model.gateway.ParallelGateway;
import java.io.*;
import java.util.UUID;

import plg.io.exporter.PNMLExporter;

import plg.utils.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import plg.model.gateway.Gateway;

public class Main {
    public static void main( String[] args) throws Exception {

        // output configuration
        int numberOfModels = 3;
        String path = "C:\\Users\\User\\Documents\\GitHub\\a-concretization-of-human-behavior-analysis-via-process-mining\\model-generator\\generated-models\\";
        int minimumNumberOfTasks = 5;
        int maximumNumberOfTasks = 30;
        int minimumNumberOfGateways = 0;
        int maximumNumberOfGateways = 20;
        
        // Set random configuration
        RandomizationConfiguration cfg = RandomizationConfiguration.BASIC_VALUES;
        cfg.setDataObjectProbability(0);

        PNMLExporter e = new PNMLExporter();
        int numberOfGeneratedModels = 0;
        //while (numberOfGeneratedModels != numberOfModels) {
            // generate a random process
            Process p = generate("process", cfg, minimumNumberOfTasks, maximumNumberOfTasks, minimumNumberOfGateways, maximumNumberOfGateways, null);
            String id = UUID.randomUUID().toString();

            System.out.println("exported " + id);
            Measures m = CFCMetric.analyze(p);
            double cfc = m.get("CFC");
            System.out.println("CFC: " + cfc);

            // export process as file
            //if (cfc >= 2 && cfc <= 3) {
            numberOfGeneratedModels++;
            maximumNumberOfGateways++;
            //new File(path + id).mkdirs();
            //e.exportModel(p, path + id + "\\model-process-"+cfc+".pnml");
            e.exportModel(p, path + "model-process-"+cfc+".pnml");
            //}
        //}
        
    }

    private static Process generate(String name,
                                 RandomizationConfiguration cfg,
                                 int minNumberOfActivities,
                                 int maxNumberOfActivities,
                                 int minNumberOfGateways,
                                 int maxNumberOfGateways,
                                 Process differentFrom) {
        Process p = null;
        boolean sizeInBounds;
        boolean sizeEqualToReference;

        do {
            p = new Process(name);
            System.out.println("generating process ------------------");
            ProcessGenerator.randomizeProcess(p, cfg);

            sizeInBounds =
                    p.getTasks().size() >= minNumberOfActivities && p.getTasks().size() <= maxNumberOfActivities &&
                    p.getGateways().size() >= minNumberOfGateways && p.getGateways().size() <= maxNumberOfGateways;
            sizeEqualToReference =
                    differentFrom != null &&
                    (p.getTasks().size() == differentFrom.getTasks().size() && p.getGateways().size() == differentFrom.getGateways().size());

        } while(!sizeInBounds || sizeEqualToReference);
        return p;
    }
    
}

class Measures extends HashMap<String, Double> {

    public Measures() {
        super();
    }

    public Measures(String name, Double value) {
        put(name, value);
    }

    public Measures add(String name, Double value) {
        put(name, value);
        return this;
    }

    public Set<String> getMeasures() {
        return keySet();
    }

    public Measures join(Measures other) {
        Measures result = new Measures();
        for (Map.Entry<String, Double> entry : entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Double> entry : other.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}

class CFCMetric {
    static public Measures analyze(Process process) {
        double tot_gateways = 0;
        double cfc_abs = 0;
        for (Gateway g : process.getGateways()) {
            if (g instanceof ExclusiveGateway && g.getOutgoingObjects().size() > 1) {
                // XOR split
                cfc_abs += g.getOutgoingObjects().size();
                tot_gateways++;
            } else if (g instanceof ParallelGateway && g.getOutgoingObjects().size() > 1) {
                // AND split
                cfc_abs += 1;
                tot_gateways++;
            }
        }

        return new Measures("CFC", cfc_abs / tot_gateways);
    }
}
