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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 *
 * @author jason
 */
class ExternalLiquibaseMigrationList {
    
    private List<ExternalLiquibaseMigration> migrations;

    public ExternalLiquibaseMigrationList(List<ExternalLiquibaseMigration> migrations) {
        this.migrations = migrations;
    }
    
    @PostConstruct
    public void update(){
        migrations.forEach(m->m.update());
        
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(()->{
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExternalLiquibaseMigrationList.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);});
        
    }
    
}
