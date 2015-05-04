/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.computation.design;

import org.sonar.graph.DirectedGraphAccessor;

import javax.annotation.CheckForNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ComputeDsm {

  private static class DependenciesGraph implements DirectedGraphAccessor<Integer, FileDependency> {

    private Set<FileDependency> dependencies = new LinkedHashSet<>();
    private Set<Integer> vertices = new HashSet<>();
    private Map<Integer, Map<Integer, FileDependency>> outgoingDependenciesByComponent = new LinkedHashMap<>();
    private Map<Integer, Map<Integer, FileDependency>> incomingDependenciesByComponent = new LinkedHashMap<>();

    private void addDependency(FileDependency dependency){
      this.dependencies.add(dependency);
      this.vertices.add(dependency.getFrom());
      this.vertices.add(dependency.getTo());
    }

    @Override
    @CheckForNull
    public FileDependency getEdge(Integer from, Integer to) {
      Map<Integer, FileDependency> map = outgoingDependenciesByComponent.get(from);
      if (map != null) {
        return map.get(to);
      }
      return null;
    }

    @Override
    public boolean hasEdge(Integer from, Integer to) {
      return getEdge(from, to) != null;
    }

    @Override
    public Set<Integer> getVertices() {
      return vertices;
    }

    @Override
    public Collection<FileDependency> getOutgoingEdges(Integer from) {
      Map<Integer, FileDependency> deps = outgoingDependenciesByComponent.get(from);
      if (deps != null) {
        return deps.values();
      }
      return Collections.emptyList();
    }

    @Override
    public Collection<FileDependency> getIncomingEdges(Integer to) {
      Map<Integer, FileDependency> deps = incomingDependenciesByComponent.get(to);
      if (deps != null) {
        return deps.values();
      }
      return Collections.emptyList();
    }
  }
}
