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

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.codegen.httpserver.HttpHandlerGenerator;
import com.theoryinpractise.restbuilder.codegen.restlet.RestletCodeGenerator;
import com.theoryinpractise.restbuilder.parser.model.Model;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * Generates Restlet classes for the RestBuilder models.
 *
 * @goal generate
 * @phase generate-sources
 */
public class CodeGenerationMojo extends AbstractRestBuilderMojo {

    /**
     * Location of the generated source files.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/restbuilder"
     * @required
     */
    private File generatedSourceDirectory;

    public void execute() throws MojoExecutionException {

        try {

            generatedSourceDirectory.getCanonicalFile().mkdirs();
            project.addCompileSourceRoot(generatedSourceDirectory.getPath());

            final JCodeModel jCodeModel = new JCodeModel();
            final CodeGenerator restletCodeGenerator = new RestletCodeGenerator();
            final CodeGenerator httpServerCodeGenerator = new HttpHandlerGenerator();

            processResourceFilesInDirectory(resourceDir, new ModelProcessor() {
                public void process(Model model) throws MojoExecutionException {
                    try {
                        restletCodeGenerator.generate(jCodeModel, model);
                        httpServerCodeGenerator.generate(jCodeModel, model);
                    } catch (JClassAlreadyExistsException e) {
                        throw new MojoExecutionException(e.getMessage());
                    }
                }
            });

            jCodeModel.build(generatedSourceDirectory.getCanonicalFile());

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }


    }

}
