package nl.geostandaarden.imx.fieldlab.source.rest;

import java.util.Map;

public final class JsonMapFlattener {

  public static Map<String, Object> flatten(Map<String, Object> resource) {
    if (resource.containsKey("nummeraanduiding")) {
      return (Map<String, Object>) resource.get("nummeraanduiding");
    }

    if (resource.containsKey("verblijfsobject")) {
      return flatten((Map<String, Object>) resource.values().iterator().next());
    }

    return resource;
  }
}
