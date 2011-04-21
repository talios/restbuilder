package com.theoryinpractise.restbuilder.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import com.sun.codemodel.internal.JCodeModel;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.parser.RestBuilder;
import com.theoryinpractise.restbuilder.parser.model.RestModel;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate
 * @phase generate-sources
 */
public class RestBuilderMojo extends AbstractMojo {


    /**
     * @parameter expression="${basedir}/src/main/resources"
     * @required
     */
    private File resourceDir;

    /**
     * Location of the generated source files.
     *
     * @parameter default-value="${project.build.outputDirectory}/../generated-sources"
     * @required
     */
    private File generatedSourceDirectory;


    public void execute() throws MojoExecutionException {


        try {
            processResourceFilesInDirectory(resourceDir);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }


        //

    }

    private void processResourceFilesInDirectory(File resourceDir) throws IOException {

        File[] files = resourceDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                processResourceFilesInDirectory(file);
            } else {
                if (file.getName().endsWith(".rbuilder")) {
                    getLog().info("Processing resource file " + file.getPath());
                    processResourceFile(file);
                }
            }
        }

    }

    private void processResourceFile(File file) throws IOException {

        RestBuilder restBuilder = new RestBuilder();

        RestModel model = restBuilder.buildModel(file);


        JCodeModel jCodeModel = new JCodeModel();

        CodeGenerator codeGenerator = null;

        generatedSourceDirectory.mkdirs();

        codeGenerator.generate(jCodeModel, model, generatedSourceDirectory);


//        getLog().info("Found resource in package: " + model.getPackage());
//        for (RestResource resource : model.getResources()) {
//            getLog().info(" - " + resource.getName());
//        }


    }


}
