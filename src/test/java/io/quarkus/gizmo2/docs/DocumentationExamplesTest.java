package io.quarkus.gizmo2.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

/**
 * To be compiled, executed and verified by this test, examples in {@code MANUAL.adoc}
 * must be enclosed by lines {@code //TEST:BEGIN} and {@code //TEST:END}. Everything
 * described below must be within that enclosure. All classes in the {@code MANUAL.adoc}
 * together with all the generated classes must have unique names.
 * <p>
 * The example code that should be compiled must be enclosed by {@code ----}. There must
 * be exactly two such lines in the example. This is classic AsciiDoc code listing.
 * <p>
 * By default, the example is compiled and executed. If it is supposed to be compiled only,
 * the example must include a line {@code //TEST:COMPILE-ONLY}. This should be exceptional,
 * a vast majority of examples should be executable.
 * <p>
 * If executing the example prints an output that should be verified, the example must
 * include exactly one line {@code //TEST:OUTPUT}, followed by an arbitrary number of output
 * expectation lines (see below).
 * <p>
 * If executing the example produces a class (as majority of examples do) that contains
 * a classic Java {@code public static void main} method which prints an output that should
 * be verified, the example must include at least one (but potentially multiple) line
 * {@code //TEST:RUN <binary class name> <arg1> <arg2> ... <argN>}, followed by an arbitrary number
 * of output expectation lines (see below).
 * <p>
 * <b>Output expectation line</b>
 * <p>
 * Each output expectation line must start with {@code //=} for an exact string match
 * or with {@code //~} for a regexp match. The order of output expectation lines must be
 * the same as the actual output order. The number of output expectation lines must be
 * the same as the number of lines in the actual output (after trimming).
 */
public class DocumentationExamplesTest {
    // the code below is a mess, but works :-)

    private static final Pattern TEST_PATTERN = Pattern.compile("//TEST:BEGIN(.*?)----(.*?)----(.*?)//TEST:END",
            Pattern.DOTALL);
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("class ([a-zA-Z0-9_]+) ");

    @Test
    public void compileAndRunDocumentationExamples() throws Throwable {
        Path path = Paths.get("MANUAL.adoc");
        assumeTrue(Files.exists(path));
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assumeTrue(javac != null);

        String manual = Files.readString(path);
        Matcher matcher = TEST_PATTERN.matcher(manual);
        while (matcher.find()) {
            String match = matcher.group();

            boolean compileOnly = match.contains("//TEST:COMPILE-ONLY");

            String source = matcher.group(2);
            Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(source);
            assertTrue(classNameMatcher.find());
            String className = classNameMatcher.group(1);

            System.out.println(className);

            ExpectedOutput expectedOutput = null;
            List<GeneratedClassRun> generatedClassRuns = new ArrayList<>();
            ListIterator<String> it = match.lines().toList().listIterator();
            while (it.hasNext()) {
                String line = it.next();
                if (line.startsWith("//TEST:OUTPUT")) {
                    expectedOutput = parseExpectedOutput(it);
                } else if (line.startsWith("//TEST:RUN")) {
                    String[] split = line.split("\\s+");
                    assertEquals("//TEST:RUN", split[0]);
                    assertTrue(split.length > 1);
                    String cls = split[1];
                    List<String> args = new ArrayList<>();
                    for (int i = 2; i < split.length; i++) {
                        args.add(split[i]);
                    }
                    generatedClassRuns.add(new GeneratedClassRun(cls, args, parseExpectedOutput(it)));
                }
            }

            List<String> output = compileAndRun(javac, source, className, compileOnly);
            if (compileOnly) {
                continue;
            }
            if (expectedOutput != null) {
                expectedOutput.verify(output);
            }

            for (GeneratedClassRun generatedClassRun : generatedClassRuns) {
                generatedClassRun.verify();
            }
        }
    }

    static ExpectedOutput parseExpectedOutput(ListIterator<String> lines) {
        List<String> result = new ArrayList<>();
        while (lines.hasNext()) {
            String line = lines.next();
            if (line.startsWith("//=") || line.startsWith("//~")) {
                result.add(line);
            } else {
                break;
            }
        }
        lines.previous();
        return new ExpectedOutput(result);
    }

    static List<String> compileAndRun(JavaCompiler javac, String source, String className, boolean compileOnly)
            throws Throwable {
        List<String> options = List.of("-cp", "target/classes", "-d", "target/doc-example");
        JavaFileObject compilationUnit = new StringJavaSource(className, source
                .replace("Path.of(\"output\")", "Path.of(\"target/doc-example\")"));
        assertTrue(javac.getTask(null, null, null, options, null, List.of(compilationUnit)).call());

        if (compileOnly) {
            return List.of();
        }

        return execute(className, new String[0]);
    }

    static List<String> execute(String className, String[] args) throws Throwable {
        String output = captureStdOut(() -> {
            try (URLClassLoader cl = new ExampleClassLoader()) {
                Class<?> example = cl.loadClass(className);
                example.getMethod("main", String[].class).invoke(null, (Object) args);
            }
        });

        List<String> result = new ArrayList<>();
        for (String line : output.trim().split("\n")) {
            result.add(line.trim());
        }
        return result;
    }

    static class ExpectedOutput {
        final List<String> lines;

        ExpectedOutput(List<String> lines) {
            this.lines = lines;
        }

        void verify(List<String> actualOutput) {
            if (actualOutput.size() == 1 && "".equals(actualOutput.get(0))) {
                actualOutput = List.of();
            }
            if (lines.size() != actualOutput.size()) {
                fail("Output has " + actualOutput.size() + " lines, but expected " + lines.size());
            }
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Pattern pattern;
                String kind;
                if (line.startsWith("//=")) {
                    pattern = Pattern.compile(Pattern.quote(line.substring(3).trim()));
                    kind = "exact";
                } else if (line.startsWith("//~")) {
                    pattern = Pattern.compile(line.substring(3).trim());
                    kind = "pattern";
                } else {
                    throw new IllegalArgumentException("Invalid output expectation line: " + line);
                }
                String outputLine = actualOutput.get(i).trim();
                Matcher matcher = pattern.matcher(outputLine);
                if (!matcher.matches()) {
                    fail("Output line " + (i + 1) + " does not match! Expectation kind: " + kind
                            + "\nexpect: " + line.substring(3).trim()
                            + "\nactual: " + outputLine);
                }
            }
        }
    }

    static class GeneratedClassRun {
        final String className;
        final List<String> args;
        final ExpectedOutput expectedOutput;

        GeneratedClassRun(String className, List<String> args, ExpectedOutput expectedOutput) {
            this.className = className;
            this.args = args;
            this.expectedOutput = expectedOutput;
        }

        void verify() throws Throwable {
            List<String> output = execute(className, args.toArray(String[]::new));
            expectedOutput.verify(output);
        }
    }

    static class StringJavaSource extends SimpleJavaFileObject {
        private final String source;

        StringJavaSource(String name, String source) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }

    static class ExampleClassLoader extends URLClassLoader {
        public ExampleClassLoader() throws MalformedURLException {
            super(new URL[] { Path.of("target/doc-example").toUri().toURL() });
        }
    }

    @FunctionalInterface
    interface Action {
        void run() throws Throwable;
    }

    static String captureStdOut(Action action) throws Throwable {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(result));
        try {
            action.run();
        } finally {
            System.setOut(old);
        }
        return result.toString();
    }
}
