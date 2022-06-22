package ru.r2cloud.apt.cli.model;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(commandDescription = "Init empty repository")
public class InitCommand {

    @Parameter(names = "--archs", description = "Comma separated list of architectures to init", required = true)
    private List<String> archs;

    public List<String> getArchs() {
        return archs;
    }

    public void setArchs(List<String> archs) {
        this.archs = archs;
    }
}
