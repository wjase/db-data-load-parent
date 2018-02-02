package au.com.cybernostics.dbdataload.mavenUtils;

/*-
 * #%L
 * db-data-load
 * %%
 * Copyright (C) 1992 - 2018 Cybernostics Pty Ltd
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import static org.apache.maven.artifact.Artifact.LATEST_VERSION;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 *
 * @author jason
 */
public class LocalMavenArtifactFetcher {

    private static String userHome = System.getProperty("user.home");
    private static String mavenRoot = String.format("%s/.m2/repository", userHome);

    public static void setMavenRoot(String mavenRoot) {
        LocalMavenArtifactFetcher.mavenRoot = mavenRoot;
        if (!new File(mavenRoot).exists()) {
            throw new IllegalArgumentException("Maven root doesn't exist:" + mavenRoot);
        }
    }

    /**
     * Either takes a local URL for a jar file and returns it or a gav which it
     * parses into a Dependency to download. The downloaded URL points to a jar
     * in the .m2 directory
     *
     * @param gav
     * @return
     */
    public static URL localFileURLForArtifact(Dependency dep) {

        File localArtifactRoot = localFolderFor(dep);
        if (localArtifactRoot.exists()) {
            try {
                String version = dep.getVersion();
                version = (version == null || LATEST_VERSION.equals(version)) ? latestLocalVersion(localArtifactRoot) : version;
                String urlPath = String.format("file:%s/%s/%s-%s.jar",
                        localArtifactRoot.getAbsolutePath(),
                        version,
                        dep.getArtifactId(),
                        version);
                File localPath = new File(new URI(urlPath));
                if(!localPath.exists()){
                    RemoteMavenArtifactFetcher.fetchArtfiact(dep);
                }
                return new URL(urlPath);
                
            } catch (MalformedURLException ex) {
                Logger.getLogger(LocalMavenArtifactFetcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(LocalMavenArtifactFetcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MavenInvocationException ex) {
                Logger.getLogger(LocalMavenArtifactFetcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static String latestLocalVersion(File localArtifactRoot) {
        String version;
        version = Stream.of(localArtifactRoot.list())
                .map(n -> new ComparableVersion(n))
                .max(ComparableVersion::compareTo)
                .get()
                .toString();
        return version;
    }

    public static File localFolderFor(Dependency dep) {
        String localArtifactRoot = mavenRoot + String.format("/%s/%s",
                dep.getGroupId().replaceAll("\\.", "/"),
                dep.getArtifactId()
        );
        return new File(localArtifactRoot);

    }
}
