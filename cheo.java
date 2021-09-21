///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "cheo", mixinStandardHelpOptions = true, version = "cheo 0.1", description = "cheo made with jbang", header = {
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

    @Option(names = { "-w", "--workspace" },
            required = true,
            paramLabel = "WORKSPACE",
            defaultValue = "${env:CHEO_WORKSPACE}", description = "The workspace dir. Defaults to 'env:CHEO_WORKSPACE'")
    private File workspace;

    @Parameters(index = "0", arity = "1", description = "The issue identifier")
    private String issueId;

    @Parameters(index = "1..*", description = "The issue title")
    private List<String> titleParameter;

    public static void main(String... args) {
        int exitCode = new CommandLine(new cheo()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("workspace: " + workspace);
        System.out.println("issueId: " + issueId);
        String title = titleParameter.stream().collect(Collectors.joining(" "));
        System.out.println("title: " + title);

        try {
            validateWorkspaceDir(workspace);
            File monthlyDir = buildMonthlyDir(workspace);
            File issueDir = createIssueDir(monthlyDir, issueId);
        } catch (Exception ex) {
            System.err.println(format("ERROR: %s", ex.getMessage()));
        }

        // -------------------------------------------------------------------------------------------------------------
        // Design: Implementation
        // -------------------------------------------------------------------------------------------------------------
        // 3. Create dir with issue id, with `notes`, T1, T2, Tn... sub dirs (to be
        // defined).
        // 4. Create `issue-id-TASKS.md` file within the `notes` dir, with its
        // respective content.
        // 5. Create `issue-id-Tn.md` files for the base tasks with its respective
        // content.
        // -------------------------------------------------------------------------------------------------------------
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

}
