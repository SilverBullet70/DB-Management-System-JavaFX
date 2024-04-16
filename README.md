# <center>  DB-Management-System-JavaFX </center>

This Project is a ***Dynamic Relational Database Control System***. It allows you to Search the database tables, insert values into tables, update and delete table rows. The system can be configured to operate on any `MySQL` Database (check [configuration](#Optional-Configuraiton) section for more details)

### Tools used
- `maven` as a build tool
- `jdbc` connector driver
- `MySQL` Database
- `JavaFX` For GUI interface structure
- `CSS` For skinning the application

# Main Features
### Queries
1. **Search (SELECT query)**

    This allows you to find a row in a table by all or any combination of it's fields.

2. **Insertion (INSERT query)**

    Users can insert a new row in the table, taking into consideration foreign keys refrences, and null constraints.

3. **Updating tuples (UPDATE query)**

    any value in the table can be updating by the row id

4. **Deleting values (DELETE query)**

    this allows you to delete any value by cascading, or any row.

The system also includes in-app detailed explanation. (check [usage](#Usage) section for extra information)

### Validaiton
The system validates for input constraints i.e it covers the folowing scenarios:
1. checking that the inserted values are os the same type and lenght defines in the database
2. checking that the primary key is not null and is unique
3. the system also checks that the fk in a table represesnts a pk in he refrenced table.
4.  ensures that an update or a delete operation cascades on the other tables

The system also provide detailed errors and confirmation messages upon each operation to provide a good uer experience.

# Installation

## 1. Clone the project
``` bash
git clone https://github.com/SilverBullet70/DB-Management-System-JavaFX.git
```
## 2. Connect to a `MySQL` DB

  The project has a sample `cars.sql` database file, in the resources folder of this project, yet, check the [next](#optional-configuraiton) section to learn how to configure your own.

  Ensure that MySQL is installed on your machine and is up and running on port `3306` then **Create database and import data** either by:

### A. *Using Command Line Interface*

Open MySQL environment command prompt and log in to MySQL Server as the root user with no password:

``` bash
mysql -u root -e "create database cars;"
mysql -u root -p cars < path/to/cars.sql
```
`cars.sql` can be found in the `resourses` folder

### B. *Using phpMyAdmin interface*

create a schema called `cars` and import the `cars.sql` file from the resources to the new DB.

## 3. Run the project through terminal
``` bash
mvn clean install
mvn clean javafx:run
```


> ### Optional Configuraiton
> 
> the database configuration can be changed from the class `DBconnection.java`.
> Change the following line to match the path to the DB, the username `root` and/or the password.
> ``` java
> c = DriverManager.getConnection("jdbc:mysql://localhost:3306/cars", "root", "");
> ```
> then repeat the previous installation steps to match the new schema name and MySQL login credentials.

# Usage
![image](https://github.com/SilverBullet70/DB-Management-System-JavaFX/assets/64022101/2d12691f-0261-4aaf-942f-23428bd86439)
![image](https://github.com/SilverBullet70/DB-Management-System-JavaFX/assets/64022101/65854d51-2f7f-4b43-a785-9650c6d519bb)



## Credits
This project is developed by Angela Salem, as part of 3rd Fall semester projects.
