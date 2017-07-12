package gorm.graphql

import org.grails.datastore.gorm.GormEntity
import org.grails.gorm.graphql.binding.GraphQLDataBinder

/**
 * A default data binder using Grails data binding
 *
 * @author James Kleeh
 */
class DefaultGraphQLDataBinder implements GraphQLDataBinder {

    @Override
    void bind(Object object, Map data) {
        object.properties = data
    }
}
