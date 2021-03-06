package com.github.ddemin.envrouter.base;

import static com.github.ddemin.envrouter.base.TestEntityWrapper.ANY_ENV;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Dmitrii Demin on 19.09.2017.
 */
@Slf4j
public class TestEntitiesQueues<T extends TestEntityWrapper> {

  private Map<String, Queue<T>> entitiesQueuesForEnvs = new HashMap<>();

  /**
   * See #add.
   *
   * @param entities wrapped entitys
   */
  public void addAll(@NonNull List<T> entities) {
    entities.forEach(this::add);
  }

  /**
   * Add entity to appropriate queue by environment name that required for entity.
   *
   * @param entity wrapped cucumber entity
   */
  public void add(@NonNull T entity) {
    String requiredEnv = entity.getRequiredEnvironmentName();
    if (entitiesQueuesForEnvs.get(requiredEnv) == null) {
      entitiesQueuesForEnvs.put(
          requiredEnv,
          new PriorityQueue<>(
              (o1, o2) -> {
                int compareInt = Integer.compare(o1.getPriority(), o2.getPriority());
                if (compareInt == 0 && o1.isRequiresHardLock() && o2.isRequiresHardLock()) {
                  compareInt = 0;
                } else if (compareInt == 0 && o1.isRequiresHardLock()) {
                  compareInt = 1;
                } else if (compareInt == 0 && o2.isRequiresHardLock()) {
                  compareInt = -1;
                }
                return compareInt;
              })
      );
    }
    entitiesQueuesForEnvs.get(requiredEnv).add(entity);
  }

  /**
   * Poll entity from queue that assign to environment name.
   *
   * @param envName environment
   * @return extract entity from environment queue
   */
  public T pollEntityFor(@NonNull String envName) {
    log.trace("Try to poll entity for environment: {}", envName);

    T chosenEntity;
    Queue<T> queueForEnv = getQueueFor(envName);
    Queue<T> queueForAnyEnv = getQueueFor(ANY_ENV);

    if (queueForEnv == null && queueForAnyEnv == null) {
      chosenEntity = null;
    } else if (queueForEnv == null) {
      chosenEntity = queueForAnyEnv.poll();
    } else if (queueForAnyEnv == null) {
      chosenEntity = queueForEnv.poll();
    } else {
      chosenEntity =
          queueForEnv.peek().getPriority() <= queueForAnyEnv.peek().getPriority()
              ? queueForEnv.poll()
              : queueForAnyEnv.poll();
    }

    log.debug("Entity was pulled from queue: {}", chosenEntity);
    return chosenEntity;
  }

  public Map<String, Queue<T>> getQueuesMap() {
    return entitiesQueuesForEnvs;
  }

  /**
   * Get total count of entitys in queues.
   *
   * @return total count of found entity files
   */
  public int entitiesInAllQueues() {
    return entitiesQueuesForEnvs.values().stream()
        .map(Queue::size)
        .reduce(0, Integer::sum);
  }

  /**
   * Get queue for environment (by name).
   *
   * @param definedEnv name of environment that exists
   * @return queue for environment
   */
  public Queue<T> getQueueFor(@NonNull String definedEnv) {
    Entry<String, Queue<T>> entry = entitiesQueuesForEnvs.entrySet().stream()
        .filter(it -> definedEnv.startsWith(it.getKey()) && it.getValue().size() > 0)
        .findFirst()
        .orElse(null);
    return entry == null ? null : entry.getValue();
  }

  /**
   * Get list of queues that have required environments that don't exist.
   *
   * @param definedEnvs set of environments that exist now
   * @return list of queues for environments that don't exist (weren't provided)
   */
  public List<Entry<String, Queue<T>>> getQueuesForUndefinedEnvs(@NonNull Set<Environment> definedEnvs) {
    log.trace("Get all queues for undefined environments...");
    return this.getQueuesMap().entrySet().stream()
        .filter(
            entry ->
                definedEnvs.stream().noneMatch(
                    confEnv -> confEnv.getName().startsWith(entry.getKey())
                )
        )
        .filter(entry -> !entry.getKey().equals(ANY_ENV))
        .filter(entry -> entry.getValue().size() > 0)
        .collect(Collectors.toList());
  }

}
