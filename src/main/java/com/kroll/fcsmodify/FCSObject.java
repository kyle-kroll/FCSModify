/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kroll.fcsmodify;

import javafx.beans.property.SimpleObjectProperty;

public class FCSObject {
    public final SimpleObjectProperty<String> dkey = new SimpleObjectProperty<>("");
    //private String dataKey;
    public final SimpleObjectProperty<String> dvalue = new SimpleObjectProperty<>("");
    
    public SimpleObjectProperty<String> dkeyProperty() {
        return dkey;
    }
    public SimpleObjectProperty<String> dvalueProperty() {
        return dvalue;
    }
    public void setDkey(String k) {
        dkey.set(k);
    }
    
    public void setDvalue(String v) {
        dvalue.set(v);
    }
    
    public String getDkey(){
        return dkeyProperty().get();
    }
    
    public String getDvalue(){ 
        return dvalueProperty().get();
    }
    
}
