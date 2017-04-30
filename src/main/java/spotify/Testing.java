package spotify;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Testing {

    static Logger log = Logger.getLogger(Testing.class.getName());

    public static void main(String[] args) {
        BasicConfigurator.configure(new ConsoleAppender(
                new PatternLayout("%d{HH:mm:ss.SSS} [%t] %-5p %30.30c - %m%n")));

        log.info("Running spotify test..");
        DockerClient docker = null;
        String containerId = null;
        try {
            docker = DefaultDockerClient.fromEnv().build();
            log.info("Creating busybox container..");
            final ContainerCreation container = docker.createContainer(ContainerConfig.builder().
                    image("busybox:latest").cmd("sleep", "9999999").build());
            containerId = container.id();
            docker.startContainer(containerId);
           log.info("Started busybox container: " + containerId);
        } catch (Exception ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            log.error(errors.toString());
            System.exit(1);
        }

        log.info("Now running exec loop in this container..");
        int count = 1;
        while(true){
            try {
                String[] command = {"sleep", "20"};
                log.info("Starting execCreate.. " + count);
                ExecCreation execCreation = docker.execCreate(containerId, command,
                        DockerClient.ExecCreateParam.attachStdout(), DockerClient.ExecCreateParam.attachStderr());
                log.info("Starting execStart.. " + count);
                LogStream output = docker.execStart(execCreation.id());
                log.info("Starting readFully.. " + count);
                output.readFully();
                log.info("Starting execInspect.. " + count);
                ExecState state = docker.execInspect(execCreation.id());
                log.info("Exit code: " + state.exitCode());
            } catch (Exception ex) {
                log.error(ex + " " + count);
            }
            count++;
        }
    }
}

