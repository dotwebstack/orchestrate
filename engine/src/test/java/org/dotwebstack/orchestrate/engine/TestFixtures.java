package org.dotwebstack.orchestrate.engine;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.PropertyMapping;
import org.dotwebstack.orchestrate.model.PropertyPath;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ModelMapping;
import org.dotwebstack.orchestrate.model.ObjectTypeMapping;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.SourceTypeRef;
import org.dotwebstack.orchestrate.model.transforms.Coalesce;
import org.dotwebstack.orchestrate.model.transforms.TestPredicate;
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.types.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFixtures {

  public static ModelMapping createModelMapping() {
    var targetModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Adres")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("postcode")
                .type(ScalarTypes.STRING)
                .build())
            .property(Attribute.builder()
                .name("straatnaam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("plaatsnaam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("isHoofdadres")
                .type(ScalarTypes.BOOLEAN)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .build();

    var sourceModel = Model.builder()
        .objectType(ObjectType.builder()
            .name("Nummeraanduiding")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("huisnummer")
                .type(ScalarTypes.INTEGER)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Attribute.builder()
                .name("postcode")
                .type(ScalarTypes.STRING)
                .build())
            .property(Relation.builder()
                .name("ligtAan")
                .target(ObjectTypeRef.forType("OpenbareRuimte"))
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("ligtIn")
                .target(ObjectTypeRef.forType("Woonplaats"))
                .cardinality(Cardinality.OPTIONAL)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("OpenbareRuimte")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("ligtIn")
                .target(ObjectTypeRef.forType("Woonplaats"))
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Woonplaats")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Attribute.builder()
                .name("naam")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .build())
            .build())
        .objectType(ObjectType.builder()
            .name("Verblijfsobject")
            .property(Attribute.builder()
                .name("identificatie")
                .type(ScalarTypes.STRING)
                .cardinality(Cardinality.REQUIRED)
                .identifier(true)
                .build())
            .property(Relation.builder()
                .name("heeftAlsHoofdadres")
                .target(ObjectTypeRef.forType("Nummeraanduiding"))
                .cardinality(Cardinality.REQUIRED)
                .build())
            .property(Relation.builder()
                .name("heeftAlsNevenadres")
                .target(ObjectTypeRef.forType("Nummeraanduiding"))
                .cardinality(Cardinality.MULTI)
                .build())
            .build())
        .build();

    var adresMapping = ObjectTypeMapping.builder()
        .sourceRoot(SourceTypeRef.fromString("bag:Nummeraanduiding"))
        .propertyMapping("identificatie", PropertyMapping.builder()
            .sourcePath(PropertyPath.fromString("identificatie"))
            .build())
        .propertyMapping("huisnummer", PropertyMapping.builder()
            .sourcePath(PropertyPath.fromString("huisnummer"))
            .build())
        .propertyMapping("postcode", PropertyMapping.builder()
            .sourcePath(PropertyPath.fromString("postcode"))
            .build())
        .propertyMapping("straatnaam", PropertyMapping.builder()
            .sourcePath(PropertyPath.fromString("ligtAan/naam"))
            .build())
        .propertyMapping("plaatsnaam", PropertyMapping.builder()
            .sourcePath(PropertyPath.fromString("ligtIn/naam"))
            .sourcePath(PropertyPath.fromString("ligtAan/ligtIn/naam"))
            .transform(Coalesce.getInstance())
            .build())
        .propertyMapping("isHoofdadres", PropertyMapping.builder()
            .sourcePath(PropertyPath.fromString("Verblijfsobject:heeftAlsHoofdadres/identificatie"))
            .transform(TestPredicate.builder()
                .name("nonNull")
                .predicate(Objects::nonNull)
                .build())
            .build())
        .build();

    return ModelMapping.builder()
        .targetModel(targetModel)
        .sourceModel("bag", sourceModel)
        .objectTypeMapping("Adres", adresMapping)
        .build();
  }
}
