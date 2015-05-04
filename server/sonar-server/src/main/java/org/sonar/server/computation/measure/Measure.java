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

package org.sonar.server.computation.measure;

public class Measure {

  private String metricKey;
  private String componentUuid;

  private Double value;
  private String textValue;

  public String getComponentUuid() {
    return componentUuid;
  }

  public Measure setComponentUuid(String componentUuid) {
    this.componentUuid = componentUuid;
    return this;
  }

  public String getMetricKey() {
    return metricKey;
  }

  public Measure setMetricKey(String metricKey) {
    this.metricKey = metricKey;
    return this;
  }

  public String getTextValue() {
    return textValue;
  }

  public Measure setTextValue(String textValue) {
    this.textValue = textValue;
    return this;
  }

  public Double getValue() {
    return value;
  }

  public Measure setValue(Double value) {
    this.value = value;
    return this;
  }
}
