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

import com.tngtech.archunit.core.domain.JavaClasses;


public class ReweArchitectureJohnLakosMetrics {

    private JavaClasses classes;

    private DependsUpon dependsUpon;
    private UsedFrom usedFrom;

    public ReweArchitectureJohnLakosMetrics(JavaClasses classes) {
        this.classes = classes;
        this.dependsUpon = new DependsUpon(classes);
        this.usedFrom = new UsedFrom(classes);
    }

    public int getCumulativeComponentDependency() {
        return dependsUpon.getCumulativeComponentDependency();
    }

    public double getAverageComponentDependency() {

        return (double) getCumulativeComponentDependency() / (double) dependsUpon.getTotalNumberOfComponents();
    }

    public double getRelativeAverageComponentDependency() {

        double acd = getAverageComponentDependency();

        return (acd / (double) dependsUpon.getTotalNumberOfComponents())*100d;
    }

    public double getNormalizedCumulativeComponentDependency () {

        double ccd = (double) getCumulativeComponentDependency();
        double ccd_BinaryTree = (double) getCCDForBalancedBinaryTreeWithNoOfComponents(dependsUpon.getTotalNumberOfComponents());

        return ccd / ccd_BinaryTree;
    }

    /*
     * calculates CCD for a balanced binary tree with a certain number of components
     * a balanced binary tree gets a new level of components when the level above is full
     * the level below can contain double as many components as the level above
     * used-from for level 1 is 1, for level 2 is 2 and so on
     * we add as many used-from values as here are components level per level
     */
    private int getCCDForBalancedBinaryTreeWithNoOfComponents (int noOfComponents) {

        int result = 0;

        int componentsPerLevel = 1;
        int currentComponentOnLevel = 0;
        int usedFromWeightOnLevel = 1;
        int alreadyCounted = 0;

        while (alreadyCounted < noOfComponents) {

            result += usedFromWeightOnLevel;

            currentComponentOnLevel++;
            alreadyCounted++;

            if (currentComponentOnLevel >= componentsPerLevel) {
                // lets move one level lower in our binary tree
                currentComponentOnLevel = 0;
                usedFromWeightOnLevel++;
                componentsPerLevel *= 2;
            }

        }

        return result;
    }

}
