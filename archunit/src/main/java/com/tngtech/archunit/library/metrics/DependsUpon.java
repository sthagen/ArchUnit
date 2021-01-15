/*
 * Copyright 2014-2021 TNG Technology Consulting GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tngtech.archunit.library.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

public class DependsUpon {
    private JavaClasses classes;
    private Map<String, Integer> dependsUpon = new HashMap<>();

    public DependsUpon(final JavaClasses classes) {
        this.classes = classes;
        calculateAllDependsUpon();
    }

    public int getTotalNumberOfComponents() {
        return dependsUpon.size();
    }

    public int getCumulativeComponentDependency() {

        if (dependsUpon.isEmpty()) {
            calculateAllDependsUpon();
        }

        int cumulativeDependency = 0;

        for (int numberOfDependencies : dependsUpon.values()) {
            cumulativeDependency += numberOfDependencies;
        }

        return cumulativeDependency;
    }

    public int getDependsUpon (JavaClass clazz) {
        return getDependsUpon(clazz.getName());
    }

    public int getDependsUpon (String javaClassName) {
        if (dependsUpon.isEmpty()) {
            calculateAllDependsUpon();
        }

        if (dependsUpon.containsKey(javaClassName)) {
            return dependsUpon.get(javaClassName);
        } else {
            return 0;
        }
    }

    public String getMaximumDependsUpon() {
        if (dependsUpon.isEmpty()) {
            calculateAllDependsUpon();
        }

        Map.Entry<String, Integer> maxDependsUpon = null;

        for (Map.Entry<String, Integer> entry : dependsUpon.entrySet()) {
            if (maxDependsUpon == null || maxDependsUpon.getValue() < entry.getValue()) {
                maxDependsUpon = entry;
            }
        }

        return maxDependsUpon.getKey();
    }

    private int calculateSingleDependsUpon(JavaClass clazz, Set<String> alreadyVisited) {
        // depends-upon includes the class itself, therefore we start with the class itsfelf
        int count = 1;
        alreadyVisited.add(clazz.getName());

        for (Dependency classDependency : clazz.getDirectDependenciesFromSelf()) {

            JavaClass accessedClass = classDependency.getTargetClass();

            // only dependencies within the analyzed scope are of interest
            if (classes.contain(accessedClass.getName())) {

                if (!alreadyVisited.contains(accessedClass.getName())) {
                    count += calculateSingleDependsUpon(accessedClass, alreadyVisited);
                }
            }
        }

        return count;
    }

    private void calculateAllDependsUpon() {
        for (JavaClass clazz : classes) {

            Set<String> alreadyVisited = new HashSet<>();
            int result = calculateSingleDependsUpon(clazz, alreadyVisited);
            dependsUpon.put(clazz.getName(),result);
        }
    }
}
