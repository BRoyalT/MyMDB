
package mymdb;

import models.*;

public class Show {
    public static void main(String[] args) {
        try {
            ORM.init(DBProps.getProps());
            
            System.out.println(ORM.getUrl());
            
            for (Model m: ORM.findAll(Actor.class)){
                System.out.println(m);
            }
            
            for (Model m: ORM.findAll(Movie.class)){
                System.out.println(m);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
