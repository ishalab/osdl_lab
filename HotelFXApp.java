import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HotelFXApp extends Application {

    ObservableList<Room> rooms = FXCollections.observableArrayList();
    ObservableList<Customer> customers = FXCollections.observableArrayList();

    TableView<Room> roomTable = new TableView<>();
    TableView<Customer> customerTable = new TableView<>();

    // ── Colour palette ──────────────────────────────────────────────
    private static final String BG_DARK      = "#0f1117";
    private static final String BG_CARD      = "#1a1d27";
    private static final String BG_FIELD     = "#252836";
    private static final String ACCENT_GOLD  = "#c9a84c";
    private static final String ACCENT_TEAL  = "#2dd4bf";
    private static final String TEXT_PRIMARY = "#f0ece0";
    private static final String TEXT_MUTED   = "#8a8a9a";
    private static final String SUCCESS      = "#4ade80";
    private static final String DANGER       = "#f87171";

    private Label statusBar = new Label("Welcome to AAI Hotel Management");

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        root.setTop(buildHeader());
        root.setCenter(buildTabPane());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 950, 650);
        scene.getStylesheets().add(inlineCSS());

        stage.setTitle("AAI — Hotel Management");
        stage.setScene(scene);
        stage.show();

        animateIn(root);
    }

    // ── Header ───────────────────────────────────────────────────────
    private HBox buildHeader() {
        Label logo = new Label("AAI");
        logo.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 22px; " +
                      "-fx-font-weight: bold; -fx-text-fill: " + ACCENT_GOLD + ";");

        Label subtitle = new Label("Hotel Management System");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TEXT_MUTED + "; " +
                          "-fx-font-style: italic;");

        VBox brand = new VBox(2, logo, subtitle);
        brand.setAlignment(Pos.CENTER_LEFT);

        // Stats pills
        Label roomCount  = buildStatPill("🛏", "Rooms", rooms);
        Label guestCount = buildStatPill("👤", "Guests", customers);

        rooms.addListener((ListChangeListener<Room>) c ->
            roomCount.setText("🛏  " + rooms.size() + " Rooms"));
        customers.addListener((ListChangeListener<Customer>) c ->
            guestCount.setText("👤  " + customers.size() + " Guests"));

        HBox stats = new HBox(10, roomCount, guestCount);
        stats.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(brand, stats);
        HBox.setHgrow(brand, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(18, 30, 18, 30));
        header.setStyle("-fx-background-color: " + BG_CARD + "; " +
                        "-fx-border-color: " + ACCENT_GOLD + "; " +
                        "-fx-border-width: 0 0 1 0;");
        return header;
    }

    private Label buildStatPill(String icon, String label, ObservableList<?> list) {
        Label pill = new Label(icon + "  " + list.size() + " " + label);
        pill.setStyle("-fx-background-color: " + BG_FIELD + "; " +
                      "-fx-text-fill: " + ACCENT_TEAL + "; " +
                      "-fx-font-size: 12px; -fx-padding: 6 14 6 14; " +
                      "-fx-background-radius: 20;");
        return pill;
    }

    // ── Tab Pane ─────────────────────────────────────────────────────
    private TabPane buildTabPane() {
        TabPane tp = new TabPane();
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tp.setStyle("-fx-background-color: " + BG_DARK + ";");

        Tab roomTab    = new Tab("  🛏   Room Management  ", createRoomPane());
        Tab bookingTab = new Tab("  🔑   Booking & Guests  ", createBookingPane());

        tp.getTabs().addAll(roomTab, bookingTab);
        return tp;
    }

    // ── Room Pane ────────────────────────────────────────────────────
    private VBox createRoomPane() {
        // Form fields
        TextField roomNoField = styledField("e.g. 101");
        ComboBox<String> typeBox = styledCombo("Single", "Double", "Deluxe", "Suite");
        TextField priceField  = styledField("e.g. 4500");
        Label msg = new Label();

        Button addBtn           = primaryButton("＋  Add Room");
        Button showAvailableBtn = outlineButton("Available Only");
        Button showAllBtn       = outlineButton("Show All");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 12;");

        form.add(fieldLabel("Room Number"), 0, 0); form.add(roomNoField, 1, 0);
        form.add(fieldLabel("Room Type"),   0, 1); form.add(typeBox,     1, 1);
        form.add(fieldLabel("Price (₹)"),  0, 2); form.add(priceField,  1, 2);

        HBox formButtons = new HBox(10, addBtn, msg);
        formButtons.setAlignment(Pos.CENTER_LEFT);
        form.add(formButtons, 1, 3);

        setupRoomTable();
        roomTable.setStyle("-fx-background-color: " + BG_CARD + ";");

        addBtn.setOnAction(e -> {
            try {
                int roomNo    = Integer.parseInt(roomNoField.getText().trim());
                String type   = typeBox.getValue();
                double price  = Double.parseDouble(priceField.getText().trim());

                if (type == null) { showMsg(msg, "⚠ Select a room type!", DANGER); return; }

                rooms.add(new Room(roomNo, type, price));
                showMsg(msg, "✔ Room " + roomNo + " added!", SUCCESS);
                roomNoField.clear(); priceField.clear(); typeBox.setValue(null);
                pulseNode(roomTable);
            } catch (NumberFormatException ex) {
                showMsg(msg, "✘ Invalid number input!", DANGER);
            }
        });

        showAvailableBtn.setOnAction(e -> {
            roomTable.setItems(rooms.filtered(Room::isAvailable));
            flashTable(roomTable);
        });
        showAllBtn.setOnAction(e -> {
            roomTable.setItems(rooms);
            flashTable(roomTable);
        });

        HBox filterRow = new HBox(10, showAvailableBtn, showAllBtn);
        filterRow.setPadding(new Insets(0, 0, 0, 4));

        VBox pane = new VBox(16, form, filterRow, roomTable);
        pane.setPadding(new Insets(20));
        VBox.setVgrow(roomTable, Priority.ALWAYS);
        return pane;
    }

    private void setupRoomTable() {
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomTable.setPlaceholder(styledPlaceholder("No rooms added yet"));

        TableColumn<Room, Integer> noCol = col("Room No", "roomNumber", 80);
        TableColumn<Room, String>  tyCol = col("Type",    "roomType",   100);
        TableColumn<Room, Double>  prCol = col("Price ₹", "price",      100);

        TableColumn<Room, Boolean> avCol = new TableColumn<>("Status");
        avCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        avCol.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(v ? "● Available" : "● Booked");
                badge.setStyle("-fx-text-fill: " + (v ? SUCCESS : DANGER) + "; " +
                               "-fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setText(null);
            }
        });

        roomTable.getColumns().addAll(noCol, tyCol, prCol, avCol);
        roomTable.setItems(rooms);
    }

    // ── Booking Pane ─────────────────────────────────────────────────
    private VBox createBookingPane() {
        TextField nameField    = styledField("Guest full name");
        TextField contactField = styledField("Phone / Email");
        ComboBox<Integer> roomBox = new ComboBox<>();
        roomBox.setPromptText("Select room");
        roomBox.setStyle(comboStyle());
        roomBox.setPrefWidth(220);

        Label msg = new Label();

        Button bookBtn     = primaryButton("🔑  Book Room");
        Button checkoutBtn = new Button("🚪  Checkout");
        checkoutBtn.setStyle(dangerButtonStyle());

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 12;");

        form.add(fieldLabel("Guest Name"),    0, 0); form.add(nameField,    1, 0);
        form.add(fieldLabel("Contact"),       0, 1); form.add(contactField, 1, 1);
        form.add(fieldLabel("Room Number"),   0, 2); form.add(roomBox,      1, 2);

        HBox formButtons = new HBox(10, bookBtn, checkoutBtn, msg);
        formButtons.setAlignment(Pos.CENTER_LEFT);
        form.add(formButtons, 1, 3);

        setupCustomerTable();
        customerTable.setStyle("-fx-background-color: " + BG_CARD + ";");

        roomBox.setOnMouseClicked(e -> {
            Integer prev = roomBox.getValue();
            roomBox.getItems().setAll(
                rooms.stream()
                     .filter(Room::isAvailable)
                     .map(Room::getRoomNumber)
                     .toList()
            );
            if (prev != null && roomBox.getItems().contains(prev)) roomBox.setValue(prev);
        });

        bookBtn.setOnAction(e -> {
            if (roomBox.getValue() == null) { showMsg(msg, "⚠ Select a room!", DANGER); return; }
            int roomNo = roomBox.getValue();
            for (Room r : rooms) {
                if (r.getRoomNumber() == roomNo) {
                    if (!r.isAvailable()) { showMsg(msg, "✘ Room already booked!", DANGER); return; }
                    r.setAvailable(false);
                    customers.add(new Customer(nameField.getText(), contactField.getText(), roomNo));
                    showMsg(msg, "✔ Room " + roomNo + " booked for " + nameField.getText() + "!", SUCCESS);
                    setStatus("Room " + roomNo + " booked for " + nameField.getText());
                    nameField.clear(); contactField.clear(); roomBox.setValue(null);
                    pulseNode(customerTable);
                    roomTable.refresh();
                    return;
                }
            }
        });

        checkoutBtn.setOnAction(e -> {
            if (roomBox.getValue() == null) { showMsg(msg, "⚠ Select a room!", DANGER); return; }
            int roomNo = roomBox.getValue();
            rooms.stream().filter(r -> r.getRoomNumber() == roomNo).findFirst()
                 .ifPresent(r -> r.setAvailable(true));
            customers.removeIf(c -> c.getRoomNumber() == roomNo);
            showMsg(msg, "✔ Room " + roomNo + " checked out!", ACCENT_TEAL);
            setStatus("Checkout completed for room " + roomNo);
            roomTable.refresh();
            roomBox.setValue(null);
        });

        VBox pane = new VBox(16, form, customerTable);
        pane.setPadding(new Insets(20));
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        return pane;
    }

    private void setupCustomerTable() {
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customerTable.setPlaceholder(styledPlaceholder("No active bookings"));

        customerTable.getColumns().addAll(
            col("Guest Name", "name",       160),
            col("Contact",    "contact",    160),
            col("Room No",    "roomNumber", 80)
        );
        customerTable.setItems(customers);
    }

    // ── Status Bar ───────────────────────────────────────────────────
    private HBox buildStatusBar() {
        statusBar.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        HBox bar = new HBox(statusBar);
        bar.setPadding(new Insets(8, 20, 8, 20));
        bar.setStyle("-fx-background-color: " + BG_CARD + "; " +
                     "-fx-border-color: " + ACCENT_GOLD + "; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private void setStatus(String text) {
        statusBar.setText("● " + text);
        statusBar.setStyle("-fx-text-fill: " + ACCENT_TEAL + "; -fx-font-size: 11px;");
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> {
            statusBar.setText("Ready");
            statusBar.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        });
        pause.play();
    }

    // ── Helpers ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private <T, V> TableColumn<T, V> col(String title, String prop, double minW) {
        TableColumn<T, V> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMinWidth(minW);
        return c;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        tf.setStyle("-fx-background-color: " + BG_FIELD + "; " +
                    "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                    "-fx-prompt-text-fill: " + TEXT_MUTED + "; " +
                    "-fx-border-color: #3a3d50; -fx-border-radius: 6; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12 8 12;");
        return tf;
    }

    private ComboBox<String> styledCombo(String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setPromptText("Select type");
        cb.setPrefWidth(220);
        cb.setStyle(comboStyle());
        return cb;
    }

    private String comboStyle() {
        return "-fx-background-color: " + BG_FIELD + "; " +
               "-fx-border-color: #3a3d50; -fx-border-radius: 6; " +
               "-fx-background-radius: 6;";
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        l.setMinWidth(100);
        return l;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + ACCENT_GOLD + "; " +
                   "-fx-text-fill: #1a1200; -fx-font-weight: bold; " +
                   "-fx-background-radius: 8; -fx-padding: 9 20 9 20; " +
                   "-fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(ACCENT_GOLD, "#e0b85a")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace("#e0b85a", ACCENT_GOLD)));
        return b;
    }

    private Button outlineButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: transparent; " +
                   "-fx-border-color: " + ACCENT_TEAL + "; " +
                   "-fx-text-fill: " + ACCENT_TEAL + "; " +
                   "-fx-border-radius: 8; -fx-background-radius: 8; " +
                   "-fx-padding: 7 16 7 16; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle() +
                   "-fx-background-color: " + ACCENT_TEAL + "22;"));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace(
                   "-fx-background-color: " + ACCENT_TEAL + "22;", "")));
        return b;
    }

    private String dangerButtonStyle() {
        return "-fx-background-color: transparent; " +
               "-fx-border-color: " + DANGER + "; " +
               "-fx-text-fill: " + DANGER + "; " +
               "-fx-border-radius: 8; -fx-background-radius: 8; " +
               "-fx-padding: 7 16 7 16; -fx-cursor: hand;";
    }

    private Label styledPlaceholder(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-style: italic;");
        return l;
    }

    private void showMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), lbl);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lbl.setText(""));
        pause.play();
    }

    private void pulseNode(javafx.scene.Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setFromX(1); st.setFromY(1);
        st.setToX(1.01); st.setToY(1.01);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    private void flashTable(TableView<?> table) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), table);
        ft.setFromValue(0.4); ft.setToValue(1); ft.play();
    }

    private void animateIn(javafx.scene.Node root) {
        root.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), root);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private String inlineCSS() {
        return "data:text/css," +
            ".tab-pane .tab-header-area .tab-header-background{-fx-background-color:%231a1d27;}" +
            ".tab-pane .tab{-fx-background-color:%23252836;-fx-background-radius:8 8 0 0;}" +
            ".tab-pane .tab:selected{-fx-background-color:%230f1117;}" +
            ".tab-pane .tab .tab-label{-fx-text-fill:%238a8a9a;-fx-font-size:13px;}" +
            ".tab-pane .tab:selected .tab-label{-fx-text-fill:%23c9a84c;-fx-font-weight:bold;}" +
            ".table-view{-fx-background-color:%231a1d27;-fx-border-color:%233a3d50;-fx-border-radius:8;-fx-background-radius:8;}" +
            ".table-view .column-header{-fx-background-color:%23252836;}" +
            ".table-view .column-header .label{-fx-text-fill:%238a8a9a;-fx-font-size:11px;}" +
            ".table-row-cell{-fx-background-color:%231a1d27;-fx-border-color:%232a2d3a;-fx-text-fill:%23f0ece0;}" +
            ".table-row-cell:odd{-fx-background-color:%23202330;}" +
            ".table-row-cell:selected{-fx-background-color:%23c9a84c22;}" +
            ".table-row-cell .table-cell{-fx-text-fill:%23f0ece0;-fx-font-size:13px;}" +
            ".scroll-bar{-fx-background-color:%231a1d27;}" +
            ".scroll-bar .thumb{-fx-background-color:%233a3d50;-fx-background-radius:4;}" +
            ".combo-box .list-cell{-fx-text-fill:%23f0ece0;-fx-background-color:%23252836;}" +
            ".combo-box-popup .list-view{-fx-background-color:%23252836;-fx-border-color:%233a3d50;}" +
            ".combo-box-popup .list-cell:hover{-fx-background-color:%23c9a84c33;}" +
            ".combo-box .arrow-button{-fx-background-color:%23252836;}" +
            ".combo-box .arrow{-fx-background-color:%238a8a9a;}";
    }

    public static void main(String[] args) { launch(args); }
}