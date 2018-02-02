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
import static com.jayway.awaitility.Awaitility.await;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

/**
 * Wrapper object for a liquibase migration contained in a jar file on the local
 * filesystem or in a maven artifact or any location for which you can provide a
 * ResourceAccessor
 *
 * @author jason
 */
public class ExternalLiquibaseMigration {

    private static Logger logger = Logger.getLogger(ExternalLiquibaseMigration.class.getName());

    private final ResourceAccessor resourceAccessor;

    private final List<String> dbchangelogs;

    private final DataSource dbToUpdate;

    /**
     *
     * @param dbToUpdate The DB connection to use
     * @param resourceAccessor The class to grab the changeset resources
     * @param changelogs A list of paths to master changelogs allowing you to
     * apply multiple sets of changes. (NOTE: The change ids will all live in
     * the same table, so they will need to be unique across master changesets.
     */
    ExternalLiquibaseMigration(DataSource dbToUpdate, ResourceAccessor resourceAccessor, List<String> changelogs) {
        this.resourceAccessor = resourceAccessor;
        this.dbchangelogs = changelogs;
        this.dbToUpdate = dbToUpdate;
    }
    
    private static Callable<Boolean> dbCheck(DataSource datasource){
        return ()-> {
            while(true){
                try{
                if(datasource.getConnection().isValid(100)){
                    return true;
                }
                }catch(Throwable t){
                    logger.warning("Couldn't reach DB yet.");
                }
            }
        };
    }

    /**
     * Call all the master changesets in this object.
     */
    public void update()  {

        try {
            if (resourceAccessor != null) {
                ExecutorService executor = Executors.newFixedThreadPool(1);
                Future<Boolean> dbOk = executor.submit(dbCheck(dbToUpdate));
                
                try {
                    dbOk.get(60000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ExternalLiquibaseMigration.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ExternalLiquibaseMigration.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TimeoutException ex) {
                    Logger.getLogger(ExternalLiquibaseMigration.class.getName()).log(Level.INFO, "Unable to get datasource conncection in 60 seconds:{0}", ex.getLocalizedMessage());
                }

                JdbcConnection jdbcConnection = new JdbcConnection(dbToUpdate.getConnection());
                for (String changelog : dbchangelogs) {
                    Liquibase liquibase = new Liquibase(changelog, resourceAccessor, jdbcConnection);
                    liquibase.setIgnoreClasspathPrefix(true);
                    liquibase.update(null, new LabelExpression());
                }

            }
        } catch (LiquibaseException ex) {
            ;
            logger.log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

}
