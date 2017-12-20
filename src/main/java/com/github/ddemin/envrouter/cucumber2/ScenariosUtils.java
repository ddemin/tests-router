package com.github.ddemin.envrouter.cucumber2;

import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Dmitrii Demin on 18.09.2017.
 */
@Slf4j
public class ScenariosUtils {

  private static final String CUKE_ENV_TAG = "@Env";
  private static final String CUKE_PRIORITY_TAG = "@Priority";

  private ScenariosUtils() {
  }

  /**
   * Wrap cucumber-jvm feature and parse its priority tag and required environment tag.
   *
   * @param scenario cucumber-jvm scenario (PickleEvent)
   * @param feature cucumber-jvm feature
   * @return list of wrapped features
   */
  public static ScenarioWrapper wrapScenario(@NonNull PickleEvent scenario, CucumberFeature feature) {
    log.debug("Wrap scenario: {}", scenario.uri);
    return new ScenarioWrapper(
        scenario,
        getScenarioRequiredEnvironment(scenario, feature),
        getScenarioPriority(scenario, feature)
    );
  }

  /**
   * Wrap cucumber-jvm features and parse its priority tag and required environment tag.
   *
   * @param scenariosMap feature:scenarios map
   * @return list of wrapped features
   */
  public static List<ScenarioWrapper> wrapScenarios(@NonNull Map<CucumberFeature, List<PickleEvent>> scenariosMap) {
    log.info(
        "Wrap scenarios: {}",
        scenariosMap.values().stream().flatMap(Collection::stream).map(ev -> ev.uri).collect(Collectors.toList())
    );
    return scenariosMap.entrySet().stream()
        .flatMap(
            entry ->
                entry.getValue().stream().map(scenario -> wrapScenario(scenario, entry.getKey()))
        )
        .collect(Collectors.toList());
  }

  private static int getScenarioPriority(@NonNull PickleEvent scenario, CucumberFeature feature) {
    return extractTagEnding(scenario, CUKE_PRIORITY_TAG)
        .filter(priorityStr -> priorityStr.matches("[\\d]+"))
        .map(Integer::parseInt)
        .orElse(FeaturesUtils.getFeaturePriority(feature));
  }

  private static String getScenarioRequiredEnvironment(@NonNull PickleEvent scenario, CucumberFeature feature) {
    return extractTagEnding(scenario, CUKE_ENV_TAG).orElse(FeaturesUtils.getFeatureRequiredEnvironment(feature));
  }

  private static Optional<String> extractTagEnding(@NonNull PickleEvent scenario,
      @NonNull String str) {
    return scenario.pickle.getTags().stream()
        .filter(tag -> tag.getName().startsWith(str))
        .map(tag -> StringUtils.substringAfterLast(tag.getName(), str))
        .reduce((first, second) -> second);
  }

}
