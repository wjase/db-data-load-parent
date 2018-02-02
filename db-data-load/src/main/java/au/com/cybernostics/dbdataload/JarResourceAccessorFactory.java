/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.com.cybernostics.dbdataload;

/*-
 * #%L
 * db-data-load
 * %%
 * Copyright (C) 1992 - 2017 Cybernostics Pty Ltd
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

import static au.com.cybernostics.dbdataload.mavenUtils.LocalM2Repository.isGavURL;
import static au.com.cybernostics.dbdataload.mavenUtils.LocalM2Repository.parseGavUrl;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.model.Dependency;
import static au.com.cybernostics.dbdataload.mavenUtils.LocalMavenArtifactFetcher.localFileURLForArtifact;
import static au.com.cybernostics.dbdataload.mavenUtils.LocalMavenArtifactFetcher.localFolderFor;
import static au.com.cybernostics.dbdataload.mavenUtils.RemoteMavenArtifactFetcher.fetchArtfiact;
import java.io.File;
import java.net.URISyntaxException;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 *
 * @author jason
 */
public class JarResourceAccessorFactory {
    
    public static final Logger LOG = Logger.getLogger(JarResourceAccessorFactory.class.getCanonicalName());
    /**
     * mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get \
     * -DrepoUrl=url \ -Dartifact=groupId:artifactId:version
     * @param rootSpec
     * @return 
     */

    public static ResourceAccessor resourceAccessor(String rootSpec) {
        URL rootURL = asURL(rootSpec);
        
        if(rootURL!=null){
            LOG.info("Root URL is:" + rootURL);
            
            URL[] urls = {rootURL};
            URLClassLoader classLoader = new URLClassLoader(urls, null);
            return new ClassLoaderResourceAccessor(classLoader);
            
        }
        return null;
    }

    public static URL asURL(String rootSpec) {
        if(rootSpec!=null){
            try {
                if (isFileURL(rootSpec) || isJarFileURL(rootSpec)) {
                    LOG.info("Is Jar file");
                    return new URL(rootSpec);
                }
                if (isGavURL(rootSpec)) {
                    Dependency artifact = parseGavUrl(rootSpec);
                    if (!localFolderFor(artifact).exists()) {
                        LOG.info("Local file doesn't exist - trying remote");
                        try {
                            fetchArtfiact(artifact);
                        } catch (MavenInvocationException ex) {
                            Logger.getLogger(JarResourceAccessorFactory.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    URL localFileURLForArtifact = localFileURLForArtifact(artifact);
                    if (!localFolderFor(artifact).exists()) {
                        Logger logger = Logger.getLogger(JarResourceAccessorFactory.class.getSimpleName());
                        logger.severe("Could not fetch artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId());
                        return null;
                    }
                    return localFileURLForArtifact;
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(JarResourceAccessorFactory.class.getName()).log(Level.SEVERE, "Malformed URL", ex);
                throw new RuntimeException(ex);
            }
            
        }
        throw new IllegalArgumentException("unknown or bad artifact spec:"+rootSpec);
    }

    private static boolean isFileURL(String rootSpec) {
        return rootSpec.startsWith("file:/");
    }

    private static boolean isJarFileURL(String rootSpec) {
        return rootSpec.startsWith("jar:file:/");
    }

}
