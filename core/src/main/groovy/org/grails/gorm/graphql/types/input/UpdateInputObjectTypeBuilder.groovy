package org.grails.gorm.graphql.types.input

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.entity.property.manager.GraphQLDomainPropertyManager

@CompileStatic
@InheritConstructors
class UpdateInputObjectTypeBuilder extends InputObjectTypeBuilder {

    GraphQLDomainPropertyManager.Builder builder = propertyManager.builder()
            .excludeTimestamps()
            .excludeIdentifiers()
            .alwaysNullable()

    GraphQLPropertyType type = GraphQLPropertyType.UPDATE
}
