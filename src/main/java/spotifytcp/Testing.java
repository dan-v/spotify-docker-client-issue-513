package spotifytcp;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class Testing {

    public static void main(String[] args) {
        System.out.println("Running spotify test..");
        DockerClient docker = null;
        String containerId = null;
        try {
            docker = new DefaultDockerClient("http://localhost:2375");
            final ContainerCreation container = docker.createContainer(ContainerConfig.builder().
                    image("busybox:latest").cmd("sleep", "9999999").build());
            containerId = container.id();
            docker.startContainer(containerId);
            System.out.println("Started busybox container: " + containerId);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        System.out.println("Now running exec loop in this container..");
        while(true){
            try {
                String[] command = {"sleep", "5"};
                ExecCreation execCreation = docker.execCreate(containerId, command,
                        DockerClient.ExecCreateParam.attachStdout(), DockerClient.ExecCreateParam.attachStderr());
                final LogStream output = docker.execStart(execCreation.id());
                output.readFully();
                final ExecState state = docker.execInspect(execCreation.id());
                System.out.println("Exit code: " + state.exitCode());
            } catch (Exception ex) {
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                System.out.println(errors.toString());
            }
        }
    }
}

