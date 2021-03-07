package org.bonitasoft.connectors

import com.google.common.base.CaseFormat
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter
import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.UniqueConstraint
import org.bonitasoft.engine.bdm.model.field.RelationField
import org.bonitasoft.engine.bdm.model.field.SimpleField
import org.bonitasoft.engine.bdm.validator.BusinessObjectModelValidator
import org.bonitasoft.engine.bdm.validator.ValidationStatus

import java.nio.file.Files
import java.util.logging.Logger

import static org.bonitasoft.engine.bdm.model.field.FieldType.BOOLEAN
import static org.bonitasoft.engine.bdm.model.field.FieldType.FLOAT
import static org.bonitasoft.engine.bdm.model.field.FieldType.INTEGER
import static org.bonitasoft.engine.bdm.model.field.FieldType.LOCALDATE
import static org.bonitasoft.engine.bdm.model.field.FieldType.LOCALDATETIME
import static org.bonitasoft.engine.bdm.model.field.FieldType.LONG
import static org.bonitasoft.engine.bdm.model.field.FieldType.STRING
import static org.bonitasoft.engine.bdm.model.field.FieldType.TEXT

class BdmUtils {

    private Logger LOGGER = Logger.getLogger(this.getClass().getName())

    BusinessObjectModel generate(String packageName, def metadata) {
        BusinessObjectModel model = new BusinessObjectModel()
        metadata.each { tableMetadata ->
            def sqlTableName = tableMetadata.table.table_name.toString().toUpperCase()
            def bdmObjectName = getJavaClassIndentifier("BDM_${sqlTableName}")
            BusinessObject businessObject = new BusinessObject("${packageName}.${bdmObjectName}")
            def excludedFields = addRelationFields(tableMetadata.foreignKeys, packageName, businessObject)
            addPrimaryKeyConstraint(tableMetadata.primaryKey, businessObject, excludedFields)
            def simpleFieldColumns = filterForeignKeyColumns(tableMetadata.columns, excludedFields)
            simpleFieldColumns.each { column ->
                def name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column."column_name")
                def nullable = column.is_nullable == 'NO' ? false : true
                def type = column.data_type

                SimpleField field = new SimpleField()
                field.setName(name)
                field.setNullable(nullable)
                field.setCollection(false)
                def typeMapping = [
                        integer                      : LONG,
                        smallint                     : INTEGER,
                        numeric                      : FLOAT,
                        boolean                      : BOOLEAN,
                        'character varying'          : STRING,
                        character                    : STRING,
                        'USER-DEFINED'               : STRING,
                        'ARRAY'                      : STRING,
                        bytea                        : TEXT,
                        text                         : TEXT,
                        tsvector                     : TEXT,
                        date                         : LOCALDATE,
                        'timestamp without time zone': LOCALDATETIME
                ]

                def fieldType = typeMapping."${column.data_type}" ? typeMapping."${column.data_type}" : null
                if (!fieldType) {
                    throw new IllegalArgumentException("field type [${type}] for column [${table.sqlName}.${name}] is not supported!".toString())
                }
                field.setType(fieldType)
                if (field.getType() == STRING) {
                    def charMaxLength = (column.character_maximum_length as String)?.toInteger()
                    field.setLength(charMaxLength)
                }
                if (!field.getLength()) {
                    //required by studio
                    field.setLength(255)
                }
                businessObject.addField(field)
            }

            model.addBusinessObject(businessObject)
        }
        BusinessObjectModelValidator validator = new BusinessObjectModelValidator()
        ValidationStatus status = validator.validate(model)
        if (!status.isOk()) {
            LOGGER.severe("errors in model")
            status.getStatuses().each {
                LOGGER.severe(it.toString())
            }
            throw new ConnectException("invalid Model")
        }
        model
    }


    def addPrimaryKeyConstraint(primaryKey, BusinessObject businessObject, excludedFields) {
        primaryKey.each { uk ->
            addUniqueConstraint(uk, businessObject, excludedFields)
        }
    }

    def addUniqueConstraint(uk, BusinessObject businessObject, excludedFields) {
        UniqueConstraint uniqueConstraint = new UniqueConstraint()
        uniqueConstraint.name = "PK_${uk.constraint_name}"
        uniqueConstraint.fieldNames = []
        uk.columns.each { column ->
            if (!excludedFields.contains(column.column_name)) {
                uniqueConstraint.fieldNames.add(getJavaFieldIdentifier(column.column_name))
            }
        }
        if (!uniqueConstraint.fieldNames.isEmpty()) {
            businessObject.addUniqueConstraint(uniqueConstraint)
        }
    }

    def addRelationFields(foreignKeys, String packageName, businessObject) {
        def excludedFields = []
        foreignKeys.each { foreignKey ->
            excludedFields.addAll(addRelationField(foreignKey, packageName, businessObject))
        }
        excludedFields
    }

    def addRelationField(foreignKey, String packageName, businessObject) {
        def excludedFields = []
        RelationField relationField = new RelationField()
        def relationFieldName = "${foreignKey.foreign_table_name}_OBJECT"
        relationField.name = getJavaFieldIdentifier(relationFieldName)
        relationField.type = RelationField.Type.AGGREGATION
        relationField.fetchType = RelationField.FetchType.EAGER
        relationField.reference = new BusinessObject("${packageName}.${getJavaClassIndentifier("BDM_${foreignKey.foreign_table_name.toString().toUpperCase()}")}")
        foreignKey.columns.each {
            excludedFields.add(it.column_name)
        }
        businessObject.addField(relationField)
        excludedFields
    }

    def getJavaClassIndentifier(sqlTableName) {
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, sqlTableName)
    }

    def getJavaFieldIdentifier(fieldName) {
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName)
    }

    def filterForeignKeyColumns(def columns, def excluded) {
        def simpleFields = columns.findAll { column ->
            !excluded.contains(column.column_name)
        }
        simpleFields
    }

    File getBdmZip(BusinessObjectModel bom) {
        BusinessObjectModelConverter converter = new BusinessObjectModelConverter()
        byte[] zip = converter.zip(bom)
        File zipFile = Files.createTempFile("bdm", ".zip").toFile()
        zipFile.bytes = zip
        zipFile
    }
}
