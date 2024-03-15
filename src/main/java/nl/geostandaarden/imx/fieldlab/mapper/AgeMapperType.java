package nl.geostandaarden.imx.fieldlab.mapper;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapper;
import nl.geostandaarden.imx.orchestrate.model.mappers.ResultMapperType;

public final class AgeMapperType implements ResultMapperType {

  @Override
  public String getName() {
    return "age";
  }

  @Override
  public ResultMapper create(Map<String, Object> options) {
    return (result, property) -> {
      if (result.isNull()) {
        return result;
      }

      var date = LocalDate.parse(String.valueOf(result.getValue()));
      var age = Period.between(date, LocalDate.now()).getYears();

      return result.withValue(age);
    };
  }
}
