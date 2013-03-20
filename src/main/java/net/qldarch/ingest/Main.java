package net.qldarch.ingest;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.qldarch.ingest.transcript.TranscriptExportFactory;

public class Main {
    public static IngestStageFactory[] ingestFactories = {
        new TranscriptExportFactory(),
    };

    public static void main(String[] args) {
        Options options = ingestOptions();
        try {
            CommandLine cmdline = parseArguments(args, options);
            if (cmdline.hasOption("help")) {
                printHelp(options);
                System.exit(0);
            }

            Configuration config = new Configuration(cmdline);

            Map<String,IngestStageFactory> factories = new HashMap<String,IngestStageFactory>();
            for (IngestStageFactory factory : ingestFactories) {
                factories.put(factory.getStageName(), factory);
            }

            for (String stage : config.getStages()) {
                factories.get(stage).createIngestStage(config).ingest();
            }
        } catch (ParseException em) {
            System.err.println(em.getMessage());

            PrintWriter pw = new PrintWriter(System.err);
            new HelpFormatter().printUsage(pw, 80, "qldarch-ingest", options);
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.err.flush();
            System.out.flush();
        }
    }

    public static Options ingestOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder
                .withLongOpt("stages")
                .withDescription("Ingest stage to perform.")
                .hasArgs()
                .withValueSeparator(',')
                .create("t"));
        options.addOption(OptionBuilder
                .withLongOpt("help")
                .withDescription("Display this message")
                .create("h"));
        options.addOption(OptionBuilder
                .withLongOpt("endpoint")
                .withDescription("Server URL for a sesame-protocol sparql endpoint " +
                    "containing the archive metadata required for ingest process.")
                .hasArg(true)
                .create("e"));
        options.addOption(OptionBuilder
                .withLongOpt("repository")
                .withDescription("Repository Name identifying a repository at the " +
                    "containing the archive metadata required for ingest process.")
                .hasArg(true)
                .create("r"));
        options.addOption(OptionBuilder
                .withLongOpt("archive")
                .withDescription("URL prefix where the omeka archive files are stored.")
                .hasArg(true)
                .create("a"));
        options.addOption(OptionBuilder
                .withLongOpt("outputdir")
                .withDescription("A directory, preferably empty, where output files are written.")
                .hasArg(true)
                .create("o"));

        return options;
    }

    public static CommandLine parseArguments(String[] args, Options options) throws ParseException {
        return new GnuParser().parse(options, args);
    }

    public static void printHelp(Options options) {
        PrintWriter pw = new PrintWriter(System.err);
        new HelpFormatter().printHelp(pw, 80,
                "java -jar qldarch-ingest.jar [OPTIONS]\n",
                "Digital Archive of Queensland Architecture Ingest Tool\n",
                options, 5, 3, "");
        pw.flush();
    }
}
