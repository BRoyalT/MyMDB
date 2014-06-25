package models;

import java.util.Properties;

public class DBProps {
  public static Properties getProps() {
    return mysql();
    //return sqlite();
  }
  
  static Properties mysql() {
    Properties db = new Properties();
    db.put("url", "jdbc:mysql://localhost/test");
    db.put("username", "guest");
    db.put("password", "");
    return db;
  }

  static Properties sqlite() {
    Properties db = new Properties();
    db.put("url", "jdbc:sqlite:database.sqlite");
    db.put("driver", "org.sqlite.JDBC");
    return db;
  }
}
