package au.com.cybernostics.dbdataload.mavenUtils;

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

import static java.lang.String.format;
import java.util.Arrays;
import java.util.Properties;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 *
 * @author jason
 */
public class RemoteMavenArtifactFetcher {
    
    private final static String gavFormat = "%s:%s%s";
    
    public static void fetchArtfiact(Dependency dependency) throws MavenInvocationException{
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList("dependency:get"));

        String versionSuffix = ((dependency.getVersion() != null) ? ":" + dependency.getVersion() : ":LATEST");
        Properties properties = new Properties();
        properties.setProperty("artifact", 
                format(gavFormat, dependency.getGroupId(), dependency.getArtifactId(), versionSuffix)
                );
        properties.setProperty("transitive", "false");
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);

    }
    
}
