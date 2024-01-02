

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DBconnection {

	private static final String DB_NAME = "Cars";
	private static DBconnection singleton;
	private Map<String, List<String>> tablesAndColumns = new HashMap<>();
	private Connection c;
	private final ObservableList<Operation> OPERATIONS = FXCollections.observableArrayList(Operation.SELECT, Operation.INSERT, Operation.UPDATE, Operation.DELETE);

	private DBconnection() {

		try {
			c = DriverManager.getConnection("jdbc:mysql://localhost:3306/cars", "root", "");
			System.out.println(Color.CYAN.value + "CONNECTION IS BUILT\n" + Color.RESET.value);

			metaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static DBconnection DB() {
		if (singleton == null)
			singleton = new DBconnection();
		return singleton;
	}

	public Connection getConnection() {
		return c;
	}

	private void metaData() {
		try (ResultSet tablesResultSet = c.getMetaData().getTables(DB_NAME, null, null, new String[] { "TABLE" })) {
			while (tablesResultSet.next()) {

				String tableName = tablesResultSet.getString("TABLE_NAME");
				ResultSet colsResultSet = c.getMetaData().getColumns(DB_NAME, null, tableName, null);

				tablesAndColumns.put(tableName, new ArrayList<>());
				while (colsResultSet.next()) {
					String colName = colsResultSet.getString("COLUMN_NAME");
					tablesAndColumns.get(tableName).add(colName);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public ObservableList<String> tableNames() {
		ObservableList<String> l = FXCollections.observableArrayList();
		l.addAll(tablesAndColumns.keySet());
		return l;
	}

	public List<String> columnsNames(String tableName) {
		return tablesAndColumns.get(tableName);
	}

	public ObservableList<Operation> getOperations() {
		return OPERATIONS;
	}

	public static String getDbname() {
		return DB_NAME;
	}
}
