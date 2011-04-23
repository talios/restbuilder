package com.theoryinpractise.restbuilder.mojo;

import com.theoryinpractise.restbuilder.parser.RestBuilder;
import com.theoryinpractise.restbuilder.parser.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 23/04/11
 * Time: 12:26 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractRestBuilderMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    protected MavenProject project;
    /**
     * @parameter expression="${basedir}/src/main/resources"
     * @required
     */
    protected File resourceDir;

    protected void processResourceFilesInDirectory(File resourceDir, ModelProcessor modelProcessor) throws IOException, MojoExecutionException {

        File[] files = resourceDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                processResourceFilesInDirectory(file, modelProcessor);
            } else {
                if (file.getName().endsWith(".rbuilder")) {
                    getLog().info("Processing resource file " + file.getPath());
                    processResourceFile(file, modelProcessor);
                }
            }
        }

    }

    private void processResourceFile(File file, ModelProcessor modelProcessor) throws IOException, MojoExecutionException {

        RestBuilder restBuilder = new RestBuilder();
        Model model = restBuilder.buildModel(file);
        modelProcessor.process(model);

    }
}
