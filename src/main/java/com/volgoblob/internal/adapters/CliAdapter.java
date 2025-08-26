package com.volgoblob.internal.adapters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.volgoblob.internal.usecase.AggregateJsonUseCase;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This class has user's argements from command line.
 */
@Command(
    name = "Stats-CLI", 
    version = "Stats-CLI 1.0 alpha", 
    mixinStandardHelpOptions = true,
    description = "Calculate stats with aggregation function in inputed json files"
)
public class CliAdapter implements Runnable {

    /**
     * aggregationName contains aggregation name from command line.
     */
    @Option(names = "-a", required = true, description = "This function will be use to calculate stats in specified field")
    private String aggregationName;

    /**
     * fieldName contains json field name from command line.
     */
    @Option(names = "-f", required = true, description = "The name of json field that will be calculate by function")
    private String fieldName;

    /**
     * groupFields contains group of json names to add it into result json.
     */
    @Option(names = "-g", required = false, split = ",", description = "This names of json fields will be use in result json")
    private List<String> groupFields;

    /**
     * jsonFile contains name of file with json data to calculate it.
     */
    @Option(names = "-d", required = true, description = "Path to the json file")
    private String jsonFile;

    private AggregateJsonUseCase aggregateJsonUseCase;

    public CliAdapter() {}

    public CliAdapter(AggregateJsonUseCase aggregateJsonUseCase) {
        this.aggregateJsonUseCase = aggregateJsonUseCase;
    }

    public CliAdapter(String aggregationName, String fieldName, List<String> groupFields, String jsonFile) {
        this.aggregationName = aggregationName;
        this.fieldName = fieldName;
        this.groupFields = groupFields;
        this.jsonFile = jsonFile;
    }

    /**
     * This is entry point to usecase logic of application
     */
    @Override
    public void run() {
        try (
            Recording r = new Recording(Configuration.getConfiguration("profile"));
        ) {
            r.start();

            Path path = Paths.get(jsonFile);

            if (!path.isAbsolute() && !Files.exists(path)) {
                path = Paths.get("data").resolve(jsonFile);
            }
            
            String outputInfo = aggregateJsonUseCase.execute(aggregationName, fieldName, groupFields, path);

            r.stop();
            Path profilerDumpPath = Paths.get("reports/profiling").resolve("latest-test.jfr");
            r.dump(profilerDumpPath);

            System.out.println(String.format("""
                    ===============================
                    Stats-CLI finished successfully
                    -------------------------------
                    %s
                    %s
                    ===============================
                    """, outputInfo, "Profiler JFR: " + profilerDumpPath));
        } catch (Exception e) {
            System.err.println("Error in CLIAdapter: "+ e.getMessage());
        }
    }

    public String getAggregationName() {
        return aggregationName;
    }

    public void setAggregationName(String aggregationName) {
        this.aggregationName = aggregationName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<String> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(List<String> groupFields) {
        this.groupFields = groupFields;
    }

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }

    @Override
    public String toString() {
        return "CommandArgs [aggregationName=" + aggregationName + ", fieldName=" + fieldName + ", groupField="
                + groupFields + ", jsonFile=" + jsonFile + "]";
    } 
}
