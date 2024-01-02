

import java.sql.SQLException;


@FunctionalInterface
public interface SqlExecuter<T> {

	public void apply(T t) throws IllegalStateException, SQLException;
}
