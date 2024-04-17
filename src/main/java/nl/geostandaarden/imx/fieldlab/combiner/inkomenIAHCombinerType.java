package nl.geostandaarden.imx.fieldlab.combiner;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.Path;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombiner;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombinerType;
import nl.geostandaarden.imx.orchestrate.model.lineage.PathExecution;
import nl.geostandaarden.imx.orchestrate.model.result.PathResult;
import nl.geostandaarden.imx.orchestrate.model.result.PropertyMappingResult;

public final class inkomenIAHCombinerType implements ResultCombinerType {

  @Override
  public String getName() {
    return "inkomenIAH";
  }

  @Override
  public ResultCombiner create(Map<String, Object> options) {
    return pathResults -> {
      var groups = pathResults.stream()
          .collect(Collectors.groupingBy(r -> r.getPathExecution()
              .getUsed()));

      var inkomenResults = groups.get(Path.fromString("waarde"));
      var leeftijdResults = groups.get(Path.fromString("geboortedatum"));

      if (inkomenResults.size() != leeftijdResults.size()) {
        throw new OrchestrateException("Path results for 'waarde' and 'geboortedatum' must have identical sizes.");
      }

      var inkomenIAH = IntStream.range(0, inkomenResults.size())
          .map(i -> {
            var inkomen = Optional.ofNullable(inkomenResults.get(i).getValue())
                .map(Integer.class::cast)
                .orElse(0);

            var leeftijd = (int) leeftijdResults.get(i).getValue();

            return leeftijd >= 23 ? inkomen : Integer.max(0, inkomen - 22356);
          }).sum();

      return PropertyMappingResult.builder()
          .value(inkomenIAH)
          .sourceDataElements(pathResults.stream()
              .map(PathResult::getPathExecution)
              .map(PathExecution::getReferences)
              .flatMap(Set::stream)
              .collect(Collectors.toSet()))
          .build();
    };
  }
}
