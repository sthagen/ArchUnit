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

public class UsedFrom {
    private JavaClasses classes;
    private Map<String, Integer> usedFrom = new HashMap<>();

    public UsedFrom(final JavaClasses classes) {
        this.classes = classes;
        calculateAllUsedFrom();
    }

    public int getUsedFrom (String javaClassName) {
        if (usedFrom.isEmpty()) {
            calculateAllUsedFrom();
        }

        if (usedFrom.containsKey(javaClassName)) {
            return usedFrom.get(javaClassName);
        } else {
            return 0;
        }
    }

    public int getUsedFrom (JavaClass clazz) {
        return getUsedFrom(clazz.getName());
    }

    public String getMaximumUsedFrom() {
        if (usedFrom.isEmpty()) {
            calculateAllUsedFrom();
        }

        Map.Entry<String, Integer> maxDependsUpon = null;

        for (Map.Entry<String, Integer> entry : usedFrom.entrySet()) {
            if (maxDependsUpon == null || maxDependsUpon.getValue() < entry.getValue()) {
                maxDependsUpon = entry;
            }
        }

        return maxDependsUpon.getKey();

    }

    private int calculateSingleUsedFrom(JavaClass clazz, Set<String> alreadyVisited) {
        // used-from includes the class itself, therefore we start with the class itsfelf
        int count = 1;
        alreadyVisited.add(clazz.getName());

        for (Dependency classDependency : clazz.getDirectDependenciesToSelf()) {

            JavaClass accessingClass = classDependency.getOriginClass();

            // only dependencies within the analyzed scope are of interest
            if (classes.contain(accessingClass.getName())) {

                if (!alreadyVisited.contains(accessingClass.getName())) {
                    count += calculateSingleUsedFrom(accessingClass, alreadyVisited);
                }
            }
        }

        return count;
    }

    private void calculateAllUsedFrom() {
        for (JavaClass clazz : classes) {

            Set<String> alreadyVisited = new HashSet<>();
            int result = calculateSingleUsedFrom(clazz, alreadyVisited);
            usedFrom.put(clazz.getName(),result);
        }

    }
}
