

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeView extends Application {

	private static List<Node> warningNode = new ArrayList<Node>();
	private static Label warnings = new Label();
	private static Label info = new Label();
	private Map<String, TableTab> tabs = new HashMap<String, TableTab>();
	private VBox fieldsPane = new VBox();

	// SQL Queries callers
	SqlExecuter<TableTab> selectExecuter = (TableTab tab) -> {
		if (tab == null) {
			warning("Select a Table First");
			return;
		}
		String[] values = FieldsView.extractValues(fieldsPane, true);
		tab.callSelect(values);
	};

	SqlExecuter<TableTab> insertExecutor = (TableTab tab) -> {
		if (tab == null) {
			warning("Select a Table First");
			return;
		}
		String[] values = FieldsView.extractValues(fieldsPane, false);
		System.err.println(Arrays.asList(values));
		if (isAllNull(values)) {
			warning("Enter Values to Insert. Cannot Insert Empty Row.");
			return;
		}

		tab.callPrimaryKeys().stream().forEach(pk -> {
			String value = values[DBconnection.DB().columnsNames(tab.getTableName()).indexOf(pk)];
			if (value == null || value.trim().isEmpty()) {
				warning("[" + pk + "] Connot Be Empty.");
			}
		});

		tab.callInsert(values);
		inform("One Entry Inserted");
	};

	SqlExecuter<TableTab> updateExecutor = (TableTab tab) -> {
		if (tab == null) {
			warning("Select a Table First");
			return;
		}
		String[] values = FieldsView.extractValues(fieldsPane, false);
		if (isAllNull(values)) {
			warning("Enter Values to Update. Cannot Update to Empty Row.\n(try to right click on target row)");
			return;
		}
		if (values[0] == null || values[0].isEmpty()) {
			warning("ID Cannot be Empty. Select Row ID to Update.");
			return;
		}

		tab.callUpdate(values[0], Arrays.copyOfRange(values, 1, values.length));
		inform("Entry With ID " + values[0] + " was Updated");
		FieldsView.buildDynamically(fieldsPane, Operation.UPDATE, tab);
	};

	SqlExecuter<TableTab> deleteExecutor = (TableTab tab) -> {
		if (tab == null) {
			warning("Select a Table First");
			return;
		}
		String[] values = FieldsView.extractValues(fieldsPane, true);

		if (isAllNull(values)) {
			warning("Enter Values to Delete. Cannot Delete Empty Row.");
			return;
		}

		Alert alert = new Alert(AlertType.CONFIRMATION,
				"You are about to Delete one or more rows including all the rows related to the deleted rows."
						+ "\nTo delete one row make sure you inserted its ID." + "\nDo You Want to Continue?");
		alert.setHeaderText("Delete Warning");
		alert.setTitle("Delete Warning");
		Optional<ButtonType> choice = alert.showAndWait();
		if (choice.isPresent()) {
			if (choice.get() == ButtonType.OK) {
				tab.callDelete(values);
			}
		}
		inform("Deleted Zero or More Entries");

	};

	public boolean isAllNull(String[] arr) {
		boolean isNull = true;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null || arr[i].trim().isEmpty()) {
				System.err.println(arr[i]);
				continue;
			} else {
				isNull = false;
				break;
			}

		}
		return isNull;
	}

	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Label description = new Label("\u2753");
		description.getStyleClass().add("description");

		// Create tabPane on the left
		TabPane tabPane = new TabPane();
		tabPane.getStyleClass().add("tab-pane");
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		// Initialize tabs map <tableName, tab>
		DBconnection.DB().tableNames().stream().forEach(e -> tabs.put(e, new TableTab(e)));
		tabPane.getTabs().addAll(tabs.values());

		// Operation combobox, tables combobox
		ComboBox<Operation> cbOperations = new ComboBox<>(DBconnection.DB().getOperations());
		ComboBox<String> cbTables = new ComboBox<>(DBconnection.DB().tableNames());

		// Initialize to Select
		cbOperations.setValue(Operation.SELECT);
		description.setOnMouseClicked(e -> {
			Alert alert = new Alert(AlertType.INFORMATION, getDescription(Operation.SELECT));
			alert.setHeaderText("How to use Select Operation?");
			alert.setTitle("About Select Operation");
			alert.showAndWait();

		});

		// set title
		Label title = new Label(DBconnection.getDbname() + " Database System");
		title.getStyleClass().add("title");
		
		// set Hbox for header that includes comboboxes
		HBox header = new HBox(20, new VBox(5, new Label("Operation: "), cbOperations),
				new VBox(5, new Label("On table: "), cbTables));
		header.getStyleClass().add("header");

		fieldsPane.getStyleClass().add("fields-pane");

		// main button
		Button apply = new Button("Search Table");
		apply.setOnAction(e -> excpetionCatcher(selectExecuter, tabs.get(cbTables.getValue())));

		// event for when table selection changes //TODO: recheck
		cbTables.valueProperty().addListener((composant, oldValue, newValue) -> {
			noWarning();
			FieldsView.buildDynamically(fieldsPane, cbOperations.getValue(), tabs.get(newValue));
			tabPane.getSelectionModel().select(tabs.get(newValue));

			excpetionCatcher(selectExecuter, tabs.get(newValue));

		});

		// event for when operation selection changes //TODO: recheck
		cbOperations.valueProperty().addListener((composant, oldValue, newValue) -> {
			noWarning();
			if (cbTables.getValue() != null)
				FieldsView.buildDynamically(fieldsPane, newValue, tabs.get(cbTables.getValue()));

			switch (newValue) {
			case SELECT:
				apply.setText("Search Table");
				apply.setOnAction(e -> excpetionCatcher(selectExecuter, tabs.get(cbTables.getValue())));
				description.setOnMouseClicked(e -> {
					Alert alert = new Alert(AlertType.INFORMATION, getDescription(Operation.SELECT));
					alert.setHeaderText("How to use Select Operation?");
					alert.setTitle("About Select Operation");
					alert.showAndWait();

				});
				break;

			case INSERT:
				apply.setText("Insert Row");
				apply.setOnAction(e -> excpetionCatcher(insertExecutor, tabs.get(cbTables.getValue())));
				description.setOnMouseClicked(e -> {
					Alert alert = new Alert(AlertType.INFORMATION, getDescription(Operation.INSERT));
					alert.setHeaderText("How to use Insert Operation?");
					alert.setTitle("About Insert Operation");
					alert.showAndWait();

				});
				break;

			case UPDATE:
				apply.setText("Update Row");
				apply.setOnAction(e -> excpetionCatcher(updateExecutor, tabs.get(cbTables.getValue())));
				description.setOnMouseClicked(e -> {
					Alert alert = new Alert(AlertType.INFORMATION, getDescription(Operation.UPDATE));
					alert.setHeaderText("How to use Update Operation?");
					alert.setTitle("About Update Operation");
					alert.showAndWait();

				});
				break;

			case DELETE:
				apply.setText("Delete Values");
				apply.setOnAction(e -> excpetionCatcher(deleteExecutor, tabs.get(cbTables.getValue())));
				description.setOnMouseClicked(e -> {
					Alert alert = new Alert(AlertType.INFORMATION, getDescription(Operation.DELETE));
					alert.setHeaderText("How to use Delete Operation?");
					alert.setTitle("About Delete Operation");
					alert.showAndWait();

				});
				break;
			}
		});
		Label signiture = new Label("\u00a9");
		signiture.getStyleClass().add("signiture");
		signiture.setOnMouseClicked(e -> {
			Alert alert = new Alert(AlertType.INFORMATION,
					"This JavaFX application is developped by Angela Salem\n" + "email: angel.salem.008@gmail.com\n\n"
							+ "This application is a Dynamic Database Controller System, it builds data dynamically "
							+ "from any database it it connected to.\n"
							+ "the System supports 4 operations, Serach, Update, Insert, Delete.\n"
							+ "it also facilitates some processes by using right click on Tables.");
			alert.setHeaderText("System Development Credits");
			alert.setTitle("Credits");
			alert.showAndWait();

		});
		HBox h = new HBox(signiture);
		h.setAlignment(Pos.BOTTOM_RIGHT);

		HBox h1 = new HBox(description);
		h1.setAlignment(Pos.TOP_RIGHT);
		// declaring right pane
		VBox rightPane = new VBox(0, h1, title, header, fieldsPane, info, warnings, apply, h);
		rightPane.getStyleClass().add("right-pane");

		// scene
		HBox hBox = new HBox(tabPane, rightPane);
		Scene scene = new Scene(hBox);

		warnings.getStyleClass().add("warning");
		info.getStyleClass().add("info");

		File file = new File("src/main/resources/styles.css");
		scene.getStylesheets().add(file.toURI().toURL().toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Database Managment System");

		// BUILDIN LEFT PANE
		tabs.values().stream().forEach(e -> {
			System.out.println(Color.RED.value + e + Color.RESET.value);
			// String[] values = FieldsView.extractValues(fieldsPane);
			excpetionCatcher(selectExecuter, e);
		});

		// ICON
		primaryStage.getIcons().add(new Image(HomeView.class.getResourceAsStream("DB_ICON.png")));
		primaryStage.show();
	}

	private String getDescription(Operation op) {
		switch (op) {
		case SELECT:

			return "Choose Select Operation from dropdown menu, and choose a table to search its values"
					+ "\nthen keep values empty to ignore them from the filteration process, or "
					+ "type values to show specific rows. some vlaues are only possible to select them from"
					+ " the dorpdown menu because they refrence another table. You can however, type in the dropdown"
					+ " menu to filter the list. finally click Search to see results.\n"
					+ "\nNote: if you leave all fields empty, it won't filter data and select will show evrything.";
		case INSERT:

			return "Choose Insert Operation from dropdown menu, and choose a table to Insert a new row to."
					+ "\ntype in proper values to be inserted. avoid inserting empty values. Also make sure to insert"
					+ " a unique ID. Note that some fields have a dropdown menu, their data should only refrence a set "
					+ "of data in another table. If you want to insert a new values to the list, you shall first "
					+ "go to the refrenced table and insert the value there. If fields are left null, and they fon't refrence"
					+ " a key, they will be inserted as null.";
		case UPDATE:

			return "Choose Update Operation from dropdown menu, and choose a table to Update a row by its id."
					+ "\nto Update a Row value including its ID, choose the row ID you want to update first, "
					+ "then type in new values. leaving some fields empty will not affect the row data. In other words, the "
					+ "system ignore empty fields on update and will only change the filled data. if you want to empty a "
					+ "field, right click on the row in the table view and select to empty non-key attributes."
					+ " This will remove unrefrenced data from that row.";
		case DELETE:
			return "Choose Delete Operation from dropdown menu, and choose a table to Delete data from."
					+ "\nuse the fields to enter what data you want to delete from table. Note that deleting will"
					+ " remove rows from table not empty a specidfic cell. it might also delete connected rows in other "
					+ "tables. If you want to deleted on row, either make sure to enter its ID, ot right clcik on it and select "
					+ "delete row. If you leave the Id field empty, it is likely that it will delete multiple rows with the same data.";
		default:
			return "";
		}
	}

	private void excpetionCatcher(SqlExecuter<TableTab> s, TableTab t) {
		try {

			s.apply(t);

		}

		catch (IllegalStateException e) {
			warning(e.getMessage());
		} catch (SQLIntegrityConstraintViolationException e) {
			if (e.getMessage().contains("foreign key"))
				warning("Foreign Keys cannot be null. Choose a proper Foreign Key from List.");
			else if (e.getMessage().contains("Duplicate entry"))
				warning("duplicate key. " + t.callPrimaryKeys() + " must be unique");

		} catch (MysqlDataTruncation e) {
			warning("Invalid data Entry. Make sure not to insert Empty or Invalid values");
		} catch (SQLException e) {
			 warning("Invalid data Entry. Make sure not to enter empty values");
			//e.printStackTrace();
		}
	}

	// applies warnings
	public static void warning(String msg) {
		info.setVisible(false);
		warnings.setVisible(true);
		warnings.setText("\uD83D\uDEC8 " + msg);
	}

	public static void inform(String msg) {
		warnings.setVisible(false);
		info.setVisible(true);
		info.setText("\uD83D\uDEC8 " + msg);
	}

	public static void warning(Node n) {
		n.getStyleClass().add("warning-node");
		warningNode.add(n);
	}

	public static void noWarning() {
		warningNode.stream().forEach(node -> node.getStyleClass().removeAll("warning-node"));
		warningNode.clear();
		warnings.setText("");
		info.setText("");
		warnings.setVisible(false);
		info.setVisible(false);

	}
}
