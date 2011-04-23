package com.theoryinpractise.restbuilder.mojo;

import com.theoryinpractise.restbuilder.parser.model.Model;
import org.apache.maven.plugin.MojoExecutionException;

public interface ModelProcessor {
    void process(Model model) throws MojoExecutionException;
}
