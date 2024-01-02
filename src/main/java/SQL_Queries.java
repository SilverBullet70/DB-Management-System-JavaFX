

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

interface SQL_Queries {

	public static String deleteQuery(String tableName, String[] colNames, String[] values) {

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM " + tableName + " WHERE (");

		int numCol = values.length;
		for (int i = 0; i < numCol; i++) {

			if (values[i].trim().isEmpty())
				continue;

			if (!sql.toString().endsWith("WHERE ("))
				sql.append(" AND ");

			sql.append(colNames[i] + " = \'" + values[i] + "\'");
		}

		sql.append(");");

		System.out.println(Color.GREEN.value + sql + Color.RESET.value);
		System.err.println("Row Deleted");

		return sql.toString();

	}

	public static String updateQuery(String id, String tableName, String[] colNames, String[] values) {

		// String SelectSQL = "SELECT * from " + tableName;
		String whereClause = " WHERE " + colNames[0] + " = \'" + id + "\';";

		// SelectSQL += whereClause;

		// System.out.println(SelectSQL);

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE " + tableName + " SET ");
		for (int i = 0; i < values.length; i++) {

			if (values[i].trim().isEmpty())
				continue;

			if (!sql.toString().endsWith("SET "))
				sql.append(", ");
			sql.append(colNames[i] + " = \'" + values[i] + "\'");

		}

		sql.append(whereClause);

		System.out.println(Color.GREEN.value + sql + Color.RESET.value);

		return sql.toString();

	}

	public static String insertQuery(String tableName, String[] values) {
		StringBuilder sql = new StringBuilder();

		sql.append("INSERT INTO " + tableName + " VALUES (");

		for (int i = 0; i < values.length; i++) {
			sql.append("\'" + values[i] + "\'");

			if (i < values.length - 1)
				sql.append(", ");
		}

		sql.append(");");

		System.out.println(Color.GREEN.value + sql + Color.RESET.value);
		return sql.toString();
	}

	public static String selectQuery(String tableName, String[] colNames, String[] values) {
		return selectByColumnsQuery(tableName, null, colNames, values);

	}

	public static String selectByColumnsQuery(String tableName, String[] selectedCols, String[] colNames,
			String[] values) {
		StringBuilder sql = new StringBuilder();
		if (selectedCols == null)
			sql.append("SELECT * ");
		else {
			sql.append("SELECT ");
			Optional<String> cols = Arrays.stream(selectedCols).reduce((e1, e2) -> e1 +", " + e2);	
			sql.append(cols.orElse(" * "));
		}
		sql.append(" FROM " + tableName);

		if (values != null) {
			boolean isFirstCondition = true;
			for (int i = 0; i < values.length; i++) {

				if (values[i] != null && !values[i].isEmpty()) {
					values[i].trim();
					if (isFirstCondition) {
						sql.append(" WHERE ");
						isFirstCondition = false;
					} else
						sql.append(" AND ");

					sql.append(colNames[i] + " = '" + values[i] + "'");
				}
			}
		}

		System.out.println(Color.GREEN.value + sql + Color.RESET.value);
		return sql.toString();
	}

	public static String updateToEmpty(String tableName, Map<String, String> colVal, List<String> newValues) {
		String whereClause = " WHERE ";
		for (Entry<String, String> entry : colVal.entrySet()) {
			whereClause += " " + entry.getKey() + " = \'" + entry.getValue() + "\' AND ";
		}
		whereClause = whereClause.substring(0, whereClause.length()-4);
		whereClause += ";";

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE " + tableName + " SET ");
		
		int i = 0;
		for(Entry<String, String> entry : colVal.entrySet()) {
			
			if (!sql.toString().endsWith("SET "))
				sql.append(", ");
			sql.append(entry.getKey() + " = \'" + newValues.get(i++) + "\'");
		}

		sql.append(whereClause);

		System.out.println(Color.GREEN.value + sql + Color.RESET.value);

		return sql.toString();
	}
}
