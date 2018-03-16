package io.github.giovibal.mqtt;

import io.github.giovibal.mqtt.prometheus.PromMetricsExporter;
import io.github.giovibal.mqtt.rest.RestApiVerticle;
import io.github.giovibal.mqtt.security.impl.OAuth2ApifestAuthenticatorVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni Baleani on 13/11/2015.
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        start(args);
    }

    static CommandLine cli(String[] args) {
        CLI cli = CLI.create("java -jar <mqtt-broker>-fat.jar")
                .setSummary("A vert.x MQTT Broker")
                .addOption(new Option()
                        .setLongName("conf")
                        .setShortName("c")
                        .setDescription("vert.x config file (in json format)")
                        .setRequired(true)
                )
                .addOption(new Option()
                        .setLongName("hazelcast-conf")
                        .setShortName("hc")
                        .setDescription("vert.x hazelcast configuration file")
                        .setRequired(false)
                )
                .addOption(new Option()
                        .setLongName("hazelcast-host")
                        .setShortName("hh")
                        .setDescription("vert.x hazelcast ip address of this node (es. -hh 10.0.0.1)")
                        .setRequired(false)
                )
                .addOption(new Option()
                        .setLongName("hazelcast-members")
                        .setShortName("hm")
                        .setDescription("vert.x hazelcast list of tcp-ip members to add (es. -hm 10.0.0.1 10.0.0.2 10.0.0.3)")
                        .setRequired(false)
                        .setMultiValued(true)
                );

        // parsing
        CommandLine commandLine = null;
        try {
            List<String> userCommandLineArguments = Arrays.asList(args);
            commandLine = cli.parse(userCommandLineArguments);
        } catch (CLIException e) {
            // usage
            StringBuilder builder = new StringBuilder();
            cli.usage(builder);
            System.out.println(builder.toString());
//            throw e;
        }
        return commandLine;
    }

    public static void start(String[] args) {
        CommandLine commandLine = cli(args);
        if (commandLine == null) {
            System.exit(-1);
        }

        String confFilePath = commandLine.getOptionValue("c");
        String hazelcastConfFilePath = commandLine.getOptionValue("hc");
        String clusterHost = commandLine.getOptionValue("hh");
        List<String> hazelcastMembers = commandLine.getOptionValues("hm");

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        if (confFilePath != null) {
            try {
                String json = FileUtils.readFileToString(new File(confFilePath), "UTF-8");
                JsonObject config = new JsonObject(json);
                deploymentOptions.setConfig(config);
            } catch (IOException e) {
                logger.fatal(e.getMessage(), e);
            }
        }


        VertxOptions options = new VertxOptions();
        options.setMetricsOptions(new DropwizardMetricsOptions()
                .setEnabled(true)
                .setJmxEnabled(true)
        );
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(MQTTBroker.class.getName(), deploymentOptions);
        //vertx.deployVerticle(RestApiVerticle.class.getName(), deploymentOptions);
        //vertx.deployVerticle(PromMetricsExporter.class.getName(), deploymentOptions);
        //vertx.deployVerticle(OAuth2ApifestAuthenticatorVerticle.class.getName(),deploymentOptions);
    }


    public static void stop(String[] args) {
        System.exit(0);
    }

}
