package org.grails.gorm.graphql.types

import graphql.Scalars
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.schema.*
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.gorm.graphql.GraphQL
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.GraphQLPropertyType
import org.grails.gorm.graphql.types.scalars.GormScalars

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by jameskleeh on 7/5/17.
 */
class DefaultGraphQLTypeManager implements GraphQLTypeManager {

    protected static final Map<Class, Class> primitiveBoxes = [:]

    protected static final ConcurrentHashMap<Class, GraphQLType> typeMap = new ConcurrentHashMap<>()

    protected static final ConcurrentHashMap<Class, GraphQLEnumType> enumTypes = new ConcurrentHashMap<>()

    static {
        primitiveBoxes.put(int, Integer)
        primitiveBoxes.put(long, Long)
        primitiveBoxes.put(double, Double)
        primitiveBoxes.put(float, Float)
        primitiveBoxes.put(boolean, Boolean)
        primitiveBoxes.put(char, Character)
        primitiveBoxes.put(byte, Byte)
        primitiveBoxes.put(short, Short)

        GraphQLScalarType

        typeMap.put(Integer, Scalars.GraphQLInt)
        typeMap.put(Long, Scalars.GraphQLLong)
        typeMap.put(Short, Scalars.GraphQLShort)
        typeMap.put(Byte, Scalars.GraphQLByte)
        typeMap.put(Double, Scalars.GraphQLFloat)
        typeMap.put(Float, GormScalars.GraphQLFloat)
        typeMap.put(BigInteger, Scalars.GraphQLBigInteger)
        typeMap.put(BigDecimal, Scalars.GraphQLBigDecimal)
        typeMap.put(String, Scalars.GraphQLString)
        typeMap.put(Boolean, Scalars.GraphQLBoolean)
        typeMap.put(Character, Scalars.GraphQLChar)
        typeMap.put(UUID, GormScalars.GraphQLUUID)

        /*       java.util.Date.class.getName(),
            Time.class.getName(),
            Timestamp.class.getName(),
            java.sql.Date.class.getName(),
            java.util.Currency.class.getName(),
            TimeZone.class.getName(),

            Class.class.getName(),
            byte[].class.getName(),
            Byte[].class.getName(),
            char[].class.getName(),
            Character[].class.getName(),
            Blob.class.getName(),
            Clob.class.getName(),
            Serializable.class.getName(),
            URI.class.getName(),
            URL.class.getName(),   */

    }

    GraphQLEntityNamingConvention namingConvention

    DefaultGraphQLTypeManager(GraphQLEntityNamingConvention namingConvention) {
        this.namingConvention = namingConvention
    }

    @Override
    GraphQLType getType(Class clazz, boolean nullable = true) {
        if (clazz.isPrimitive()) {
            clazz = boxPrimitive(clazz)
        }
        GraphQLType type = typeMap.get(clazz)
        if (!nullable) {
            GraphQLNonNull.nonNull(type)
        }
        else {
            type
        }
    }

    protected Class boxPrimitive(Class clazz) {
        primitiveBoxes.get(clazz)
    }

    @Override
    void registerType(Class clazz, GraphQLType type) {
        typeMap.put(clazz, type)
    }

    @Override
    GraphQLType getEnumType(Class clazz, boolean nullable) {
        GraphQLEnumType enumType

        if (!enumTypes.containsKey(clazz)) {
            GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                    .name(clazz.simpleName)

            GraphQL annotation = clazz.getAnnotation(GraphQL)

            if (annotation != null && annotation.value()) {
                builder.description(annotation.value())
            }

            clazz.enumConstants.each { Enum anEnum ->
                builder.value(anEnum.name(), anEnum)
            }

            enumType = builder.build()
            enumTypes.put(clazz, enumType)
        }
        else {
            enumType = enumTypes.get(clazz)
        }

        if (!nullable) {
            GraphQLNonNull.nonNull(enumType)
        }
        else {
            enumType
        }
    }

    @Override
    GraphQLType createReference(PersistentEntity entity, GraphQLPropertyType type) {
        final String referenceName = namingConvention.getType(entity, type)
        if (type == GraphQLPropertyType.OUTPUT) {
            GraphQLObjectType.reference(referenceName)
        }
        else {
            GraphQLInputObjectType.reference(referenceName)
        }
    }

    @Override
    GraphQLInputObjectType createUpdateType(PersistentEntity entity, GraphQLInputObjectType createType) {
        new GraphQLInputObjectType(namingConvention.getType(entity, GraphQLPropertyType.UPDATE), createType.description, createType.fields.collect {
            GraphQLInputType unwrappedType
            if (it.type instanceof GraphQLNonNull) {
                unwrappedType = (GraphQLInputType)((GraphQLNonNull)it.type).wrappedType
            }
            else {
                unwrappedType = it.type
            }
            if (unwrappedType instanceof TypeReference) {
                unwrappedType = GraphQLInputObjectType.reference(namingConvention.getType(entity, GraphQLPropertyType.OUTPUT))
            }
            new GraphQLInputObjectField(it.name, it.description, unwrappedType, it.defaultValue)
        })
    }
}
