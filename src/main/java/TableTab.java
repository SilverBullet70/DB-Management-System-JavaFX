

import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.naming.directory.NoSuchAttributeException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


public class TableTab extends Tab {

	private String tableName;
	private Table table;
	private TableView<ObservableList<?>> tableView;

	// private TableColumn[] cols;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TableTab(String tableName) {
		super(tableName);
		super.setId(tableName);
		this.tableName = tableName;
		table = new Table(tableName);
		tableView = new TableView();

		for (int i = 0; i < table.getColNames().length; i++) {
			final int j = i;
			TableColumn col = new TableColumn(table.getColNames()[i]);
			col.setCellValueFactory((param) -> new SimpleStringProperty(
					((CellDataFeatures<ObservableList, String>) param).getValue().get(j).toString()));
			tableView.getColumns().add(col);
			col.getStyleClass().add("column");
		}
		super.setContent(tableView);
		

		tableView.setRowFactory(tv -> {
		    TableRow row = new TableRow<>();
			
			ContextMenu menu = new ContextMenu(new MenuItem("Delete non-key attributes Values"), new MenuItem("Delete Row"));
			

			row.setContextMenu(menu);
			menu.setOnAction( e -> {
				if(((MenuItem)e.getTarget()).getText() == "Delete non-key attributes Values") {
					ObservableList clickedRow = (ObservableList) row.getItem();
					Map<String, String> colVal = new LinkedHashMap<>();
			        IntStream.range(0, clickedRow.size())
			        		 .forEach(i -> colVal.put(table.getColNames()[i], (String) clickedRow.get(i)));
			        
			        
			        try {
						table.callEmptyRow(colVal);
						changeTabContent();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				
				if(((MenuItem)e.getTarget()).getText() == "Delete Row") {
					ObservableList clickedRow = (ObservableList) row.getItem();
					Map<String, String> colVal = new LinkedHashMap<>();
			        IntStream.range(0, clickedRow.size())
			        		 .forEach(i -> colVal.put(table.getColNames()[i], (String) clickedRow.get(i)));
			        
			        
			        try {
						this.callDelete(colVal.values().toArray(new String[0]));
						HomeView.inform("Deleted Row");
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			});
		    row.setOnMouseClicked(me -> {
		        if (! row.isEmpty() && me.getButton()==MouseButton.SECONDARY) {
	                row.getContextMenu().show(tableView, me.getSceneX(), me.getSceneY());
		            
		        }
		    });
		    return row ;
		});
	
		
		EventHandler<MouseEvent> event = new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent me) {
	            if (me.getButton() == MouseButton.SECONDARY) {
	            }
	        }
	    };
	    tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

	}

	public String getTableName() {
		return tableName;
	}

	public void callSelect(String[] values) throws SQLException {
		table.searchData(values);
		changeTabContent();
	}

	public void callUpdate(String id, String[] values) throws SQLException {
		table.updateByID(id, values);
		changeTabContent();
	}

	public void callDelete(String[] values) throws SQLException {
		table.deleteData(values);
		changeTabContent();
	}

	public void callInsert(String[] values) throws SQLException {
		table.addData(values);
		changeTabContent();
	}
	
	public ObservableList<String> callPKAllowedValues() throws NoSuchAttributeException{
		Optional<List<String>> l = Optional.ofNullable(table.getAllowedValuesForPK());
		if (l.isPresent()) {
			//l.get().add(0, "");
			return FXCollections.observableArrayList(l.get());
		}
		return null;
		
	}

	public ObservableList<String> callFKAllowedValues(String colName) {
		Optional<List<String>> l = Optional.ofNullable(table.getAllowedValuesForFK(colName));
		if (l.isPresent()) {
			//l.get().add(0, "");
			return FXCollections.observableArrayList(l.get());
		}
		return null;
	}
	
	public List<String> callPrimaryKeys() {
		return table.getPrimanryKeys();
	}

	public String[] getColumnType(String colName) {
		int[] type = table.getColType(colName);

		String result;

		switch (type[0]) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			result = "String";
			break;

		case Types.REAL:
		case Types.FLOAT:
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.DOUBLE:
			result = "Double";
			break;

		case Types.BIT:
			result = "Boolean";
			break;


		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
			result = "Long";
			break;

		case Types.DATE:
			result = "Date";
			break;

//		case Types.TIME:
//			result = java.sql.Time.class;
//			break;
//
//		case Types.TIMESTAMP:
//			result = java.sql.Timestamp.class;
//			break;
			
			default:
				result = "String";
		}
		
		return new String[] {result , type[1]+""};
	}

	private void changeTabContent() {
		tableView.getItems().clear();
		tableView.getItems().addAll(table.getData());


	}

	@Override
	public String toString() {
		return tableName;
	}
}
