module com.example.lab_1_fb_statistics_javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.lab_1_fb_statistics_javafx to javafx.fxml;
    exports com.example.lab_1_fb_statistics_javafx;
}