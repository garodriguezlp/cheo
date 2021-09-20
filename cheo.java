///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0

import static java.lang.String.format;

import java.io.File;
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

        validateWorkspaceDir(workspace);

        // -------------------------------------------------------------------------------------------------------------
        // Design: Implementation
        // -------------------------------------------------------------------------------------------------------------
        // 1. Verify working dir.
        // 2. Based on current date, check if folder structure exist, otherwise, create it.
        //      e.g.: 2021.09.19 --> working-dir/2021/2021.09/
        // 3. Create dir with issue id, with `notes`, T1, T2, Tn... sub dirs (to be defined).
        // 4. Create `issue-id-TASKS.md` file within the `notes` dir, with its respective content.
        // 5. Create `issue-id-Tn.md` files for the base tasks with its respective content.
        // -------------------------------------------------------------------------------------------------------------
        return 0;
    }

    private void validateWorkspaceDir(File ws) {
        if (!(ws.exists() && ws.isDirectory() && ws.canWrite())) {
            System.err.println(format("ERROR: Workspace path '%s' is invalid", ws));
        }
    }
}
