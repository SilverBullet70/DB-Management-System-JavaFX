/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author developer
 */


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.directory.NoSuchAttributeException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class Table implements SQL_Queries {

	private ObservableList<ObservableList<?>> data;
	private String tableName;
	private LinkedHashMap<String, int[]> columnsDataTypes = new LinkedHashMap<String, int[]>();
	private Connection c;
	private HashMap<String, String[]> foriegnKeysToOrigin = new HashMap<>();
	private List<String> primaryKey = new ArrayList<String>();

	public Table(String tableName) {
		super();
		this.tableName = tableName;
		connect();

		ResultSet rs;

		try {
			rs = c.getMetaData().getPrimaryKeys(DBconnection.getDbname(), tableName, tableName);
			while (rs.next())
				primaryKey.add(rs.getString("COLUMN_NAME"));
			System.out.println(Color.PURPLE.value + "[" + primaryKey.toString() + "] <-- IS A PRIMARY KEY IN TABLE ["
					+ tableName + "]" + Color.RESET.value);
			rs.close();

			rs = c.getMetaData().getImportedKeys(DBconnection.getDbname(), null, tableName);
			while (rs.next()) {
				foriegnKeysToOrigin.put(rs.getString("FKColumn_NAME"),
						new String[] { rs.getString("PKTABLE_NAME"), rs.getString("PKcolumn_NAME") });
				System.out.println(
						Color.YELLOW.value + "[" + rs.getString("FKColumn_NAME") + "] <-- IS A FOREIGN KEY IN TABLE ["
								+ tableName + "] FROM TABLE --> " + rs.getString("PKTABLE_NAME") + Color.RESET.value);
			}
			rs.close();

			System.out.println();

			rs = c.createStatement().executeQuery("SELECT * FROM " + tableName);

			int i = 1;
			while (i <= rs.getMetaData().getColumnCount()) {
				columnsDataTypes.put(rs.getMetaData().getColumnName(i),
						new int[] { rs.getMetaData().getColumnType(i), rs.getMetaData().getColumnDisplaySize(i) });
				i++;
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// CONNECTION DATABASE
	private void connect() {
		c = DBconnection.DB().getConnection();
	}

	public List<String> getPrimanryKeys() {
		return primaryKey;
	}

	public List<String> getAllowedValuesForFK(String colName) {

		try {
			String[] origin = foriegnKeysToOrigin.get(colName);
			if (origin == null)
				return (null);
			else {
				String sql = SQL_Queries.selectByColumnsQuery(origin[0], new String[] { origin[1] }, getColNames(),
						null);
				ResultSet rs;

				rs = c.createStatement().executeQuery(sql);
				ArrayList<String> allowed = new ArrayList<>();
				while (rs.next())
					allowed.add(rs.getString(1));
				return (allowed);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getAllowedValuesForPK() throws NoSuchAttributeException {

		try {

			String sql = SQL_Queries.selectByColumnsQuery(this.tableName, new String[] { this.primaryKey.get(0) },
					getColNames(), null);
			ResultSet rs;

			rs = c.createStatement().executeQuery(sql);
			ArrayList<String> allowed = new ArrayList<>();
			while (rs.next())
				allowed.add(rs.getString(1));
			return (allowed);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchAttributeException();
		}
		return null;
	}

	// BUILD TABLEVIEW WITH SELECT QUERY
	private void buildData(ResultSet rs) {

		data = FXCollections.observableArrayList();
		try {

			/**
			 * ****************************** Data added to ObservableList *
			 *******************************
			 */
			while (rs.next()) {

				// Iterate Row
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					// Iterate Column
					row.add(rs.getString(i));
				}
				System.out.println("Row [1] added " + row);
				data.add(row);

			}
			System.out.println();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on Building Data");
		}

	}

	public ObservableList<ObservableList<?>> getData() {
		return data;
	}

	public String[] getColNames() {
		return columnsDataTypes.keySet().toArray(new String[0]);
	}

	public int[] getColType(String col) {
		return columnsDataTypes.get(col);
	}

	// SEARCH USING SELECT QUERY
	public void searchData(String[] values) throws SQLException {

		String sql = SQL_Queries.selectQuery(tableName, getColNames(), values);
		ResultSet rs;

		rs = c.createStatement().executeQuery(sql);
		buildData(rs);

	}

	public void updateByID(String id, String[] values) throws SQLException {

		String sql = SQL_Queries.updateQuery(id, tableName, getColNames(), values);

		c.createStatement().executeUpdate(sql);
		searchData(null);

	}

	// INSERT DATA INTO TABLE
	public void addData(String[] values) throws SQLException {
		String sql = SQL_Queries.insertQuery(tableName, values);

		c.createStatement().executeUpdate(sql);
		searchData(null);
	}

	public void deleteData(String[] values) throws SQLException {

		String sql = SQL_Queries.deleteQuery(tableName, getColNames(), values);
		c.createStatement().executeUpdate(sql);
		searchData(null);

	}

	public void callEmptyRow(Map<String, String> colVal) throws SQLException {
		List<String> newValues = new ArrayList<String>();
		for (Entry<String, String> e : colVal.entrySet()) {
			if (!foriegnKeysToOrigin.keySet().contains(e.getKey())) {
				if (primaryKey.contains(e.getKey())) {
					newValues.add(e.getValue());
					continue;
				}
				System.out.println(columnsDataTypes.get(e.getKey())[0]);
				switch (columnsDataTypes.get(e.getKey())[0]) {
				

				case Types.REAL:
				case Types.FLOAT:
				case Types.NUMERIC:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
				case Types.BIGINT:
					newValues.add("0");
					break;
					
				case Types.DATE:
					newValues.add(e.getValue());
					break;
				default:
					newValues.add("");
				}
			} else {
				newValues.add(e.getValue());
			}
		}

		String sql = SQL_Queries.updateToEmpty(tableName, colVal, newValues);
		c.createStatement().executeUpdate(sql);
		searchData(null);

	}

}
