package org.primefaces.forge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;

/**
 * 
 * @author bleathem
 * @author rvkuijk
 */
public enum PrimefacesVersion
{
    PRIMEFACES_2_2_1("Primefaces 2.2.1",
            Arrays.asList(
           		 DependencyBuilder.create("org.primefaces:primefaces:2.2.1")
           		 ),
            Collections.EMPTY_LIST
   ),
   PRIMEFACES_3_0_M1("Primefaces 3.0.M1",
            Arrays.asList(
                     DependencyBuilder.create("org.primefaces:primefaces:3.0.M1")
                     ),
                     Collections.EMPTY_LIST
    ),
    PRIMEFACES_3_0_M2_SNAPSHOT("Primefaces 3.0.M2 Snapshot",
            Arrays.asList(
                     DependencyBuilder.create("org.primefaces:primefaces:3.0.M2-SNAPSHOT")
                     ),
//            Arrays.asList(
//                     DependencyBuilder.create("org.primefaces:primefaces:3.0M1").setScopeType(ScopeType.IMPORT)
//                              .setPackagingType("pom")
//                     )
                     Collections.EMPTY_LIST
    );

   private List<? extends Dependency> dependencies;
   private List<? extends Dependency> dependencyManagement;
   private String name;

   private PrimefacesVersion(String name, List<? extends Dependency> deps, List<? extends Dependency> depManagement)
   {
      this.name = name;
      this.dependencies = deps;
      this.dependencyManagement = depManagement;
   }

   public List<? extends Dependency> getDependencies()
   {
      return dependencies;
   }

   public List<? extends Dependency> getDependencyManagement()
   {
      return dependencyManagement;
   }

   @Override
   public String toString()
   {
      return name;
   }
}
