package adventureconstructor.utils;

import java.sql.Types;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

public class CustomSQLiteDialect extends Dialect {
    private static final String SQLITE_BIT_TYPE = "integer";
    private static final String SQLITE_TINYINT_TYPE = "tinyint";
    private static final String SQLITE_SMALLINT_TYPE = "smallint";
    private static final String SQLITE_INTEGER_TYPE = "integer";
    private static final String SQLITE_BIGINT_TYPE = "bigint";
    private static final String SQLITE_FLOAT_TYPE = "float";
    private static final String SQLITE_REAL_TYPE = "real";
    private static final String SQLITE_DOUBLE_TYPE = "double";
    private static final String SQLITE_NUMERIC_TYPE = "numeric";
    private static final String SQLITE_DECIMAL_TYPE = "decimal";
    private static final String SQLITE_CHAR_TYPE = "char";
    private static final String SQLITE_VARCHAR_TYPE = "varchar";
    private static final String SQLITE_LONGVARCHAR_TYPE = "longvarchar";
    private static final String SQLITE_DATE_TYPE = "date";
    private static final String SQLITE_TIME_TYPE = "time";
    private static final String SQLITE_TIMESTAMP_TYPE = "timestamp";
    private static final String SQLITE_BINARY_TYPE = "blob";
    private static final String SQLITE_VARBINARY_TYPE = "blob";
    private static final String SQLITE_LONGVARBINARY_TYPE = "blob";
    private static final String SQLITE_BLOB_TYPE = "blob";
    private static final String SQLITE_CLOB_TYPE = "clob";
    private static final String SQLITE_BOOLEAN_TYPE = "integer";

    private static final String SQLITE_CONCAT_FUNC = "concat";
    private static final String SQLITE_MOD_FUNC = "mod";
    private static final String SQLITE_SUBSTR_FUNC = "substr";
    private static final String SQLITE_SUBSTRING_FUNC = "substring";

    private static final String SQLITE_CONCAT_FUNC_BEGIN = "";
    private static final String SQLITE_CONCAT_FUNC_SEPARATOR = "||";
    private static final String SQLITE_CONCAT_FUNC_END = "";

    private static final String SQLITE_MOD_FUNC_TEMPLATE = "?1 % ?2";

    private static final String SQLITE_IDENTITY_SELECT_STRING = "select last_insert_rowid()";

    private static final String SQLITE_LIMIT_EXPRESSION_FIRST = " limit ? offset ?";
    private static final String SQLITE_LIMIT_EXPRESSION_SECOND = " limit ?";

    private static final String SQLITE_CREATE_TEMPORARY_TABLE_STRING = "create temporary table if not exists";

    private static final String SQLITE_CURRENT_TIMESTAMP_STRING = "select current_timestamp";

    private static final String SQLITE_ADD_COLUMN_STRING = "add column";

    private static final String EMPTY = "";

    public CustomSQLiteDialect() {
        super();
        registerColumnType(Types.BIT, SQLITE_BIT_TYPE);
        registerColumnType(Types.TINYINT, SQLITE_TINYINT_TYPE);
        registerColumnType(Types.SMALLINT, SQLITE_SMALLINT_TYPE);
        registerColumnType(Types.INTEGER, SQLITE_INTEGER_TYPE);
        registerColumnType(Types.BIGINT, SQLITE_BIGINT_TYPE);
        registerColumnType(Types.FLOAT, SQLITE_FLOAT_TYPE);
        registerColumnType(Types.REAL, SQLITE_REAL_TYPE);
        registerColumnType(Types.DOUBLE, SQLITE_DOUBLE_TYPE);
        registerColumnType(Types.NUMERIC, SQLITE_NUMERIC_TYPE);
        registerColumnType(Types.DECIMAL, SQLITE_DECIMAL_TYPE);
        registerColumnType(Types.CHAR, SQLITE_CHAR_TYPE);
        registerColumnType(Types.VARCHAR, SQLITE_VARCHAR_TYPE);
        registerColumnType(Types.LONGVARCHAR, SQLITE_LONGVARCHAR_TYPE);
        registerColumnType(Types.DATE, SQLITE_DATE_TYPE);
        registerColumnType(Types.TIME, SQLITE_TIME_TYPE);
        registerColumnType(Types.TIMESTAMP, SQLITE_TIMESTAMP_TYPE);
        registerColumnType(Types.BINARY, SQLITE_BINARY_TYPE);
        registerColumnType(Types.VARBINARY, SQLITE_VARBINARY_TYPE);
        registerColumnType(Types.LONGVARBINARY, SQLITE_LONGVARBINARY_TYPE);
        registerColumnType(Types.BLOB, SQLITE_BLOB_TYPE);
        registerColumnType(Types.CLOB, SQLITE_CLOB_TYPE);
        registerColumnType(Types.BOOLEAN, SQLITE_BOOLEAN_TYPE);
        registerFunction(
                SQLITE_CONCAT_FUNC,
                new VarArgsSQLFunction(
                        StringType.INSTANCE,
                        SQLITE_CONCAT_FUNC_BEGIN,
                        SQLITE_CONCAT_FUNC_SEPARATOR,
                        SQLITE_CONCAT_FUNC_END));
        registerFunction(SQLITE_MOD_FUNC, new SQLFunctionTemplate(IntegerType.INSTANCE, SQLITE_MOD_FUNC_TEMPLATE));
        registerFunction(SQLITE_SUBSTR_FUNC, new StandardSQLFunction(SQLITE_SUBSTR_FUNC, StringType.INSTANCE));
        registerFunction(SQLITE_SUBSTRING_FUNC, new StandardSQLFunction(SQLITE_SUBSTR_FUNC, StringType.INSTANCE));
    }

    public boolean supportsIdentityColumns() {
        return true;
    }

    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }

    public String getIdentityColumnString() {
        return SQLITE_INTEGER_TYPE;
    }

    public String getIdentitySelectString() {
        return SQLITE_IDENTITY_SELECT_STRING;
    }

    public boolean supportsLimit() {
        return true;
    }

    public String getLimitString(String query, boolean hasOffset) {
        return new StringBuffer(query.length() + 20)
                .append(query)
                .append(hasOffset ? SQLITE_LIMIT_EXPRESSION_FIRST : SQLITE_LIMIT_EXPRESSION_SECOND)
                .toString();
    }

    public boolean supportsTemporaryTables() {
        return true;
    }

    public String getCreateTemporaryTableString() {
        return SQLITE_CREATE_TEMPORARY_TABLE_STRING;
    }

    public boolean dropTemporaryTableAfterUse() {
        return false;
    }

    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    public String getCurrentTimestampSelectString() {
        return SQLITE_CURRENT_TIMESTAMP_STRING;
    }

    public boolean supportsUnionAll() {
        return true;
    }

    public boolean hasAlterTable() {
        return false;
    }

    public boolean dropConstraints() {
        return false;
    }

    public String getAddColumnString() {
        return SQLITE_ADD_COLUMN_STRING;
    }

    public String getForUpdateString() {
        return EMPTY;
    }

    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    public String getDropForeignKeyString() {
        throw new UnsupportedOperationException("No drop foreign key syntax supported by SQLiteDialect");
    }

    public String getAddForeignKeyConstraintString(
            String constraintName,
            String[] foreignKey,
            String referencedTable,
            String[] primaryKey,
            boolean referencesPrimaryKey) {
        throw new UnsupportedOperationException("No add foreign key syntax supported by SQLiteDialect");
    }

    public String getAddPrimaryKeyConstraintString(String constraintName) {
        throw new UnsupportedOperationException("No add primary key syntax supported by SQLiteDialect");
    }

    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    public boolean supportsCascadeDelete() {
        return false;
    }
}
