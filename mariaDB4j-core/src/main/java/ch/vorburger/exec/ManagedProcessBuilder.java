/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2014 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.exec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.exec.util.StringUtils;

/**
 * Builder for ManagedProcess.
 * 
 * <p>This is inspired by {@link java.lang.ProcessBuilder} &amp;
 * {@link org.apache.commons.exec.CommandLine}, and/but:
 * 
 * <p>It offers to add java.io.File arguments, and makes sure that their absolute path is used.
 * 
 * <p>If no directory is set, it automatically sets the initial working directory using the directory
 * of executable if it was a File, and thus makes sure an initial working directory is always passed
 * to the process.
 * 
 * <p>It intentionally doesn't offer "parsing" space delimited command "lines", but forces you to set
 * an executable and add arguments.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessBuilder {

    protected final CommandLine commonsExecCommandLine;
    protected final Map<String, String> environment;
    protected File directory;
    protected InputStream inputStream;
    protected boolean destroyOnShutdown = true;
    protected int consoleBufferMaxLines = 100;
    protected OutputStreamLogDispatcher outputStreamLogDispatcher = new OutputStreamLogDispatcher();

    public ManagedProcessBuilder(String executable) throws ManagedProcessException {
        commonsExecCommandLine = new CommandLine(executable);
        this.environment = initialEnvironment();
    }

    public ManagedProcessBuilder(File executable) throws ManagedProcessException {
        commonsExecCommandLine = new CommandLine(executable);
        this.environment = initialEnvironment();
    }

    protected Map<String, String> initialEnvironment() throws ManagedProcessException {
        try {
            return EnvironmentUtils.getProcEnvironment();
        } catch (IOException e) {
            throw new ManagedProcessException("Retrieving default environment variables failed", e);
        }
    }

    public ManagedProcessBuilder addArgument(String arg, boolean handleQuoting) {
        commonsExecCommandLine.addArgument(arg, handleQuoting);
        return this;
    }

    /**
     * Adds a File as a argument to the command. This uses {@link File#getCanonicalPath()}, which is
     * usually what you'll actually want when launching external processes.
     * 
     * @param arg the File to add
     * @return this
     * @throws IOException if File getCanonicalPath() fails
     * @see ProcessBuilder
     */
    public ManagedProcessBuilder addArgument(File arg) throws IOException {
        addArgument(arg.getCanonicalPath(), true);
        return this;
    }

    /**
     * Adds an argument to the command.
     * 
     * @param arg the String Argument to add. It will be escaped with single or double quote if it contains a space.
     * @return this
     * @see ProcessBuilder
     */
    public ManagedProcessBuilder addArgument(String arg) {
        addArgument(arg, true);
        return this;
    }

    /**
     * Adds a single argument to the command, composed of two parts.
     * The two parts are independently escaped (see above), and then concatenated, without separator.
     */
    public ManagedProcessBuilder addArgument(String argPart1, String argPart2) {
        addArgument(argPart1, "", argPart2); // No separator
        return this;
    }

    /**
     * Adds a single argument to the command, composed of two parts and a given separator.
     * The two parts are independently escaped (see above), and then concatenated using the separator.
     */
    protected ManagedProcessBuilder addArgument(String argPart1, String separator, String argPart2) {
        // @see MariaDB4j Issue #30 why 'quoting' (https://github.com/vorburger/MariaDB4j/issues/30)
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.quoteArgument(argPart1));
        sb.append(separator);
        sb.append(StringUtils.quoteArgument(argPart2));
        // @see https://issues.apache.org/jira/browse/EXEC-93 why we have to use 'false' here
        // TODO Remove the false when commons-exec has a release including EXEC-93 fixed.
        addArgument(sb.toString(), false);
        return this;
    }

    /**
     * Adds a single argument to the command, composed of a prefix, separated by a '=', followed by a file path.
     * The prefix and file path are independently escaped (see above), and then concatenated.
     */
    public ManagedProcessBuilder addFileArgument(String arg, File file) throws IOException {
        return addArgument(arg, "=", file.getCanonicalPath());
    }

    public String[] getArguments() {
        return commonsExecCommandLine.getArguments();
    }

    /**
     * Sets working directory.
     * 
     * @param directory working directory to use for process to be launched
     * @return this
     * @see ProcessBuilder#directory(File)
     */
    public ManagedProcessBuilder setWorkingDirectory(File directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Get working directory used for process to be launched.
     * 
     * @see ProcessBuilder#directory()
     */
    public File getWorkingDirectory() {
        return this.directory;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public String getExecutable() {
        return commonsExecCommandLine.getExecutable();
    }

    public boolean isDestroyOnShutdown() {
        return destroyOnShutdown;
    }

    public ManagedProcessBuilder setDestroyOnShutdown(boolean flag) {
        this.destroyOnShutdown = flag;
        return this;
    }

    public void setConsoleBufferMaxLines(int consoleBufferMaxLines) {
        this.consoleBufferMaxLines = consoleBufferMaxLines;
    }

    public int getConsoleBufferMaxLines() {
        return consoleBufferMaxLines;
    }

    public void setOutputStreamLogDispatcher(OutputStreamLogDispatcher outputStreamLogDispatcher) {
        this.outputStreamLogDispatcher = outputStreamLogDispatcher;
    }

    public OutputStreamLogDispatcher getOutputStreamLogDispatcher() {
        return outputStreamLogDispatcher;
    }

    // ----

    public ManagedProcess build() {
        return new ManagedProcess(getCommandLine(), directory, environment, inputStream, destroyOnShutdown, consoleBufferMaxLines,
                outputStreamLogDispatcher);
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /* package-local... let's keep ch.vorburger.exec's API separate from Apache Commons Exec, so it
     * COULD be replaced */
    CommandLine getCommandLine() {
        if (getWorkingDirectory() == null) {
            if (commonsExecCommandLine.isFile()) {
                File exec = new File(commonsExecCommandLine.getExecutable());
                File dir = exec.getParentFile();
                if (dir == null)
                    throw new IllegalStateException(
                            "directory MUST be set (and could not be auto-determined from executable, although it was a File)");
                this.setWorkingDirectory(dir);
                // DO NOT } else {
                // throw new
                // IllegalStateException("directory MUST be set (and could not be auto-determined from executable)");
            }
        }
        return commonsExecCommandLine;
    }

    /**
     * Intended for debugging / logging, only.
     */
    @Override
    public String toString() {
        return commonsExecCommandLine.toString();
    }

}
