package undomanager.command.impl;

import undomanager.command.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Groups multiple commands so they execute/undo together as a single step.
 * Useful for things like "Replace All" (many ReplaceTextCommands) or
 * "Clear Formatting" (several FormatCommands) where one Ctrl+Z should
 * reverse the whole action, not just the last piece of it.
 */
public class CompositeCommand implements Command {

    private final List<Command> commands = new ArrayList<>();
    private final String description;

    public CompositeCommand(String description, Command... commands) {
        this.description = description;
        this.commands.addAll(Arrays.asList(commands));
    }

    public void add(Command command) {
        commands.add(command);
    }

    @Override
    public void execute() {
        for (Command c : commands) {
            c.execute();
        }
    }

    @Override
    public void undo() {
        // Reverse order: last thing done must be first thing undone.
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String getDescription() {
        return description;
    }
}
