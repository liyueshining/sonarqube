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
package org.sonar.server.plugins.ws;

import org.junit.Test;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.WebService;
import org.sonar.core.platform.PluginInfo;
import org.sonar.server.plugins.ServerPluginRepository;
import org.sonar.server.ws.WsTester;
import org.sonar.updatecenter.common.Version;

import java.io.File;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.test.JsonAssert.assertJson;

public class InstalledPluginsWsActionTest {
  private static final String DUMMY_CONTROLLER_KEY = "dummy";
  private static final String JSON_EMPTY_PLUGIN_LIST =
    "{" +
      "  \"plugins\":" + "[]" +
      "}";

  private ServerPluginRepository pluginRepository = mock(ServerPluginRepository.class);
  private InstalledPluginsWsAction underTest = new InstalledPluginsWsAction(pluginRepository, new PluginWSCommons());

  private Request request = mock(Request.class);
  private WsTester.TestResponse response = new WsTester.TestResponse();
  private PluginInfo corePlugin = corePlugin("core1", "1.0");

  @Test
  public void action_installed_is_defined() throws Exception {
    WsTester wsTester = new WsTester();
    WebService.NewController newController = wsTester.context().createController(DUMMY_CONTROLLER_KEY);

    underTest.define(newController);
    newController.done();

    WebService.Controller controller = wsTester.controller(DUMMY_CONTROLLER_KEY);
    assertThat(controller.actions()).extracting("key").containsExactly("installed");

    WebService.Action action = controller.actions().iterator().next();
    assertThat(action.isPost()).isFalse();
    assertThat(action.description()).isNotEmpty();
    assertThat(action.responseExample()).isNotNull();
  }

  @Test
  public void empty_array_is_returned_when_there_is_not_plugin_installed() throws Exception {
    underTest.handle(request, response);

    assertJson(response.outputAsString()).setStrictArrayOrder(true).isSimilarTo(JSON_EMPTY_PLUGIN_LIST);
  }

  @Test
  public void core_plugin_are_not_returned() throws Exception {
    when(pluginRepository.getPluginInfos()).thenReturn(of(corePlugin));

    underTest.handle(request, response);

    assertJson(response.outputAsString()).setStrictArrayOrder(true).isSimilarTo(JSON_EMPTY_PLUGIN_LIST);
  }

  @Test
  public void empty_fields_are_not_serialized_to_json() throws Exception {
    when(pluginRepository.getPluginInfos()).thenReturn(
      of(
      new PluginInfo("").setName("").setCore(false)
      )
      );

    underTest.handle(request, response);

    assertThat(response.outputAsString()).doesNotContain("name").doesNotContain("key");
  }

  @Test
  public void verify_properties_displayed_in_json_per_plugin() throws Exception {
    String jarFilename = getClass().getSimpleName() + "/" + "some.jar";
    when(pluginRepository.getPluginInfos()).thenReturn(of(
      new PluginInfo("plugKey").setName("plugName").setCore(false)
        .setDescription("desc_it")
        .setVersion(Version.create("1.0"))
        .setLicense("license_hey")
        .setOrganizationName("org_name")
        .setOrganizationUrl("org_url")
        .setHomepageUrl("homepage_url")
        .setIssueTrackerUrl("issueTracker_url")
        .setFile(new File(getClass().getResource(jarFilename).toURI()))
        .setImplementationBuild("sou_rev_sha1")
      )
      );

    underTest.handle(request, response);

    assertJson(response.outputAsString()).isSimilarTo(
      "{" +
        "  \"plugins\":" +
        "  [" +
        "    {" +
        "      \"key\": \"plugKey\"," +
        "      \"name\": \"plugName\"," +
        "      \"description\": \"desc_it\"," +
        "      \"version\": \"1.0\"," +
        "      \"license\": \"license_hey\"," +
        "      \"organizationName\": \"org_name\"," +
        "      \"organizationUrl\": \"org_url\"," +
        "      \"homepage\": \"homepage_url\"," +
        "      \"issueTrackerUrl\": \"issueTracker_url\"," +
        "      \"implementationBuild\": \"sou_rev_sha1\"" +
        "    }" +
        "  ]" +
        "}"
      );
  }

  @Test
  public void plugins_are_sorted_by_name_then_key_and_only_one_plugin_can_have_a_specific_name() throws Exception {
    when(pluginRepository.getPluginInfos()).thenReturn(
      of(
        plugin("A", "name2"),
        plugin("B", "name1"),
        plugin("C", "name0"),
        plugin("D", "name0")
      )
      );

    underTest.handle(request, response);

    assertJson(response.outputAsString()).setStrictArrayOrder(true).isSimilarTo(
      "{" +
        "  \"plugins\":" +
        "  [" +
        "    {\"key\": \"C\"}" + "," +
        "    {\"key\": \"D\"}" + "," +
        "    {\"key\": \"B\"}" + "," +
        "    {\"key\": \"A\"}" +
        "  ]" +
        "}"
      );
  }

  @Test
  public void only_one_plugin_can_have_a_specific_name_and_key() throws Exception {
    when(pluginRepository.getPluginInfos()).thenReturn(
      of(
        plugin("A", "name2"),
        plugin("A", "name2")
      )
      );

    underTest.handle(request, response);

    assertJson(response.outputAsString()).setStrictArrayOrder(true).isSimilarTo(
      "{" +
        "  \"plugins\":" +
        "  [" +
        "    {\"key\": \"A\"}" +
        "  ]" +
        "}"
      );
    assertThat(response.outputAsString()).containsOnlyOnce("name2");
  }

  private static PluginInfo corePlugin(String key, String version) {
    return new PluginInfo(key).setName(key).setCore(true).setVersion(Version.create(version));
  }

  private static PluginInfo plugin(String key, String name, String version) {
    return new PluginInfo(key).setName(name).setCore(false).setVersion(Version.create(version));
  }

  private static PluginInfo plugin(String key, String name) {
    return new PluginInfo(key).setName(name).setCore(false).setVersion(Version.create("1.0"));
  }
}
