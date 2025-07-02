// This is a JavaFX Resume Maker app with 3 steps:
// Step 1: Field Entry -> Step 2: Styling Options -> Step 3: Template Selection -> Export to PDF (with profile photo support and stylish layout)

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Separator;

import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ResumeMakerApp extends Application {
    private TextArea objectiveArea;
    private VBox educationBox = new VBox(5);
    private VBox skillsBox = new VBox(5);
    private VBox projectBox = new VBox(5);
    private VBox internshipBox = new VBox(5);
    private VBox experienceBox = new VBox(5);

    private StackPane rootStack;
    private VBox formPane;
    private VBox stylingPane;
    private VBox templatePane;
    private VBox previewBox;
    private Scene scene;

    private Map<String, javafx.scene.control.TextField> fields = new LinkedHashMap<>();
    private File profileImageFile;

    private ColorPicker colorPicker;
    private ComboBox<String> fontStyleBox;
    private Spinner<Integer> fontSizeSpinner;
    private Spinner<Integer> marginSpinner;
    private CheckBox boldCheck;
    private CheckBox italicCheck;
    private ComboBox<String> templateSelector;

    private Button saveBtn;
    private Button loadBtn;

    @Override
public void start(Stage primaryStage) {
    rootStack = new StackPane();

    setupFormPane(); // Sets up the VBox with all form fields

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(formPane);
    scrollPane.setFitToWidth(true); // Make formPane match scrollPane width
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setPadding(new Insets(10));

    // Optional: Set preferred height on formPane if not already set
    formPane.setPrefHeight(1200); // ensure content is tall enough to scroll

    rootStack.getChildren().add(scrollPane);

    scene = new Scene(rootStack, 800, 600); // Final size
    primaryStage.setTitle("Resume Maker App");
    primaryStage.setScene(scene);
    primaryStage.show();
}



    private void saveDraft() {
        ResumeData data = new ResumeData();

        // Basic fields
        data.fullName = fields.get("Full Name").getText();
        data.email = fields.get("Email").getText();
        data.phone = fields.get("Phone").getText();

        // Education extraction
        for (javafx.scene.Node node : educationBox.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                if (row.getChildren().size() >= 5) {
                    String[] entry = new String[] {
                        ((TextField) row.getChildren().get(0)).getText(), // course
                        ((TextField) row.getChildren().get(1)).getText(), // stream
                        ((TextField) row.getChildren().get(2)).getText(), // institute
                        ((TextField) row.getChildren().get(3)).getText(), // grade
                        ((TextField) row.getChildren().get(4)).getText()  // year
                    };
                    data.education.add(entry);
                }
            }
        }

        // Skills, Projects, Internships, Experience
        extractTextList(skillsBox, data.skills);
        extractTextList(projectBox, data.projects);
        extractTextList(internshipBox, data.internships);
        extractTextList(experienceBox, data.experience);

        // Custom fields
        for (Map.Entry<String, TextField> entry : fields.entrySet()) {
            String key = entry.getKey();
            if (!List.of("Full Name", "Email", "Phone").contains(key)) {
                data.customFields.put(key, entry.getValue().getText());
            }
        }

        // Profile photo path
        if (profileImageFile != null) {
            data.profilePhotoPath = profileImageFile.getAbsolutePath();
        }

        // Styling options
        if (colorPicker != null) {
            data.fontColor = colorPicker.getValue().toString();
        }
        if (fontStyleBox != null) {
            data.fontStyle = fontStyleBox.getValue();
        }
        if (fontSizeSpinner != null) {
            data.fontSize = fontSizeSpinner.getValue();
        }
        if (marginSpinner != null) {
            data.margin = marginSpinner.getValue();
        }
        if (boldCheck != null) {
            data.bold = boldCheck.isSelected();
        }
        if (italicCheck != null) {
            data.italic = italicCheck.isSelected();
        }

        // Template
        if (templateSelector != null) {
            data.selectedTemplate = templateSelector.getValue();
        }

        // Save as JSON
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Resume Draft");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showSaveDialog(null);

        if (file != null) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(gson.toJson(data).getBytes());
                fos.close();
                showAlert("Success", "Resume draft saved successfully.");
            } catch (Exception ex) {
                showAlert("Error", "Failed to save draft:\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void loadDraft() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Draft");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            try {
                Gson gson = new Gson();
                ResumeData data = gson.fromJson(new FileReader(file), ResumeData.class);

                // Basic fields
                if (data.fullName != null) fields.get("Full Name").setText(data.fullName);
                if (data.email != null) fields.get("Email").setText(data.email);
                if (data.phone != null) fields.get("Phone").setText(data.phone);

                // Custom fields
                for (Map.Entry<String, String> entry : data.customFields.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (fields.containsKey(k)) {
                        fields.get(k).setText(v);
                    } else {
                        TextField tf = new TextField(v);
                        fields.put(k, tf);
                        formPane.getChildren().add(formPane.getChildren().size() - 1, new Label(k));
                        formPane.getChildren().add(formPane.getChildren().size() - 1, tf);
                    }
                }

                // Clear existing
                educationBox.getChildren().clear();
                skillsBox.getChildren().clear();
                internshipBox.getChildren().clear();
                experienceBox.getChildren().clear();
                projectBox.getChildren().clear();

                // Education
                for (String[] edu : data.education) {
                    TextField course = new TextField(edu.length > 0 ? edu[0] : "");
                    TextField stream = new TextField(edu.length > 1 ? edu[1] : "");
                    TextField institute = new TextField(edu.length > 2 ? edu[2] : "");
                    TextField grade = new TextField(edu.length > 3 ? edu[3] : "");
                    TextField year = new TextField(edu.length > 4 ? edu[4] : "");
                    
                    course.setPromptText("Course Name");
                    stream.setPromptText("Stream");
                    institute.setPromptText("Institute");
                    grade.setPromptText("Grade/Score");
                    year.setPromptText("Year of Passing");
                    
                    HBox row = new HBox(10, course, stream, institute, grade, year);
                    educationBox.getChildren().add(row);
                }

                // Restore other dynamic sections
                populateTextList(skillsBox, data.skills, "Skills info");
                populateTextList(internshipBox, data.internships, "Internship info");
                populateTextList(experienceBox, data.experience, "Experience info");
                populateTextList(projectBox, data.projects, "Projects info");

                // Profile image
                if (data.profilePhotoPath != null) {
                    File img = new File(data.profilePhotoPath);
                    if (img.exists()) {
                        profileImageFile = img;
                    }
                }

                // Styling options
                if (colorPicker != null && data.fontColor != null) {
                    try {
                        colorPicker.setValue(Color.valueOf(data.fontColor));
                    } catch (Exception e) {
                        // ignore invalid color
                    }
                }
                if (fontStyleBox != null && data.fontStyle != null) {
                    fontStyleBox.setValue(data.fontStyle);
                }
                if (fontSizeSpinner != null && data.fontSize != null) {
                    fontSizeSpinner.getValueFactory().setValue(data.fontSize);
                }
                if (marginSpinner != null && data.margin != null) {
                    marginSpinner.getValueFactory().setValue(data.margin);
                }
                if (boldCheck != null) {
                    boldCheck.setSelected(data.bold);
                }
                if (italicCheck != null) {
                    italicCheck.setSelected(data.italic);
                }
                if (templateSelector != null && data.selectedTemplate != null) {
                    templateSelector.setValue(data.selectedTemplate);
                }

                showAlert("Loaded", "Resume draft loaded successfully!");
                if (previewBox != null) {
                    updatePreview();
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to load draft:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void extractTextList(VBox box, List<String> list) {
        for (javafx.scene.Node node : box.getChildren()) {
            if (node instanceof TextField) {
                TextField tf = (TextField) node;
                list.add(tf.getText());
            }
        }
    }

    private void populateTextList(VBox box, List<String> list, String promptText) {
        for (String item : list) {
            TextField tf = new TextField(item);
            tf.setPromptText(promptText);
            box.getChildren().add(tf);
        }
    }

    private void setupFormPane() {
        formPane = new VBox(10);
        formPane.setPadding(new Insets(15));
        formPane.setPrefWidth(700);

        String[] staticFields = {"Full Name", "Email", "Phone"};
        for (String field : staticFields) {
            TextField tf = new TextField();
            tf.setPromptText(field);
            fields.put(field, tf);
            tf.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
            formPane.getChildren().addAll(new Label(field), tf);
        }

         // ✅ Objective field (TextArea)
        Label objLabel = new Label("Objective");
        objectiveArea = new TextArea();
        objectiveArea.setPromptText("Write your career objective...");
        objectiveArea.setPrefRowCount(3);
        objectiveArea.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        formPane.getChildren().addAll(objLabel, objectiveArea);
        // Clear existing content
        educationBox.getChildren().clear();
        skillsBox.getChildren().clear();
        internshipBox.getChildren().clear();
        experienceBox.getChildren().clear();
        projectBox.getChildren().clear();

        formPane.getChildren().addAll(
            createDynamicSection("Education", educationBox),
            createDynamicSection("Skills", skillsBox),
            createDynamicSection("Internship", internshipBox),
            createDynamicSection("Experience", experienceBox),
            createDynamicSection("Projects", projectBox)
        );

        Button photoBtn = new Button("Upload Photo");
        photoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Profile Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg"));
            profileImageFile = chooser.showOpenDialog(null);
            updatePreview();
        });

        Button addCustomFieldBtn = new Button("Add Custom Field");
        addCustomFieldBtn.setOnAction(e ->{
            addCustomField();
            updatePreview();
        });

        formPane.getChildren().addAll(photoBtn, addCustomFieldBtn);

        // Initialize save and load buttons
        saveBtn = new Button("Save Draft");
        loadBtn = new Button("Load Draft");
        
        saveBtn.setOnAction(e -> saveDraft());
        loadBtn.setOnAction(e -> loadDraft());

        Button nextBtn = new Button("Next");
        nextBtn.setOnAction(e -> {
            setupStylingPane();
            rootStack.getChildren().setAll(stylingPane);
        });

        HBox navButtons = new HBox(10, saveBtn, loadBtn, nextBtn);
        navButtons.setPadding(new Insets(10));
        formPane.getChildren().add(navButtons);
    }

    private void setupStylingPane() {
        stylingPane = new VBox(10);
        stylingPane.setPadding(new Insets(15));

        colorPicker = new ColorPicker(Color.BLACK);
        fontStyleBox = new ComboBox<>();
        fontStyleBox.getItems().addAll("Arial", "Times New Roman", "Verdana", "Tahoma", "Courier New", "Georgia", "Comic Sans MS", "Trebuchet MS", "Impact", "Lucida Console");
        fontStyleBox.setValue("Arial");

        fontSizeSpinner = new Spinner<>(8, 48, 12);
        marginSpinner = new Spinner<>(0, 100, 20);
        boldCheck = new CheckBox("Bold");
        italicCheck = new CheckBox("Italic");

        Button backToForm = new Button("Back");
        backToForm.setOnAction(e -> rootStack.getChildren().setAll(formPane));

        Button nextToTemplate = new Button("Next");
        nextToTemplate.setOnAction(e -> {
            setupTemplatePane();
            rootStack.getChildren().setAll(templatePane);
        });

        HBox navButtons = new HBox(10, backToForm, nextToTemplate);

        stylingPane.getChildren().addAll(
            new Label("Select Font Color"), colorPicker,
            new Label("Font Style"), fontStyleBox,
            new Label("Font Size"), fontSizeSpinner,
            new Label("Page Margin"), marginSpinner,
            boldCheck, italicCheck,
            navButtons
        );
    }

    private VBox createDynamicSection(String label, VBox container) {
        Label sectionLabel = new Label(label);
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button addBtn = new Button("+ Add");
        addBtn.setOnAction(e -> {
           if (label.equals("Education")) {
                TextField courseField = new TextField();
                courseField.setPromptText("Course Name");

                TextField streamField = new TextField();
                streamField.setPromptText("Stream");

                TextField instituteField = new TextField();
                instituteField.setPromptText("Institute");

                TextField gradeField = new TextField();
                gradeField.setPromptText("Grade/Score");

                TextField yearField = new TextField();
                yearField.setPromptText("Year of Passing");

                HBox row = new HBox(10); // ✅ Correctly declare HBox before use

                Button cutBtn = new Button("×");
                cutBtn.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: transparent;");

                cutBtn.setOnAction(ev -> container.getChildren().remove(row)); // ✅ Now 'row' is known

                row.getChildren().addAll(courseField, streamField, instituteField, gradeField, yearField, cutBtn);
                container.getChildren().add(row);
            } else {
                    TextField tf = new TextField();
                    tf.setPromptText(label + " info");

                    Button cutBtn = new Button("×");
                    cutBtn.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: transparent;");

                    HBox row = new HBox(10, tf, cutBtn);
                    row.setAlignment(Pos.CENTER_LEFT);

                    cutBtn.setOnAction(ev -> container.getChildren().remove(row)); // ✅ use correct variable

                    container.getChildren().add(row);
                }

        });

        HBox header = new HBox(10, sectionLabel, addBtn);
        VBox section = new VBox(5, header, container);
        return section;
    }

    private void setupTemplatePane() {
        BorderPane templateLayout = new BorderPane();
        templateLayout.setPadding(new Insets(15));

        // Template selection
        VBox selectorBox = new VBox(10);
        selectorBox.getChildren().add(new Label("Select Template Style"));
        templateSelector = new ComboBox<>();
        templateSelector.getItems().addAll("Modern", "Classic", "Dark");
        templateSelector.setValue("Modern");
        templateSelector.setOnAction(e -> updatePreview());
        selectorBox.getChildren().add(templateSelector);
        selectorBox.setPrefWidth(200);
        selectorBox.setPadding(new Insets(10));

        // Preview box
        previewBox = new VBox(5);
        previewBox.setPadding(new Insets(10));
        previewBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
        updatePreview();

        // Navigation buttons
        Button backToStyle = new Button("Back");
        backToStyle.setOnAction(e -> rootStack.getChildren().setAll(stylingPane));

        Button exportBtn = new Button("Export to PDF");
        exportBtn.setOnAction(e -> exportToStylishPDF());

        Button saveBtnTemplate = new Button("Save Draft");
        saveBtnTemplate.setOnAction(e -> saveDraft());

        Button loadBtnTemplate = new Button("Load Draft");
        loadBtnTemplate.setOnAction(e -> loadDraft());

        HBox navButtons = new HBox(10, backToStyle, saveBtnTemplate, loadBtnTemplate, exportBtn);
        navButtons.setPadding(new Insets(10));

        // Assemble layout
        templateLayout.setLeft(selectorBox);
        templateLayout.setCenter(previewBox);
        templateLayout.setBottom(navButtons);

        templatePane = new VBox(templateLayout);
    }

    private void exportToStylishPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Resume PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.setMargins(marginSpinner.getValue(), marginSpinner.getValue(), marginSpinner.getValue(), marginSpinner.getValue());
                document.open();

                int style = (boldCheck.isSelected() ? Font.BOLD : 0) | (italicCheck.isSelected() ? Font.ITALIC : 0);
                BaseColor fontColor = new BaseColor(
                        (float) colorPicker.getValue().getRed(),
                        (float) colorPicker.getValue().getGreen(),
                        (float) colorPicker.getValue().getBlue()
                );

                Font baseFont = FontFactory.getFont(FontFactory.HELVETICA, fontSizeSpinner.getValue(), style, fontColor);
                Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA, fontSizeSpinner.getValue() + 4, Font.BOLD, BaseColor.DARK_GRAY);
                
                // Header with photo
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                headerTable.setWidths(new int[]{3, 1});

                PdfPCell nameCell = new PdfPCell();
                nameCell.setBorder(Rectangle.NO_BORDER);
                nameCell.addElement(new Paragraph(fields.get("Full Name").getText(), new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
                nameCell.addElement(new Paragraph(fields.get("Email").getText(), baseFont));
                nameCell.addElement(new Paragraph(fields.get("Phone").getText(), baseFont));
                headerTable.addCell(nameCell);

                if (profileImageFile != null) {
                    try {
                        com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(profileImageFile.getAbsolutePath());
                        img.scaleToFit(100, 100);
                        PdfPCell imgCell = new PdfPCell(img);
                        imgCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        imgCell.setBorder(Rectangle.NO_BORDER);
                        headerTable.addCell(imgCell);
                    } catch (Exception imgEx) {
                        PdfPCell empty = new PdfPCell();
                        empty.setBorder(Rectangle.NO_BORDER);
                        headerTable.addCell(empty);
                    }
                } else {
                    PdfPCell empty = new PdfPCell();
                    empty.setBorder(Rectangle.NO_BORDER);
                    headerTable.addCell(empty);
                }

                document.add(headerTable);
                document.add(Chunk.NEWLINE);

                // Helper to add section headers
                Consumer<String> lineSeparator = title -> {
                    try {
                        Paragraph heading = new Paragraph(title.toUpperCase(), sectionFont);
                        heading.setSpacingBefore(10);
                        heading.setSpacingAfter(5);
                        heading.setAlignment(Element.ALIGN_LEFT);
                        document.add(heading);
                        document.add(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(1, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_LEFT, -2f)));
                    } catch (Exception ignored) {}
                };

                // Helper to add bullet lists
               Consumer<VBox> addDynamicSection = (box) -> {
    com.itextpdf.text.List list = new com.itextpdf.text.List(false, 15);
    list.setListSymbol(new Chunk("\u2022 ", baseFont));

    // Recursive method to find and collect text from all TextInputControls
    Consumer<javafx.scene.Node> extractTexts = new Consumer<>() {
        @Override
        public void accept(javafx.scene.Node node) {
            if (node instanceof TextInputControl) {
                String text = ((TextInputControl) node).getText();
                if (!text.trim().isEmpty()) {
                    list.add(new ListItem(text, baseFont));
                }
            } else if (node instanceof Pane) {
                for (javafx.scene.Node child : ((Pane) node).getChildrenUnmodifiable()) {
                    this.accept(child); // Recurse into child containers
                }
            }
        }
    };

    // Start traversal from the top-level VBox
    for (javafx.scene.Node node : box.getChildren()) {
        extractTexts.accept(node);
    }

    if (!list.isEmpty()) {
        try {
            document.add(list);
            document.add(Chunk.NEWLINE);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
};



                // Education section
                lineSeparator.accept("Education");
                com.itextpdf.text.List eduList = new com.itextpdf.text.List(false, 15);
                for (javafx.scene.Node node : educationBox.getChildren()) {
                    if (node instanceof HBox) {
                        HBox row = (HBox) node;
                        if (row.getChildren().size() >= 5) {
                            String course = ((TextField) row.getChildren().get(0)).getText();
                            String stream = ((TextField) row.getChildren().get(1)).getText();
                            String institute = ((TextField) row.getChildren().get(2)).getText();
                            String grade = ((TextField) row.getChildren().get(3)).getText();
                            String year = ((TextField) row.getChildren().get(4)).getText();

                            if (!course.isEmpty() || !stream.isEmpty() || !institute.isEmpty() || !grade.isEmpty() || !year.isEmpty()) {
                                Paragraph item = new Paragraph();
                                item.add(new Chunk(year + "  ", baseFont));
                                item.add(new Chunk(institute + "\n", new Font(baseFont.getFamily(), baseFont.getSize(), Font.BOLD)));
                                item.add(new Chunk(course + (stream.isEmpty() ? "" : " (" + stream + ")") + "\n", baseFont));
                                if (!grade.isEmpty())
                                    item.add(new Chunk(grade, baseFont));
                                eduList.add(new ListItem(item));
                            }
                        }
                    }
                }
                document.add(eduList);
                document.add(Chunk.NEWLINE);

                // Other sections
                lineSeparator.accept("Skills");
                addDynamicSection.accept(skillsBox);

                lineSeparator.accept("Internship");
                addDynamicSection.accept(internshipBox);

                lineSeparator.accept("Experience");
                addDynamicSection.accept(experienceBox);

                lineSeparator.accept("Projects");
                addDynamicSection.accept(projectBox);

                // Custom fields
                for (Map.Entry<String, TextField> entry : fields.entrySet()) {
                    String key = entry.getKey();
                    if (!key.equals("Full Name") && !key.equals("Email") && !key.equals("Phone")) {
                        String value = entry.getValue().getText();
                        if (!value.trim().isEmpty()) {
                            lineSeparator.accept(key);
                            document.add(new Paragraph(value, baseFont));
                            document.add(Chunk.NEWLINE);
                        }
                    }
                }

                document.close();
                showAlert("Success", "Resume exported successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to export PDF:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void addCustomField() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Custom Field");
    dialog.setHeaderText("Enter the name of the custom field:");
    dialog.setContentText("Field name:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(fieldName -> {
        if (!fields.containsKey(fieldName)) {
            TextField tf = new TextField();
            fields.put(fieldName, tf);

            Label labelNode = new Label(fieldName);
            Button cutBtn = new Button("×");
            cutBtn.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: transparent;");
            
            // Layout all in HBox for easy removal
            HBox fieldRow = new HBox(10, labelNode, tf, cutBtn);
            fieldRow.setPadding(new Insets(5, 0, 5, 0));

            cutBtn.setOnAction(ev -> {
                formPane.getChildren().remove(fieldRow);
                fields.remove(fieldName);
            });

            formPane.getChildren().add(formPane.getChildren().size() - 1, fieldRow);
        } else {
            showAlert("Warning", "Field already exists.");
        }
    });
}

    private void updatePreview() {
        if (previewBox == null) return;

        previewBox.getChildren().clear();

        String selected = templateSelector.getValue();
        String bgColor;
        String textColor;
        String titleStyle = "-fx-font-size: 16px; -fx-font-weight: bold;";
        String sectionStyle = "-fx-font-size: 14px;";

        switch (selected) {
            case "Modern":
                bgColor = "#f0f8ff";
                textColor = "#333";
                break;
            case "Classic":
                bgColor = "#fffaf0";
                textColor = "#000";
                break;
            case "Dark":
                bgColor = "#2b2b2b";
                textColor = "#ffffff";
                break;
            default:
                bgColor = "#f9f9f9";
                textColor = "#000";
                break;
        }

    previewBox.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");

    // Profile photo
    if (profileImageFile != null) {
        try {
            javafx.scene.image.Image fxImage = new javafx.scene.image.Image(profileImageFile.toURI().toString(), 100, 100, true, true);
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(fxImage);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            previewBox.getChildren().add(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Basic fields
    Label name = new Label(fields.get("Full Name").getText());
    name.setStyle(titleStyle + "; -fx-text-fill: " + textColor + ";");

    Label email = new Label(fields.get("Email").getText());
    Label phone = new Label(fields.get("Phone").getText());

    for (Label label : new Label[]{email, phone}) {
        label.setStyle(sectionStyle + "; -fx-text-fill: " + textColor + ";");
    }

    previewBox.getChildren().addAll(name, email, phone);

    // Objective section (from TextArea)
    if (objectiveArea != null && !objectiveArea.getText().trim().isEmpty()) {
        Label objHeading = new Label("OBJECTIVE");
        objHeading.setStyle(titleStyle + "; -fx-text-fill: " + textColor + ";");

        Label objText = new Label(objectiveArea.getText().trim());
        objText.setWrapText(true);
        objText.setStyle(sectionStyle + "; -fx-text-fill: " + textColor + ";");

        previewBox.getChildren().addAll(new Separator(), objHeading, objText);
    }

    previewBox.getChildren().add(new Separator());

    // Education section
    Label eduHeading = new Label("EDUCATION");
    eduHeading.setStyle(titleStyle + "; -fx-text-fill: " + textColor + ";");
    previewBox.getChildren().add(eduHeading);

    for (Node node : educationBox.getChildren()) {
        if (node instanceof HBox row && row.getChildren().size() >= 5) {
            String course = ((TextField) row.getChildren().get(0)).getText();
            String stream = ((TextField) row.getChildren().get(1)).getText();
            String institute = ((TextField) row.getChildren().get(2)).getText();
            String grade = ((TextField) row.getChildren().get(3)).getText();
            String year = ((TextField) row.getChildren().get(4)).getText();

            if (!institute.isEmpty() || !course.isEmpty() || !stream.isEmpty() || !year.isEmpty() || !grade.isEmpty()) {
                Label entry = new Label("• " + institute + "\n" + course + " (" + stream + ")\n" + "Year: " + year + " | Score: " + grade);
                entry.setStyle(sectionStyle + "; -fx-text-fill: " + textColor + ";");
                previewBox.getChildren().add(entry);
            }
        }
    }

    previewBox.getChildren().add(new Separator());

    // Dynamic sections (Skills, Internship, etc.)
    BiConsumer<String, VBox> bulletSection = (title, box) -> {
        Label heading = new Label(title.toUpperCase());
        heading.setStyle(titleStyle + "; -fx-text-fill: " + textColor + ";");
        previewBox.getChildren().add(heading);

        for (Node node : box.getChildren()) {
            if (node instanceof TextField tf) {
                String val = tf.getText();
                if (!val.trim().isEmpty()) {
                    Label bullet = new Label("• " + val);
                    bullet.setStyle(sectionStyle + "; -fx-text-fill: " + textColor + ";");
                    previewBox.getChildren().add(bullet);
                }
            } else if (node instanceof HBox hbox) {
                for (Node field : hbox.getChildren()) {
                    if (field instanceof TextField tf) {
                        String val = tf.getText();
                        if (!val.trim().isEmpty()) {
                            Label bullet = new Label("• " + val);
                            bullet.setStyle(sectionStyle + "; -fx-text-fill: " + textColor + ";");
                            previewBox.getChildren().add(bullet);
                        }
                    }
                }
            }
        }

        previewBox.getChildren().add(new Separator());
    };

    bulletSection.accept("Skills", skillsBox);
    bulletSection.accept("Internship", internshipBox);
    bulletSection.accept("Experience", experienceBox);
    bulletSection.accept("Projects", projectBox);

    // Custom fields
    for (Map.Entry<String, TextField> entry : fields.entrySet()) {
        String key = entry.getKey();
        if (!Set.of("Full Name", "Email", "Phone").contains(key)) {
            String value = entry.getValue().getText();
            if (!value.trim().isEmpty()) {
                Label customHeading = new Label(key.toUpperCase());
                customHeading.setStyle(titleStyle + "; -fx-text-fill: " + textColor + ";");

                Label customValue = new Label(value);
                customValue.setStyle(sectionStyle + "; -fx-text-fill: " + textColor + ";");

                previewBox.getChildren().addAll(customHeading, customValue, new Separator());
            }
        }
    }
}


    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Data class for JSON serialization
    public static class ResumeData {
        public String fullName = "";
        public String email = "";
        public String phone = "";
        public List<String[]> education = new ArrayList<>();
        public List<String> skills = new ArrayList<>();
        public List<String> projects = new ArrayList<>();
        public List<String> internships = new ArrayList<>();
        public List<String> experience = new ArrayList<>();
        public Map<String, String> customFields = new LinkedHashMap<>();
        public String profilePhotoPath = "";
        public String fontColor = "#000000";
        public String fontStyle = "Arial";
        public Integer fontSize = 12;
        public Integer margin = 20;
        public boolean bold = false;
        public boolean italic = false;
        public String selectedTemplate = "Modern";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
