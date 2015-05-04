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
package org.sonar.batch.bootstrap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.sonar.core.plugins.RemotePlugin;
import org.sonar.home.cache.FileCache;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BatchPluginInstallerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  FileCache fileCache = mock(FileCache.class);
  BatchPluginPredicate pluginPredicate = mock(BatchPluginPredicate.class);

  @Test
  public void listRemotePlugins() {

    ServerClient server = mock(ServerClient.class);
    when(server.request("/deploy/plugins/index.txt")).thenReturn("checkstyle,false\nsqale,false");
    BatchPluginInstaller installer = new BatchPluginInstaller(server, fileCache, pluginPredicate);

    List<RemotePlugin> remotePlugins = installer.listRemotePlugins();
    assertThat(remotePlugins).extracting("key").containsOnly("checkstyle", "sqale");
  }

  @Test
  public void should_download_plugin() throws Exception {
    File pluginJar = temp.newFile();
    when(fileCache.get(eq("checkstyle-plugin.jar"), eq("fakemd5_1"), any(FileCache.Downloader.class))).thenReturn(pluginJar);

    ServerClient server = mock(ServerClient.class);
    BatchPluginInstaller installer = new BatchPluginInstaller(server, fileCache, pluginPredicate);

    RemotePlugin remote = new RemotePlugin("checkstyle", true).setFile("checkstyle-plugin.jar", "fakemd5_1");
    File file = installer.download(remote);

    assertThat(file).isEqualTo(pluginJar);
  }

  @Test
  public void should_fail_to_get_plugin_index() throws Exception {
    thrown.expect(IllegalStateException.class);

    ServerClient server = mock(ServerClient.class);
    doThrow(new IllegalStateException()).when(server).request("/deploy/plugins/index.txt");

    new BatchPluginInstaller(server, fileCache, pluginPredicate).installRemotes();
  }
}
