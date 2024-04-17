package nl.geostandaarden.imx.fieldlab;

import com.google.auto.service.AutoService;
import nl.geostandaarden.imx.fieldlab.mapper.AgeMapperType;
import nl.geostandaarden.imx.fieldlab.combiner.inkomenIAHCombinerType;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.OrchestrateExtension;

@AutoService(OrchestrateExtension.class)
public class FieldlabExtension implements OrchestrateExtension {

  @Override
  public void registerComponents(ComponentRegistry registry) {
    registry.register(new AgeMapperType())
        .register(new inkomenIAHCombinerType());
  }
}
