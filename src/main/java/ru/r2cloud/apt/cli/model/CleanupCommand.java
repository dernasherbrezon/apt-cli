package ru.r2cloud.apt.cli.model;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Cleanup APT repository. Remove unused files")
public class CleanupCommand {

	@Parameter(names = "--keep", description = "Number of files to keep for each file type")
	private int keepLast = 2;

	public int getKeepLast() {
		return keepLast;
	}

	public void setKeepLast(int keepLast) {
		this.keepLast = keepLast;
	}

}
