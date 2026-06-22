package org.plovdev.pvvacli.handlers;

import org.plovdev.commaidle.commands.Command;
import org.plovdev.commaidle.commands.CommandInfo;
import org.plovdev.commaidle.commands.handlers.CommandHandler;

public class UtilsHandler extends CommandHandler {
    @Command("version")
    void version(CommandInfo info) {
        System.out.println("PVVA-CLI version 1.2; build 2026w7.");
    }

    @Command("help")
    void help(CommandInfo info) {
        StringBuilder commands = new StringBuilder("Available commands:\n\n");
        commands.append("pvva version - print the pvva-cli version.\n");
        commands.append("pvva help - print the list of available commands.\n");
        commands.append("\n");
        commands.append("pvva build - Build the project to .pvva.\n");
        commands.append("pvva info -i={input .pvva} - print the adapter info.\n");
        commands.append("pvva extract -i={input .pvva} -e={entry to extract} - extracts entry from adapter and print.\n");
        commands.append("pvva unpack -i={input .pvva} - unpacks pvva to current or specified dir(use -o={output/dir}.\n");
        commands.append("\n");
        commands.append("pvva keys --init - initiate key pair and print public key. User -re for reinitiate.\n");
        commands.append("pvva keys --public - print public key.\n");

        System.out.println(commands);
    }
}