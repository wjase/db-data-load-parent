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

import au.com.cybernostics.dbdataload.mavenUtils.LocalM2Repository;
import static au.com.cybernostics.dbdataload.mavenUtils.LocalM2Repository.setEnvironment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import static java.util.Collections.EMPTY_LIST;
import java.util.List;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import javax.sql.DataSource;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class})
public class LiquibaseDatabaseInitialiserApplication implements CommandLineRunner {

    //
    private Logger LOG = Logger.getLogger(LiquibaseDatabaseInitialiserApplication.class.getName());
    
    public static void main(String[] args) {

        SpringApplication.run(LiquibaseDatabaseInitialiserApplication.class, args);
    }
    
    @Override
    public void run(String... strings) throws Exception {
    }
/**
 * 
 * @param ds
 * @param migrations json object specifying what migrations to run. eg
 * [{"changeLogRoot":"gav:au.com.cybernostics.changesets:changeset1",
     * "changelogs":["db/changelog/db.changelog-master.yaml",
     * "db/changelog2/db.changelog-master.yaml" ]}]
     * 
     * the changeLogRoot can be a jar url or a special "gav:" url with
     * optional version using the latest by default.
     * Note: changelogs is an array of paths into the bundle which could
     * contain multiple changesets
     * 
     * changeLogRoot - either a URL pointing to the root of a set of changesets
     * or a gav: url which will fetch from maven and resolve to a jar URL.
     * changelogs - array of paths relative to the root
 * @param localMavenRepo
 * @return 
 */
    @Bean
    public ExternalLiquibaseMigrationList elm(DataSource ds, 
            @Value("${db.migrations:}") String migrations, 
            Environment environment) {
        System.out.println("Running liquibase migration");
        
        setEnvironment(environment);
        
        System.out.println("Maven Root:"+ LocalM2Repository.getMavenRoot());

        List<ExternalLiquibaseMigration> migrationsList = EMPTY_LIST;

        try{
            if (!isBlank(migrations)) {

                LOG.info("Processing Migrations:" + migrations);

                Type listType = new TypeToken<List<ExternalJarMigration>>() {
                }.getType();
                List<ExternalJarMigration> mc = new Gson().fromJson(migrations, listType);
                if (mc != null) {
                    migrationsList = //EMPTY_LIST;
                            mc
                            .stream()
                            .map(m -> new ExternalLiquibaseMigration(ds,
                            JarResourceAccessorFactory.resourceAccessor(m.getChangelogRoot()),
                            m.getChangelogs()))
                            .filter(it->it!=null)
                            .collect(toList());

                }
            } else {
                LOG.severe("No migrations set - no database changes will be applied.");
            }
        
            return new ExternalLiquibaseMigrationList(migrationsList);
        }catch(Throwable t){
            t.printStackTrace();
            LOG.severe("Error running migrations - exception thrown");
            
        }

        throw new IllegalStateException("Coudn't create migrations");
    }

}
