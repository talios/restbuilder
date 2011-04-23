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

import com.theoryinpractise.restbuilder.docbuilder.RestBuilderDocumentor;
import com.theoryinpractise.restbuilder.parser.model.Model;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * Generate documentation for the provided RestBuilder models.
 *
 * @goal documentation
 * @phase generate-sources
 */
public class DocumentationMojo extends AbstractRestBuilderMojo {


    /**
     * Location of the generated source files.
     *
     * @parameter default-value="${project.build.directory}/documentation"
     * @required
     */
    private File documentationDirectory;

    public void execute() throws MojoExecutionException {

        try {

            documentationDirectory.getCanonicalFile().mkdirs();

            final RestBuilderDocumentor restBuilderDocumentor = new RestBuilderDocumentor();

            processResourceFilesInDirectory(resourceDir, new ModelProcessor() {
                public void process(Model model) throws MojoExecutionException {
                    try {
                        restBuilderDocumentor.generateDocumentation(documentationDirectory, model);
                    } catch (IOException e) {
                        throw new MojoExecutionException(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }


    }

}
