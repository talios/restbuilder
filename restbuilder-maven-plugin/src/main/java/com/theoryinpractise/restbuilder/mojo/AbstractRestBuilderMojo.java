package com.theoryinpractise.restbuilder.mojo;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.theoryinpractise.restbuilder.parser.RestBuilder;
import com.theoryinpractise.restbuilder.parser.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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

        List<InputSupplier<InputStreamReader>> files = locateResourceFileSuppliers(resourceDir);
        RestBuilder restBuilder = new RestBuilder();
        Model model = restBuilder.buildModel(files);
        modelProcessor.process(model);

    }

    protected List<InputSupplier<InputStreamReader>> locateResourceFileSuppliers(File resourceDir) throws IOException, MojoExecutionException {

        List<InputSupplier<InputStreamReader>> modelFiles = Lists.newArrayList();

        File[] files = resourceDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                modelFiles.addAll(locateResourceFileSuppliers(file));
            } else {
                if (file.getName().endsWith(".rbuilder")) {
                    getLog().info("Processing resource file " + file.getPath());
                    modelFiles.add(Files.newReaderSupplier(file, Charsets.UTF_8));
                }
            }
        }

        return modelFiles;

    }

}
