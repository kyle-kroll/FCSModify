package com.kroll.fcsmodify;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;



public class PrimaryController implements Initializable{
    FCS fcs = new FCS();
    private ObservableList<FCSObject> fcsObj;
    
    @FXML
    private Label fcsVerLabel;
    @FXML
    private TableView<FCSObject> paramTable;
    @FXML 
    private TableColumn<FCSObject, String> paramColumn;
    @FXML
    private TableColumn<FCSObject, String> valueCol;

    public PrimaryController() {
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paramTable.setEditable(true);
    }
    
    @FXML
    private void closeApp(){
        Platform.exit();
        System.exit(0);
    }
    
    @FXML
    private void selectFCS(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(new Stage());
        fcs.setFilepath(file.getAbsolutePath());
        try{
            fcs.parseFCS();
            setTableData();
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    
    @FXML
    private void setTableData(){
        fcsVerLabel.setText("Version: " + fcs.getFCSVersion());
        paramColumn.setCellValueFactory(new PropertyValueFactory<FCSObject, String>("dkey"));
        valueCol.setCellValueFactory(new PropertyValueFactory<FCSObject, String>("dvalue"));
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(
            new EventHandler<CellEditEvent<FCSObject, String>>() {
                @Override
                public void handle(CellEditEvent<FCSObject, String> t) {
                    ((FCSObject) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setDvalue(t.getNewValue());
                }
            }
        );
        fcsObj = FXCollections.observableList(parseDataKeys());
        paramTable.getItems().addAll(fcsObj);
        
    }
    private List<FCSObject> parseDataKeys() {
        List<FCSObject> objects = new ArrayList<FCSObject>();
        for (String key: fcs.getText().keySet()) {
            FCSObject f = new FCSObject();
            f.setDkey(key);
            f.setDvalue(fcs.getText().get(key));
            
            objects.add(f);
        }
        return objects;
    }
    @FXML
    private void saveFCS(){
        for (FCSObject f: fcsObj) {
            try{
                fcs.setNewParameter(f.getDkey(), f.getDvalue());
            } catch (Exception e) {
                System.out.println(e);
            }
            
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save new FCS...");
        File file = fileChooser.showSaveDialog(new Stage());
        try {
            fcs.writeFCS(file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println(e);
        } 
        
    }
    
    @FXML
    private void testFunction() {
        for (String key: fcs.getText().keySet()) {
            System.out.printf("%s: %s\n", key, fcs.getText().get(key));
        }
    }

    @FXML
    private void showAboutDialog(){
        Stage popupwindow=new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("About FCSModify");
        Label label1= new Label("FCSModify v1.0");
        Label contactLabel = new Label("https://github.com/kyle-kroll/FCSModify");
        Button button1= new Button("Close");
        button1.setOnAction(e -> popupwindow.close());
        VBox layout= new VBox(10);
        layout.getChildren().addAll(label1,contactLabel, button1);
        layout.setAlignment(Pos.CENTER);
        Scene scene1= new Scene(layout, 250, 100);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }
    

}