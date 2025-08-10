package com.volgoblob.cmd;


import com.volgoblob.internal.adapters.CliAdapter;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CliAdapter(null)).execute(args);
        System.exit(exitCode);
    }
}