package org.bonitasoft.connectors

import org.bonitasoft.engine.bdm.model.BusinessObjectModel
import org.bonitasoft.engine.bpm.document.DocumentValue
import org.bonitasoft.engine.connector.Connector
import org.bonitasoft.engine.connector.ConnectorException
import org.bonitasoft.engine.connector.ConnectorValidationException

import java.util.logging.Logger

class BdmGeneration implements Connector {

    public static final String USER_NAME = "userName";

    public static final String PASSWORD = "password";

    public static final String DRIVER = "driver";

    public static final String JDBC_URL = "jdbcUrl";

    private String jdbcUrl

    private String userName

    private String password

    private String driver

    SqlUtils sqlUtils = new SqlUtils()
    BdmUtils bdmUtils = new BdmUtils()

    private Logger LOGGER = Logger.getLogger(this.getClass().getName())

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        userName = (String) parameters.get(USER_NAME)
        LOGGER.info(USER_NAME + " " + userName)

        final String passwordString = (String) parameters.get(PASSWORD)
        LOGGER.info(PASSWORD + "***")
        if (passwordString != null && !passwordString.isEmpty()) {
            password = passwordString
        } else {
            password = null
        }
        driver = (String) parameters.get(DRIVER);
        LOGGER.info(DRIVER + " " + driver);
        jdbcUrl = (String) parameters.get(JDBC_URL);
        LOGGER.info(JDBC_URL + " " + jdbcUrl);
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> messages = new ArrayList<String>(0);
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            messages.add("Url can't be empty");
        }
        if (driver == null || driver.isEmpty()) {
            messages.add("Driver is not set");
        }

        if (userName == null || userName.isEmpty()) {
            messages.add("user name is not set");
        }

        if (password == null || password.isEmpty()) {
            messages.add("password is not set");
        }

        if (!messages.isEmpty()) {
            throw new ConnectorValidationException(this, messages);
        }
    }

    @Override
    public void connect() throws ConnectorException {
        try {
            sqlUtils.connect(jdbcUrl, userName, password, driver)
        } catch (final Exception e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        try {
            sqlUtils.disconnect()
        } catch (final Exception e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    Map<String, Object> execute() throws ConnectorException {
        return generateBdmFromDatabase()
    }

    Map<String, Object> generateBdmFromDatabase() {
        try {
            def metadata = []
            def result = [:]
            def allTables = sqlUtils.allTables()
            allTables.each { table ->
                metadata.add([table      : table,
                              columns    : sqlUtils.allColumns(table.table_name),
                              foreignKeys: sqlUtils.allForeignKeys(table.table_name)
                ])
            }
            result.put("sqlMetadata", metadata)

            BusinessObjectModel bdm = bdmUtils.generate("com.company.model", metadata)
            result.put("bdmModel", bdm)

            File bdmZipFile = bdmUtils.getBdmZip(bdm)
            DocumentValue documentValue = new DocumentValue(bdmZipFile.bytes, 'application/zip', bdmZipFile.name)
            result.put("bdmZipFile", documentValue)
            return result;
        } catch (final Exception e) {
            throw new ConnectorException(e);
        }
    }


}