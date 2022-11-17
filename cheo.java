///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11
//DEPS info.picocli:picocli:4.5.0
//DEPS org.apache.commons:commons-lang3:3.11

import org.apache.commons.lang3.SystemUtils;
import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static java.lang.System.*;
import static java.nio.file.StandardOpenOption.APPEND;

@Command(name = "cheo",
        mixinStandardHelpOptions = true,
        showEndOfOptionsDelimiterInUsageHelp = true,
        version = "cheo 0.1",
        description = "cheo made with jbang",
        defaultValueProvider = PropertiesDefaultProvider.class,
        header = {
                "",
                " .o88b. db   db d88888b  .d88b.",
                "d8P  Y8 88   88 88'     .8P  Y8.",
                "8P      88ooo88 88ooooo 88    88",
                "8b      88~~~88 88~~~~~ 88    88",
                "Y8b  d8 88   88 88.     `8b  d8'",
                " `Y88P' YP   YP Y88888P  `Y88P'",
                ""
        })
class cheo implements Callable<Integer> {

    private static List<String> DEFAULT_TASKS = List.of(
            "Plan & Design: Brainstorming and strategy",
            "Dev: Implementation",
            "Test: Manual verifications",
            "Integrate: Do not break the CI build",
            "Release: Release notes and Jira fix version",
            "Deploy: APP configuration in higher environments",
            "Retro: What we learn");

    @Option(names = {"-w", "--workspace"},
            required = true,
            paramLabel = "WORKSPACE",
            defaultValue = "${cheo.workspace}",
            description = "The workspace dir. Defaults to `workspace` property on '${sys:user.home}${sys:file.separator}.cheo.properties'")
    private File workspace;

    @Option(names = {"-t", "--tasks"},
            paramLabel = "TASKS",
            arity = "1..*",
            description = "The list of tasks")
    private List<String> tasks;

    @Option(names = {"-e", "--editor"},
            required = true,
            paramLabel = "EDITOR",
            defaultValue = "code",
            description = "The text code editor")
    private String editor;

    @Parameters(index = "0", arity = "1", description = "The issue identifier")
    private String issueId;

    @Parameters(index = "1..*", description = "The issue title")
    private List<String> titleParameter;

    @Spec
    private CommandSpec spec;

    public static void main(String... args) {
        int exitCode = new CommandLine(new cheo())
                .setUsageHelpAutoWidth(true)
                .execute(args);
        exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        Arrays.stream(spec.usageMessage().header()).forEach(out::println);

        out.println("");
        out.println("--- ----------------------------------------------------------------------------");
        out.println("Input parameters:");
        out.println("workspace: " + workspace);
        out.println("issueId: " + issueId);
        String title = String.join(" ", titleParameter);
        out.println("title: " + title);
        List<String> taskNames = tasks == null || tasks.isEmpty() ? DEFAULT_TASKS : tasks;
        out.println("tasks:" + lineSeparator() + String.join("," + lineSeparator(), taskNames));
        out.println("--- ----------------------------------------------------------------------------");
        out.println("");

        try {
            validateWorkspaceDir(workspace);
            File monthlyDir = buildMonthlyDir(workspace);
            File issueDir = createIssueDir(monthlyDir, issueId);
            File notesDir = createNotesDir(issueDir);
            File tasksFile = createTasksFile(notesDir, issueId, title);
            createTasks(issueId, taskNames, notesDir, tasksFile);
            openEditor(issueDir.getAbsolutePath());
        } catch (Exception ex) {
            err.println(format("ERROR: %s", ex.getMessage()));
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
            out.println("Creating monthly dir: " + monthlyDir);
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
        out.println(format("Issue dir '%s' was created", issueDir));
        return issueDir;
    }

    private File createNotesDir(File issueDir) throws FileSystemException {
        File notesDir = new File(issueDir, "notes");
        if (!notesDir.mkdirs()) {
            throw new FileSystemException(format("Notes dir '%s' could not be created", notesDir));
        }
        out.println(format("Notes dir '%s' was created", notesDir));
        return notesDir;
    }

    private File createTasksFile(File notesDir, String id, String title) throws IOException {
        File tasksFile = new File(notesDir, id + "-TASKS.md");
        Files.writeString(tasksFile.toPath(), format("# %s: %s%n%n", id, title));
        out.println(format("Task file '%s' was created", tasksFile));
        return tasksFile;
    }

    private void createTasks(String id, List<String> taskNames, File notesDir, File tasksFile)
            throws IOException {
        for (int number = 1; number <= taskNames.size(); number++) {
            String task = taskNames.get(number - 1);
            out.println(format("Adding task %d: %s", number, task));
            Files.writeString(tasksFile.toPath(), format("- [ ] T%d: %s%n%n", number, task), APPEND);
            File taskFile = new File(notesDir, format("%s-T%d.md", id, number));
            Files.writeString(taskFile.toPath(), format("# T%d: %s%n%n", number, task));
        }
    }

    private void openEditor(final String path) throws IOException {
        String filePath = path.contains(" ") ? format("\"%s\"", path) : path;
        String editorCmd = String.join(" ", editor, filePath);

        List<String> cmd = SystemUtils.IS_OS_UNIX ?
                List.of("sh", "-c", editorCmd) :
                List.of("cmd", "/c", editorCmd);

        out.println("Running `" + String.join(" ", cmd) + "`");

        new ProcessBuilder(cmd).start();
    }
}
