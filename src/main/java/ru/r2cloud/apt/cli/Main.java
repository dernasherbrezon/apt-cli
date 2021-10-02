package ru.r2cloud.apt.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import ru.r2cloud.apt.cli.model.CleanupCommand;
import ru.r2cloud.apt.cli.model.CommandLineArgs;
import ru.r2cloud.apt.cli.model.DeleteCommand;
import ru.r2cloud.apt.cli.model.SaveCommand;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] argv) throws Exception {
		CleanupCommand cleanup = new CleanupCommand();
		DeleteCommand delete = new DeleteCommand();
		SaveCommand save = new SaveCommand();
		CommandLineArgs args = new CommandLineArgs();
		JCommander parser = JCommander.newBuilder().addObject(args).addCommand("save", save).addCommand("cleanup", cleanup).addCommand("delete", delete).build();
		try {
			parser.parse(argv);
		} catch (ParameterException e) {
			LOG.error(e.getMessage());
			parser.usage();
			System.exit(-1);
		}
		if (args.isHelp()) {
			parser.usage();
			return;
		}

	}

}
