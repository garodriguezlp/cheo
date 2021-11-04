///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0

import static java.lang.String.format;
import static java.nio.file.StandardOpenOption.APPEND;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "cheo",
    mixinStandardHelpOptions = true,
    showEndOfOptionsDelimiterInUsageHelp = true,
    version = "cheo 0.1",
    description = "cheo made with jbang",
    header = {
        "",
        "                   hhhhhhh",
        "                   h:::::h",
        "                   h:::::h",
        "                   h:::::h",
        "    cccccccccccccccch::::h hhhhh           eeeeeeeeeeee       ooooooooooo",
        "  cc:::::::::::::::ch::::hh:::::hhh      ee::::::::::::ee   oo:::::::::::oo",
        " c:::::::::::::::::ch::::::::::::::hh   e::::::eeeee:::::eeo:::::::::::::::o",
        "c:::::::cccccc:::::ch:::::::hhh::::::h e::::::e     e:::::eo:::::ooooo:::::o",
        "c::::::c     ccccccch::::::h   h::::::he:::::::eeeee::::::eo::::o     o::::o",
        "c:::::c             h:::::h     h:::::he:::::::::::::::::e o::::o     o::::o",
        "c:::::c             h:::::h     h:::::he::::::eeeeeeeeeee  o::::o     o::::o",
        "c::::::c     ccccccch:::::h     h:::::he:::::::e           o::::o     o::::o",
        "c:::::::cccccc:::::ch:::::h     h:::::he::::::::e          o:::::ooooo:::::o",
        " c:::::::::::::::::ch:::::h     h:::::h e::::::::eeeeeeee  o:::::::::::::::o",
        "  cc:::::::::::::::ch:::::h     h:::::h  ee:::::::::::::e   oo:::::::::::oo",
        "    cccccccccccccccchhhhhhh     hhhhhhh    eeeeeeeeeeeeee     ooooooooooo",
        ""
})
class cheo implements Callable<Integer> {

    private static List<String> HEADER = List.of(
        "",
        "                   hhhhhhh",
        "                   h:::::h",
        "                   h:::::h",
        "                   h:::::h",
        "    cccccccccccccccch::::h hhhhh           eeeeeeeeeeee       ooooooooooo",
        "  cc:::::::::::::::ch::::hh:::::hhh      ee::::::::::::ee   oo:::::::::::oo",
        " c:::::::::::::::::ch::::::::::::::hh   e::::::eeeee:::::eeo:::::::::::::::o",
        "c:::::::cccccc:::::ch:::::::hhh::::::h e::::::e     e:::::eo:::::ooooo:::::o",
        "c::::::c     ccccccch::::::h   h::::::he:::::::eeeee::::::eo::::o     o::::o",
        "c:::::c             h:::::h     h:::::he:::::::::::::::::e o::::o     o::::o",
        "c:::::c             h:::::h     h:::::he::::::eeeeeeeeeee  o::::o     o::::o",
        "c::::::c     ccccccch:::::h     h:::::he:::::::e           o::::o     o::::o",
        "c:::::::cccccc:::::ch:::::h     h:::::he::::::::e          o:::::ooooo:::::o",
        " c:::::::::::::::::ch:::::h     h:::::h e::::::::eeeeeeee  o:::::::::::::::o",
        "  cc:::::::::::::::ch:::::h     h:::::h  ee:::::::::::::e   oo:::::::::::oo",
        "    cccccccccccccccchhhhhhh     hhhhhhh    eeeeeeeeeeeeee     ooooooooooo",
        ""
    );
    private static List<String> DEFAULT_TASKS = List.of("Plan & Design", "Dev", "Test", "Integrate", "Release", "Deploy", "Retro");

    @Option(names = { "-w", "--workspace" },
            required = true,
            paramLabel = "WORKSPACE",
            defaultValue = "${env:CHEO_WORKSPACE}",
            description = "The workspace dir. Defaults to 'env:CHEO_WORKSPACE'")
    private File workspace;

    @Option(names = { "-t", "--tasks" },
            paramLabel = "TASKS",
            arity = "1..*",
            description = "The list of tasks")
    private List<String> tasks;

    @Parameters(index = "0", arity = "1", description = "The issue identifier")
    private String issueId;

    @Parameters(index = "1..*", description = "The issue title")
    private List<String> titleParameter;

    public static void main(String... args) {
        int exitCode = new CommandLine(new cheo())
            .setUsageHelpAutoWidth(true)
            .execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println(String.join(System.lineSeparator(), HEADER));
        System.out.println("");
        System.out.println("--- ----------------------------------------------------------------------------");
        System.out.println("Input parameters:");
        System.out.println("workspace: " + workspace);
        System.out.println("issueId: " + issueId);
        String title = String.join(" ", titleParameter);
        System.out.println("title: " + title);
        List<String> taskNames = tasks == null || tasks.isEmpty() ? DEFAULT_TASKS : tasks;
        System.out.println("tasks: " + String.join(", ", taskNames));
        System.out.println("--- ----------------------------------------------------------------------------");
        System.out.println("");

        try {
            validateWorkspaceDir(workspace);
            File monthlyDir = buildMonthlyDir(workspace);
            File issueDir = createIssueDir(monthlyDir, issueId);
            File notesDir = createNotesDir(issueDir);
            File tasksFile = createTasksFile(notesDir, issueId, title);
            createTasks(issueId, taskNames, notesDir, tasksFile);
        } catch (Exception ex) {
            System.err.println(format("ERROR: %s", ex.getMessage()));
        }
        return 0;
    }

    private void validateWorkspaceDir(File ws) throws FileNotFoundException {
        if (!(ws.exists() && ws.isDirectory() && ws.canWrite())) {
            throw new FileNotFoundException(format("Workspace path '%s' is invalid", ws));
        }
    }

    private File buildMonthlyDir(File ws) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        String monthlyDirName = format("%d.%02d", year, month);
        File monthlyDir = new File(ws, monthlyDirName);

        if (!monthlyDir.exists() && monthlyDir.mkdirs()) {
            System.out.println("Creating monthly dir: " + monthlyDir);
        }

        return monthlyDir;
    }

    private File createIssueDir(File monthlyDir, String id) throws FileSystemException {
        File issueDir = new File(monthlyDir, id);
        if (issueDir.exists()) {
            throw new IllegalArgumentException(format("Issue dir '%s' already exists", issueDir));
        }
        if (!issueDir.mkdirs()) {
            throw new FileSystemException(format("Issue dir '%s' could not be created", issueDir));
        }
        System.out.println(format("Issue dir '%s' was created", issueDir));
        return issueDir;
    }

    private File createNotesDir(File issueDir) throws FileSystemException {
        File notesDir = new File(issueDir, "notes");
        if (!notesDir.mkdirs()) {
            throw new FileSystemException(format("Notes dir '%s' could not be created", notesDir));
        }
        System.out.println(format("Notes dir '%s' was created", notesDir));
        return notesDir;
    }

    private File createTasksFile(File notesDir, String id, String title) throws IOException {
        File tasksFile = new File(notesDir, id + "-TASKS.md");
        Files.writeString(tasksFile.toPath(), format("# %s: %s%n%n", id, title));
        System.out.println(format("Task file '%s' was created", tasksFile));
        return tasksFile;
    }

    private void createTasks(String id, List<String> taskNames, File notesDir, File tasksFile) throws IOException {
        for (int number = 1; number <= taskNames.size(); number++) {
            String task = taskNames.get(number - 1);
            System.out.println(format("Adding task %d: %s", number, task));
            Files.writeString(tasksFile.toPath(), format("- [ ] T%d: %s%n%n", number, task), APPEND);
            File taskFile = new File(notesDir, format("%s-T%d.md", id, number));
            Files.writeString(taskFile.toPath(), format("# T%d: %s%n%n", number, task));
        }
    }

}
