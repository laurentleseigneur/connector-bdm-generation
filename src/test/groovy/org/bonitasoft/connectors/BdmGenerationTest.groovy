package org.bonitasoft.connectors


import org.bonitasoft.engine.bdm.model.BusinessObject
import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bdm.model.UniqueConstraint
import org.bonitasoft.engine.bdm.model.field.RelationField
import org.postgresql.Driver
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class BdmGenerationTest extends Specification {

    public static final String USER_NAME = "foo"
    public static final String PASSWORD = "secret"
    public static final String JDBC_DRIVER = 'org.postgresql.Driver'

    @Shared
    SqlUtils sqlUtils = new SqlUtils()

    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.9")
            .withDatabaseName("dvdrental")
            .withUsername(USER_NAME)
            .withPassword(PASSWORD)

    def fillDatabase() {
        def sql = getSqlScript('/dvdRental.sql')
        def split = sql.split(";")
        sqlUtils.connect(postgreSQLContainer.jdbcUrl, USER_NAME, PASSWORD, JDBC_DRIVER)
        split.each {
            sqlUtils.execute(it)
        }
        sqlUtils.disconnect()
    }

    def String getSqlScript(String sqlScript) {
        def url = getClass().getResource(sqlScript)
        def file = new File(url.toURI())
        file.text
    }

    def should_generate() {
        given: 'A connector and a database '
        fillDatabase()
        def connector = new BdmGeneration()
        connector.setInputParameters([
                (BdmGeneration.USER_NAME): USER_NAME,
                (BdmGeneration.PASSWORD) : PASSWORD,
                (BdmGeneration.DRIVER)   : Driver.class.getCanonicalName(),
                (BdmGeneration.JDBC_URL) : postgreSQLContainer.jdbcUrl]
        )

        when: 'Executing connector'
        connector.connect()
        def execute = connector.execute()
        connector.disconnect()

        then: 'BDM is generated'
        BusinessObjectModel bom = execute.bdmModel
        List<BusinessObject> businessObjects = bom.getBusinessObjects()

        and: 'all generated'
        businessObjects.size() == 15
        BusinessObject address = businessObjects.get(1)

        and: 'exclude FK fields'
        address.fields.size() == 8

        and: 'check relation field'
        RelationField city = address.fields.get(0)
        city.name == "cityObject"
        city.reference.qualifiedName == 'com.company.model.BdmCity'

        and: 'check unique constraint uses BDM field name'
        def uniqueConstraint = address.uniqueConstraints.get(0)
        uniqueConstraint.name == 'PK_address_pkey'
        uniqueConstraint.fieldNames == ['addressId']

        and: 'length is set for all simpleFields'
        businessObjects.each { bo ->
            bo.fields.each { field ->
                if (field.class.getSimpleName() == 'SimpleField') {
                    assert field.length > 0
                }
            }
        }

    }
}
