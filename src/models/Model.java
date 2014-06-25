package models;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Map;
import java.util.LinkedHashMap;

public abstract class Model {
  public int getId() { return 0; }
  
  /* set the object from a ResultSet table row */
  void load(ResultSet rs) throws Exception { }
  
  /* create a new record from the property values (minus id) */
  void insert() throws Exception { }
  
  /* modify an existing record (non-zero id) with property values */
  void update() throws Exception { }
  
  @Override
  public boolean equals(Object obj) {
    if (!this.getClass().isInstance(obj)) {
      return false;
    }
    return getId() == ((Model)obj).getId();
  }

  @Override
  public int hashCode() {
    return getId();
  }
   
  /* retrieve the exposed properties (other than "class")
   * determine the value of the property (through the getter)
   * and store the property/value pair into a Map
   */
  private Map<String,Object> beanValues() throws Exception {
    Map<String,Object> values = new LinkedHashMap<>();
    for (PropertyDescriptor pd : 
         Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
      String property = pd.getShortDescription();
      if (property.equals("class")) { 
        continue; 
      }
      Method m = pd.getReadMethod();  
      values.put(property, m.invoke(this));
    }
    return values;
  }
  
  @Override
  @SuppressWarnings("CallToThreadDumpStack")
  public String toString() {
    String ret = "*** " + getClass().getSimpleName() + " ***";
    try {
      for(Map.Entry<String,Object> value: this.beanValues().entrySet()) {
        ret += "\n" + value.getKey() + ": " + value.getValue();        
      }
      return ret;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
