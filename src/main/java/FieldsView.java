

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.naming.directory.NoSuchAttributeException;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class FieldsView {

	private static Pane buildSelectPane(Pane main, TableTab tab) {
		main.getChildren().clear();

		Label explanation = new Label("Search " + tab.getTableName() + " using fields:");
		GridPane gp = new GridPane();
		changeGridpane(gp, tab, true);
		gp.getStyleClass().add("grid-pane");

		main.getChildren().addAll(explanation, gp);
		return main;
	}

	private static Pane buildInsertPane(Pane main, TableTab tab) {
		main.getChildren().clear();

		Label explanation = new Label("Insert new row into " + tab.getTableName() + ". values:");
		GridPane gp = new GridPane();
		gp.getStyleClass().add("grid-pane");

		changeGridpane(gp, tab, false);
		main.getChildren().addAll(explanation, gp);
		return main;
	}

	private static Pane buildDeletePane(Pane main, TableTab tab) {
		main.getChildren().clear();

		Label explanation = new Label("Delete from " + tab.getTableName() + " using fields:");
		GridPane gp = new GridPane();
		gp.getStyleClass().add("grid-pane");
		changeGridpane(gp, tab, true);
		main.getChildren().addAll(explanation, gp);
		return main;
	}

	private static Pane buildUpdatePane(Pane main, TableTab tab) {
		main.getChildren().clear();
		Label explanation = new Label("Update Table " + tab.getTableName() + " by ID");
		
		try {
			ComboBox<?> cbpk = AutoCompleteCombobox.createComboBoxWithAutoCompletionSupport(tab.callPKAllowedValues());
			GridPane idGp = new GridPane();
			idGp.getStyleClass().add("grid-pane");
			
			idGp.addRow(0, new Label("Choose Row ID to Update: "), cbpk);

			GridPane gp = new GridPane();
			gp.getStyleClass().add("grid-pane");
			changeGridpane(gp, tab, true);

			main.getChildren().addAll(explanation, idGp, gp);
			return main;
		} catch (NoSuchAttributeException e) {
			HomeView.warning("No PK in Table. Cannot Update Rows, Choose Delete to Remove Rows.");
		}
		return main;
		
		
	}

	public static String[] extractValues(Pane fieldsView, boolean allowNull) throws IllegalStateException {
		if (!allowNull) {
			if (fieldsView.getChildren().stream().filter(c -> c instanceof GridPane)
					.flatMap(o -> ((GridPane) o).getChildren().stream()).filter(e -> e instanceof ComboBox)
					.map(e -> getValue(e)).anyMatch(e -> e == null)) {
				throw new IllegalStateException("Cannot operate on null values in the selected operation");
			}
		}
		
		List<String> l = fieldsView.getChildren().stream().filter(c -> c instanceof GridPane)
				.flatMap(o -> ((GridPane) o).getChildren().stream())
				.filter(e -> e instanceof TextField || e instanceof ComboBox)
				.map(e -> getValue(e))
				.collect(Collectors.toList());

		
		String[] values = l.toArray(new String[0]);
		return values;
	}

	public static String getValue(Node n) {
		if (n instanceof TextField) 
			return ((TextField) (n)).getText();
		else if (n instanceof ComboBox) 
			return ((ComboBox<?>) (n)).getValue() != null ? ((ComboBox<?>) (n)).getValue().toString() : "";
		
		return "";
	}

	private static void changeGridpane(GridPane gp, TableTab tab, boolean allowNull) {
		gp.getChildren().clear();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setAlignment(Pos.CENTER_RIGHT);

		UnaryOperator<String> toTitleCase = (String input) -> {
			if (input == null || input.isEmpty()) {
				return input;
			}

			StringBuilder titleCase = new StringBuilder();
			boolean nextTitleCase = true;

			for (char c : input.toCharArray()) {
				if (Character.isSpaceChar(c)) {
					nextTitleCase = true;
				} else if (nextTitleCase) {
					c = Character.toTitleCase(c);
					nextTitleCase = false;
				} else {
					c = Character.toLowerCase(c);
				}
				titleCase.append(c);
			}

			return titleCase.toString();
		};

		List<String> cols = DBconnection.DB().columnsNames(tab.getTableName());
		for (int i = 0; i < cols.size(); i++) {
			gp.addRow(i, new Label(toTitleCase.apply(cols.get(i))), inputType(tab, cols.get(i), allowNull)); // add object or node
		}

	}

	private static Node inputType(TableTab tab, String column, Boolean allowNull) { // if FK are originally null, does
		// this allow for
// adding
// disallowed FK here?
		Optional<ObservableList<String>> ol = Optional.ofNullable(tab.callFKAllowedValues(column));

		if (ol.isPresent()) {
			if (allowNull) {
				ol.get().add(0, "");
			}
			ComboBox<?> cb = AutoCompleteCombobox.createComboBoxWithAutoCompletionSupport(ol.get());
			cb.setPromptText("Type to filter List");
			return cb;

		}

		TextField t = new TextField();

		String[] typeAndSize = tab.getColumnType(column);
		t.setTextFormatter(
				new TextFormatter<Change>(formatter(typeAndSize[0], Integer.parseInt(typeAndSize[1]), column, t, allowNull)));
		return t;

	}

	private static UnaryOperator<Change> formatter(String type, int size, String column, TextField t, Boolean allowNull) {
		UnaryOperator<Change> u = (change) -> {
			// tab .get text type as [data type][]length
			// map the [][] to regex formatteer
			HomeView.noWarning();
			String regex = "";

			boolean flag = false;
			switch (type) {
			case "String":
				regex = ".{0," + size + "}";
				break;
			case "Long":
				regex = "^[+|-]?[0-9]{0," + size + "}";
				flag = true;
				break;
			case "Double":
				regex = "^[+|-]?\\d{0," + (size - 4) + "}[\\.]{0,1}\\d{0,4}";
				flag = true;
				break;
			case "Boolean":
				regex = "[true | false]";
				break;
			case "Date":

			}

			String newText = change.getControlNewText();
			if (newText.matches(regex)) {
				if(newText.equals("") && flag && !allowNull)
					t.setText("0");				
				return change;
			}

			HomeView.warning(t);
			HomeView.warning("Wrong Data Type");
			return null;

		};
		return u;

	}

	public static Pane buildDynamically(Pane main, Operation operation, TableTab tab) {

		switch (operation) {
		case SELECT:
			return buildSelectPane(main, tab);

		case INSERT:

			return buildInsertPane(main, tab);

		case UPDATE:

			return buildUpdatePane(main, tab);

		case DELETE:
			return buildDeletePane(main, tab);

		}

		return main;
	}
}
