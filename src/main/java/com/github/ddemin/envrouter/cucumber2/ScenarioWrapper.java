package com.github.ddemin.envrouter.cucumber2;

import static java.lang.String.format;

import com.github.ddemin.envrouter.base.TestEntityWrapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gherkin.events.PickleEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Dmitrii Demin on 18.09.2017.
 */
@Getter
@Slf4j(topic = "wrapper")
public class ScenarioWrapper extends TestEntityWrapper<PickleEvent> {

  @SuppressFBWarnings
  public ScenarioWrapper(
      @NonNull PickleEvent entity,
      String requiredEnvironmentName,
      int priority,
      boolean requiresHardLocking) {
    super(entity, requiredEnvironmentName, priority, requiresHardLocking);
  }

  @Override
  public String toString() {
    return format(
        "%s/'%s' (Env: %s, Priority: %d)",
        getEntity().uri,
        getEntity().pickle.getName(),
        getRequiredEnvironmentName(),
        getPriority()
    );
  }

  @Override
  public String getName() {
    return getEntity().pickle.getName();
  }

  @Override
  public String getPath() {
    return getEntity().uri;
  }

}
