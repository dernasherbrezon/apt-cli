package ru.r2cloud.apt.cli.model;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Delete packages from the APT repository")
public class DeleteCommand {

	@Parameter(names = "--packages", description = "Comma separated list of packages to include into the search. Example: invalid1,accidentally-uploaded2")
	private List<String> packages;

	@Parameter(names = "--archs", description = "Comma separated list of architectures to delete")
	private List<String> archs;

	public List<String> getPackages() {
		return packages;
	}

	public void setPackages(List<String> packages) {
		this.packages = packages;
	}

	public List<String> getArchs() {
		return archs;
	}

	public void setArchs(List<String> archs) {
		this.archs = archs;
	}

}
