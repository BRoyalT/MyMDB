
package models;

import java.sql.*;

public final class Actor extends Model {
    
  public static final String table = "actor";
   
  private int id = 0;
  private String name;
  private int birthYear;

  public Actor() { }

  public Actor(String name, int birthYear) {
    this.name = name;
    this.birthYear = birthYear;
  }
  
  @Override
  public int getId() { return id; }
  public String getName() { return name; }
  public int getBirthYear() { return birthYear; }

  public void setName(String name) {this.name = name; }
  public void setBirthYear(int birthYear) { this.birthYear = birthYear; }
  
  @Override
  void load(ResultSet rs) throws Exception {
    id = rs.getInt("id");
    name = rs.getString("name");
    birthYear = rs.getInt("birthYear");
  }
 
  @Override
  void insert() throws Exception {
    Connection cx = ORM.connection();
    String sql = String.format(
      "insert into %s (name,birthYear) values (?,?)", table);
    PreparedStatement st = cx.prepareStatement(sql);
    int i = 0;
    st.setString(++i, name);
    st.setInt(++i, birthYear);
    st.executeUpdate();
    id = ORM.getMaxId(table);
  }

  @Override
  void update() throws Exception {
    Connection cx = ORM.connection();
    String sql = String.format(
            "update %s set name=?,birthYear=? where id=?", table);
    PreparedStatement st = cx.prepareStatement(sql);
    int i = 0;
    st.setString(++i, name);
    st.setInt(++i, birthYear);
    st.setInt(++i,id);
    st.executeUpdate();
  }
    
}
