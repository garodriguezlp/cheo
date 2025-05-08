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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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
        usageHelpAutoWidth = true,
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

    private static final String DEFAULT_TASKS =
            "Analyze=Clarify requirements; define scope; investigate context," +
            "Build=Design; code; write tests," +
            "Verify=Test thoroughly; check integration; ensure quality; collaborate with QA," +
            "WrapUp=Validate config; clean resources; update docs; present outcomes";

    @Parameters(index = "0", arity = "1", description = "The issue identifier")
    private String issueId;

    @Option(names = {"-t", "--tasks"},
            paramLabel = "TASKS",
            arity = "1..*",
            description = "The list of tasks",
            split = ",",
            defaultValue = DEFAULT_TASKS)
    private LinkedHashMap<String, String> tasks;

    @Parameters(index = "1..*",
            arity = "1..*",
            description = "The issue title")
    private List<String> title;

    @Option(names = {"-w", "--workspace"},
            required = true,
            paramLabel = "WORKSPACE",
            defaultValue = "${cheo.workspace}",
            description = "The workspace dir. Defaults to `workspace` property on '${sys:user.home}${sys:file.separator}.cheo.properties'")
    private File workspace;

    @Option(names = {"-e", "--editor"},
            required = true,
            paramLabel = "EDITOR",
            defaultValue = "code",
            description = "The text code editor")
    private String editor;

    @Spec
    private CommandSpec spec;

    public static void main(String... args) {
        exit(new CommandLine(new cheo()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        Arrays.stream(spec.usageMessage().header()).forEach(out::println);

        out.println("");
        out.println("--- ----------------------------------------------------------------------------");
        out.println("Input parameters:");
        out.println("workspace: " + workspace);
        out.println("issueId: " + issueId);
        String joinedTitle = String.join(" ", title);
        out.println("title: " + joinedTitle);
        out.println("tasks:");
        tasks.forEach((k, v) -> out.printf("\t- %s: %s%n", k, v));
        out.println("--- ----------------------------------------------------------------------------");
        out.println("");

        try {
            validateWorkspaceDir(workspace);
            File monthlyDir = buildMonthlyDir(workspace);
            File issueDir = createIssueDir(monthlyDir, issueId);
            File tasksFile = createMainFile(issueDir, issueId, joinedTitle);
            createTasks(issueId, tasks, issueDir, tasksFile);
            openEditor(issueDir.getAbsolutePath());
        } catch (Exception ex) {
            err.printf("ERROR: %s%n", ex.getMessage());
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
        out.printf("Issue dir '%s' was created%n", issueDir);
        return issueDir;
    }

    private File createMainFile(File notesDir, String id, String title) throws IOException {
        File mainFile = new File(notesDir, id + "-Main.md");
        Files.writeString(mainFile.toPath(), format("# %s: %s%n%n", id, title));
        out.printf("Main file '%s' was created%n", mainFile);
        return mainFile;
    }

    private void createTasks(String issueId,
                             LinkedHashMap<String, String> tasks,
                             File issueDir,
                             File mainFile) throws IOException {

        int counter = 0;
        for (Entry<String, String> entry : tasks.entrySet()) {
            String key = entry.getKey();
            String description = entry.getValue();
            out.printf("Adding task %s: %s%n", key, description);
            String filePath = format("%02d-%s/%s-%s.md", counter, key, issueId, key);
            Files.writeString(mainFile.toPath(), format("- [%s](%s): %s%n", key, filePath, description), APPEND);
            File taskFile = new File(issueDir, filePath);
            Files.createDirectories(taskFile.getParentFile().toPath());
            Files.writeString(taskFile.toPath(), format("# %s: %s%n%n", key, description));
            counter++;
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
