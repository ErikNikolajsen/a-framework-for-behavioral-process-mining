package dk.dtu;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
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

public class SingleDatasetGenerator {
    public static void main( String[] args) throws Exception {

        int numberOfModels = 100;
        String path = "C:\\Users\\andbur\\Desktop\\models2\\";
        int minimumNumberOfTasks = 5;
        int maximumNumberOfTasks = 30;
        int minimumNumberOfGateways = 0;
        int maximumNumberOfGateways = 20;

        RandomizationConfiguration cfg = RandomizationConfiguration.BASIC_VALUES;
        cfg.setDataObjectProbability(0);

        PLGExporter e = new PLGExporter();
        for(int i = 0; i < numberOfModels; i++) {
            // generate a random process
            Process p = generate("process", cfg, minimumNumberOfTasks, maximumNumberOfTasks, minimumNumberOfGateways, maximumNumberOfGateways, null);
            Process p2 = generate("process-variation", cfg,
                    p.getTasks().size() - 2, p.getTasks().size() + 2,
                    p.getGateways().size() - 2, p.getGateways().size() + 2,
                    p);
            String id = UUID.randomUUID().toString();
            new File(path + id).mkdirs();
            e.exportModel(p, path + id + "\\model-process.plg");
            e.exportModel(p2, path + id + "\\model-process-variation.plg");
            exportProperties(p, path + id + "\\data-process.json");
            exportProperties(p2, path + id + "\\data-process-variation.json");
            generateLog(p, path + id + "\\log-process.xes");
            generateLog(p2, path + id + "\\log-process-variation.xes");
            exportDot(p, path + id + "\\graph-process.dot");
            exportDot(p2, path + id + "\\graph-process-variation.dot");
            System.out.println("exported " + id);
        }
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

    private static void generateLog(Process p, String path) throws Exception {
        LogGenerator g = new LogGenerator(p, new SimulationConfiguration(1000, NoiseConfiguration.NO_NOISE), new ProgressAdapter());
        XLog l = g.generateLog();
        XesXmlSerializer s = new XesXmlSerializer();
        s.serialize(l, new FileOutputStream(path));
    }

    private static void exportDot(Process p, String path) {
        GraphvizBPMNExporter e = new GraphvizBPMNExporter();
        e.exportModel(p, path);
    }

    private static void exportProperties(Process p, String path) throws FileNotFoundException {
        String properties = "{\n";
        properties += "\t\"no. of activities\": " + p.getTasks().size() + ",\n";
        properties += "\t\"no. of xor gateways\": " + p.getGateways().stream().filter(g -> g instanceof ExclusiveGateway).toList().size() + ",\n";
        properties += "\t\"no. of xor gateways\": " + p.getGateways().stream().filter(g -> g instanceof ParallelGateway).toList().size() + ",\n";
        properties += "\t\"no. of sequence flows\": " + p.getSequences().size() + "\n";
        properties += "}";
        // write properties string to file
        PrintWriter out = new PrintWriter(path);
        out.print(properties);
        out.close();
    }
}
