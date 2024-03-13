package nl.geostandaarden.imx.fieldlab.source.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.DataRequest;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;
import nl.geostandaarden.imx.orchestrate.engine.source.DataRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@RequiredArgsConstructor
public class RestRepository implements DataRepository {

  private final ObjectMapper jsonMapper = new JsonMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private final HttpClient httpClient;

  private final Map<String, String> paths;

  @Override
  public Mono<Map<String, Object>> findOne(ObjectRequest request) {
    if ("Persoon".equals(request.getObjectType().getName())) {
      var bsn = request.getObjectKey().get("bsn");
      return Mono.just(Map.of("burgerservicenummer", bsn, "naam", "Odessa de Jager"));
    }

    return httpClient.get()
        .uri(getObjectURI(request))
        .responseSingle((response, content) -> content.asInputStream())
        .map(this::parseObject)
        .map(JsonMapFlattener::flatten)
        .onErrorComplete();
  }

  @Override
  public Flux<Map<String, Object>> find(CollectionRequest request) {
    var uri = getCollectionURI(request);

    return httpClient.get()
        .uri(uri)
        .responseSingle((response, content) -> content.asInputStream())
        .map(this::parseCollection)
        .flatMapMany(resource -> Flux.fromIterable(resource.getData()))
        .map(JsonMapFlattener::flatten)
        .onErrorComplete();
  }

  private String getObjectURI(ObjectRequest request) {
    var objectKey = request.getObjectKey()
        .values()
        .iterator()
        .next()
        .toString();

    return getCollectionURI(request)
        .concat("/" + objectKey);
  }

  private String getCollectionURI(DataRequest request) {
    var typeName = request.getObjectType()
        .getName();

    var path = Optional.ofNullable(paths.get(typeName))
        .orElse(typeName);

    return "/" + path;
  }

  private ObjectResource parseObject(InputStream content) {
    try {
      return jsonMapper.readValue(content, ObjectResource.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private CollectionResource parseCollection(InputStream content) {
    try {
      return jsonMapper.readValue(content, CollectionResource.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
