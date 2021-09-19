///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

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

    @Parameters(index = "0", description = "The greeting to print", defaultValue = "World!")
    private String greeting;

    public static void main(String... args) {
        int exitCode = new CommandLine(new cheo()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        System.out.println("Hello " + greeting);
        return 0;
    }
}
