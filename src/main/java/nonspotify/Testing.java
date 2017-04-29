package nonspotify;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;

public class Testing {

    public static void main(String[] args) {
        System.out.println("Running non spotify test..");
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient docker = DockerClientBuilder.getInstance(config).build();
        CreateContainerResponse container = docker.createContainerCmd("busybox:latest").withCmd("sleep", "9999999").exec();
        docker.startContainerCmd(container.getId()).exec();
        System.out.println("Started busybox container: " + container.getId());

        System.out.println("Now running exec loop in this container..");
        while(true) {
            try {
                String[] command = {"sleep", "5"};
                ExecCreateCmdResponse createExecCmd = docker.execCreateCmd(container.getId()).withAttachStdout(true)
                        .withAttachStderr(true).withCmd(command).exec();
                docker.execStartCmd(createExecCmd.getId()).withDetach(false)
                        .exec(new ExecStartResultCallback(System.out, System.err)).awaitCompletion();
                InspectExecResponse execCmdResponse = docker.inspectExecCmd(createExecCmd.getId()).exec();
                System.out.println("Exit code: " + execCmdResponse.getExitCode());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}

