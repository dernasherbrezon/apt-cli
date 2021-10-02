package ru.r2cloud.apt.cli.model;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Upload one or multiple .deb files to APT repository")
public class SaveCommand {

	@Parameter(names = "--patterns", description = "File patterns to upload. Example: *.deb", required = true)
	private List<String> patterns;

	public List<String> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<String> patterns) {
		this.patterns = patterns;
	}

}
