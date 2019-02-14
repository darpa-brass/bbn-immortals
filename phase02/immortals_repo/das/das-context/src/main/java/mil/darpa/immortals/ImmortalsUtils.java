package mil.darpa.immortals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetailsList;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.HashMap;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by awellman@bbn.com on 1/11/18.
 */
public class ImmortalsUtils {
    private static final HashMap<String, NetworkLogger> loggerMap = new HashMap<>();

    public static final Gson gson;
    public static final Gson nonHtmlEscapingGson;

    static {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.registerTypeAdapter(TestDetailsList.class, new TestDetailsList.TestDetailsListDeserializer())
                .registerTypeAdapter(TestDetailsList.class, new TestDetailsList.TestDetailsListSerializer())
                .registerTypeAdapter(AdaptationDetailsList.class, new AdaptationDetailsList.AdaptationDetailsListDeserializer())
                .registerTypeAdapter(AdaptationDetailsList.class, new AdaptationDetailsList.AdaptationDetailsListSerializer())
                .setPrettyPrinting().create();

        builder = new GsonBuilder();
        nonHtmlEscapingGson = builder.registerTypeAdapter(TestDetailsList.class, new TestDetailsList.TestDetailsListDeserializer())
                .registerTypeAdapter(TestDetailsList.class, new TestDetailsList.TestDetailsListSerializer())
                .registerTypeAdapter(AdaptationDetailsList.class, new AdaptationDetailsList.AdaptationDetailsListDeserializer())
                .registerTypeAdapter(AdaptationDetailsList.class, new AdaptationDetailsList.AdaptationDetailsListSerializer())
                .setPrettyPrinting().disableHtmlEscaping().create();
    }

    public static synchronized NetworkLogger getNetworkLogger(@Nonnull String localIdentifier, @Nullable String remoteIdentifier) {
        String identifier = localIdentifier + (remoteIdentifier == null ? "" : "-" + remoteIdentifier);
        NetworkLogger logger = loggerMap.get(identifier);
        if (logger == null) {
            logger = new NetworkLogger(localIdentifier, remoteIdentifier);
            loggerMap.put(identifier, logger);
        }
        return logger;
    }

    public static class NetworkLogger {

        private final Logger logger;
        private final String sendingTemplate;
        private final String receivedAckToSendTemplate;
        private final String receivedTemplate;
        private final String sendingAckToReceivedTemplate;


        public NetworkLogger(@Nonnull String localIdentifier, @Nullable String remoteIdentifier) {
            sendingTemplate = localIdentifier + " sending %s %s to " + remoteIdentifier + " with %s";
            receivedAckToSendTemplate = localIdentifier + " received ACK %s from sent %s %s to " + remoteIdentifier + " with %s";
            receivedTemplate = localIdentifier + " received %s %s with %s";
            sendingAckToReceivedTemplate = localIdentifier + " sending ACK to received %s %s with %s";

            if (ImmortalsConfig.getInstance().debug.isLogNetworkActivityToSeparateFile()) {
                String loggerIdentifier =
                        "network_" + localIdentifier + (remoteIdentifier == null ? "" : "-" + remoteIdentifier);

                LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                PatternLayoutEncoder ple = new PatternLayoutEncoder();
                ple.setPattern("[%date] %msg%n");
                ple.setContext(lc);
                ple.start();
                FileAppender<ILoggingEvent> fa = new FileAppender<>();
                fa.setFile(ImmortalsConfig.getInstance().globals.getGlobalLogDirectory().resolve(
                        loggerIdentifier + ".log").toAbsolutePath().toString());
                fa.setEncoder(ple);
                fa.setContext(lc);
                fa.start();

                ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerIdentifier);
                logger.addAppender(fa);
                logger.setLevel(Level.ALL);
                logger.setAdditive(false);
                this.logger = logger;

            } else {
                logger = LoggerFactory.getLogger(NetworkLogger.class);
            }
        }

        private String getBody(@Nullable Object body) {
            return (body == null ? "no BODY" : (logger.isTraceEnabled() ? ("BODY:\n" + gson.toJson(body, body.getClass())) : ("BODY")));
        }

        public synchronized void logPostSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingTemplate, "POST", path, getBody(body)));
        }

        public synchronized void logPostSendingAckReceived(@Nonnull String path, int status, @Nullable Object body) {
            logger.debug(String.format(receivedAckToSendTemplate, Integer.toString(status), "POST", path, getBody(body)));

        }

        public synchronized void logPostReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedTemplate, "POST", path, getBody(body)));
        }

        public synchronized void logPostReceivedAckSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingAckToReceivedTemplate, "POST", path, getBody(body)));
        }

        public synchronized void logGetSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingTemplate, "GET", path, getBody(body)));
        }

        public synchronized void logGetSendingAckReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedAckToSendTemplate, "GET", path, getBody(body)));

        }

        public synchronized void logGetReceived(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(receivedTemplate, "GET", path, getBody(body)));
        }

        public synchronized void logGetReceivedAckSending(@Nonnull String path, @Nullable Object body) {
            logger.debug(String.format(sendingAckToReceivedTemplate, "GET", path, getBody(body)));
        }
    }

    /*
     * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions
     * are met:
     *
     *   - Redistributions of source code must retain the above copyright
     *     notice, this list of conditions and the following disclaimer.
     *
     *   - Redistributions in binary form must reproduce the above copyright
     *     notice, this list of conditions and the following disclaimer in the
     *     documentation and/or other materials provided with the distribution.
     *
     *   - Neither the name of Oracle nor the names of its
     *     contributors may be used to endorse or promote products derived
     *     from this software without specific prior written permission.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
     * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
     * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
     * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
     * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
     * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
     * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
     * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
     * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
     * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */

    /**
     * Sample code that copies files in a similar manner to the cp(1) program.
     */

    public static class Copy {

        /**
         * Returns {@code true} if okay to overwrite a  file ("cp -i")
         */
        static boolean okayToOverwrite(Path file) {
            String answer = System.console().readLine("overwrite %s (yes/no)? ", file);
            return (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
        }

        /**
         * Copy source file to target location. If {@code prompt} is true then
         * prompt user to overwrite target if it exists. The {@code preserve}
         * parameter determines if file attributes should be copied/preserved.
         */
        static void copyFile(Path source, Path target, boolean prompt, boolean preserve) {
            CopyOption[] options = (preserve) ?
                    new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING} :
                    new CopyOption[]{REPLACE_EXISTING};
            if (!prompt || Files.notExists(target) || okayToOverwrite(target)) {
                try {
                    Files.copy(source, target, options);
                } catch (IOException x) {
                    System.err.format("Unable to copy: %s: %s%n", source, x);
                }
            }
        }

        /**
         * A {@code FileVisitor} that copies a file-tree ("cp -r")
         */
        static class TreeCopier implements FileVisitor<Path> {
            private final Path source;
            private final Path target;
            private final boolean prompt;
            private final boolean preserve;

            TreeCopier(Path source, Path target, boolean prompt, boolean preserve) {
                this.source = source;
                this.target = target;
                this.prompt = prompt;
                this.preserve = preserve;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // before visiting entries in a directory we copy the directory
                // (okay if directory already exists).
                CopyOption[] options = (preserve) ?
                        new CopyOption[]{COPY_ATTRIBUTES} : new CopyOption[0];

                Path newdir = target.resolve(source.relativize(dir));
                try {
                    Files.copy(dir, newdir, options);
                } catch (FileAlreadyExistsException x) {
                    // ignore
                } catch (IOException x) {
                    System.err.format("Unable to create: %s: %s%n", newdir, x);
                    return SKIP_SUBTREE;
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                copyFile(file, target.resolve(source.relativize(file)),
                        prompt, preserve);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                // fix up modification time of directory when done
                if (exc == null && preserve) {
                    Path newdir = target.resolve(source.relativize(dir));
                    try {
                        FileTime time = Files.getLastModifiedTime(dir);
                        Files.setLastModifiedTime(newdir, time);
                    } catch (IOException x) {
                        System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
                    }
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                if (exc instanceof FileSystemLoopException) {
                    System.err.println("cycle detected: " + file);
                } else {
                    System.err.format("Unable to copy: %s: %s%n", file, exc);
                }
                return CONTINUE;
            }
        }

        static void usage() {
            System.err.println("java Copy [-ip] source... target");
            System.err.println("java Copy -r [-ip] source-dir... target");
            System.exit(-1);
        }

        public synchronized static void copyDir(Path source, Path dest) throws IOException {
            // follow links when copying files
            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            TreeCopier tc = new TreeCopier(source, dest, true, true);
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
        }
    }

}
