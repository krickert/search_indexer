package kafka.container.test;

import io.micronaut.configuration.picocli.PicocliRunner;

import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "kafka-container-test", description = "...",
        mixinStandardHelpOptions = true)
public class KafkaContainerTestCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Inject
    SampleKafkaProducer producer;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(KafkaContainerTestCommand.class, args);
    }

    public void run() {
        producer.sendMessage("testing 1 2 3");
        // business logic here
        if (verbose) {
            {
                System.out.println("Hi!");
            }
        }
    }

}
