package org.opencds.cqf.tooling.ui;

import java.io.File;
import java.io.IOException;

import org.opencds.cqf.tooling.operations.codesystem.loinc.HierarchyProcessor;
import org.opencds.cqf.tooling.operations.valueset.generate.config.Config.ValueSets;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class HierarchyUI extends Application {

   private final FhirContext fhirContext = FhirContext.forR4Cached();

   private final HierarchyProcessor processor = new HierarchyProcessor();
   private ValueSets config;

   private static final String ERROR_STYLE = "-fx-border-color: RED; -fx-border-width: 2; -fx-border-radius: 5;";

   public static void main(String[] args) {
      launch(args);
   }

   @Override
   public void start(Stage primaryStage) {
      processor.setFhirContext(fhirContext);
      processor.setVersion("r4");
      primaryStage.setScene(loginScene(primaryStage));
      primaryStage.show();
   }

   public Scene valueSetGeneratorScene(Stage primaryStage) {
      primaryStage.setTitle("Hierarchy ValueSet Generator");
      GridPane grid = new GridPane();
      grid.setAlignment(Pos.TOP_LEFT);
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(25, 25, 25, 25));

      int rowIdx = 0;

      Text scenetitle = new Text("Hierarchy ValueSet Generation Tool");
      scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
      grid.add(scenetitle, 0, rowIdx++, 2, 1);

      Button uploadConfigBtn = new Button("Upload Configuration");
      HBox hbConfigBtn = new HBox(10);
      hbConfigBtn.setAlignment(Pos.BOTTOM_LEFT);
      hbConfigBtn.getChildren().add(uploadConfigBtn);
      grid.add(hbConfigBtn, 0, rowIdx++, 2, 1);

      Label queryLabel = new Label("Query:");
      grid.add(queryLabel, 0, rowIdx);
      TextField queryTextField = new TextField();
      queryTextField.setPromptText("A valid LOINC query (e.g. multiaxial_descendantsof:LP18149-2 system:urine)");
      queryTextField.setPrefWidth(500);
      grid.add(queryTextField, 1, rowIdx++);
      Hyperlink link = new Hyperlink();
      link.setText("LOINC search syntax documentation");
      link.setOnAction(event -> getHostServices().showDocument("https://loinc.org/kb/search/overview/"));
      grid.add(link, 0, rowIdx++, 4, 1);

      Label idLabel = new Label("ID:");
      grid.add(idLabel, 0, rowIdx);
      TextField idTextField = new TextField();
      idTextField.setPromptText("The ID for the resulting ValueSet resource");
      grid.add(idTextField, 1, rowIdx++);

      Button getVsBtn = new Button("Get ValueSet");
      HBox hbBtn = new HBox(10);
      hbBtn.setAlignment(Pos.BOTTOM_LEFT);
      hbBtn.getChildren().add(getVsBtn);
      grid.add(hbBtn, 0, rowIdx++, 2, 1);

      TextArea output = new TextArea();
      output.setPrefHeight(500);
      output.setPrefWidth(800);
      output.setVisible(false);
      grid.add(output, 0, rowIdx, 4, 8);

      uploadConfigBtn.setOnAction(e -> {
         FileChooser configUpload = new FileChooser();
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Select a configuration file (*.json)", "*.json");
         configUpload.getExtensionFilters().add(extensionFilter);
         File configFile = configUpload.showOpenDialog(new Stage());
         if (configFile != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
               setConfig(mapper.readValue(configFile, ValueSets.class));
               queryTextField.setText(config.getHierarchy().getQuery());
               idTextField.setText(config.getId());
            } catch (IOException ex) {
               throw new RuntimeException("Error mapping configuration", ex);
            }
         }
      });


      getVsBtn.setOnAction(e -> {
         if (queryTextField.getText().isBlank() || idTextField.getText().isBlank()) {
            queryTextField.setStyle(queryTextField.getText().isBlank() ? ERROR_STYLE : "");
            idTextField.setStyle(idTextField.getText().isBlank() ? ERROR_STYLE : "");
         } else {
            processor.setQuery(queryTextField.getText());
            processor.setId(idTextField.getText());
            output.setText(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(
                    config == null ? processor.getValueSet() : processor.getValueSet(config)));
            output.setVisible(true);
         }
      });

      return new Scene(grid, 1000, 1000);
   }

   public Scene loginScene(Stage primaryStage) {
      primaryStage.setTitle("LOINC Credentials");
      GridPane grid = new GridPane();
      grid.setAlignment(Pos.CENTER);
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(25, 25, 25, 25));

      Text scenetitle = new Text("Please provide valid LOINC credentials");
      scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
      grid.add(scenetitle, 0, 0, 2, 1);

      Label userName = new Label("User Name:");
      grid.add(userName, 0, 1);

      TextField userTextField = new TextField();
      grid.add(userTextField, 1, 1);

      Label pw = new Label("Password:");
      grid.add(pw, 0, 2);

      PasswordField pwBox = new PasswordField();
      grid.add(pwBox, 1, 2);

      Button btn = new Button("Validate");
      HBox hbBtn = new HBox(10);
      hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
      hbBtn.getChildren().add(btn);
      grid.add(hbBtn, 1, 4);

      btn.setOnAction(e -> {
         if (userTextField.getText().isBlank() || pwBox.getText().isBlank()) {
            userTextField.setStyle(userTextField.getText().isBlank() ? ERROR_STYLE : "");
            pwBox.setStyle(pwBox.getText().isBlank() ? ERROR_STYLE : "");
         } else {
            processor.setUsername(userTextField.getText());
            processor.setPassword(pwBox.getText());
            primaryStage.close();
            primaryStage.setScene(valueSetGeneratorScene(primaryStage));
            primaryStage.show();
         }
      });

      return new Scene(grid, 500, 275);
   }

   private void setConfig(ValueSets config) {
      this.config = config;
   }
}
