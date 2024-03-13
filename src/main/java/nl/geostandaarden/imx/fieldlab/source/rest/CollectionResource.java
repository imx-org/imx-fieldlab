package nl.geostandaarden.imx.fieldlab.source.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CollectionResource {

  private final List<Map<String, Object>> data = new ArrayList<>();
}
