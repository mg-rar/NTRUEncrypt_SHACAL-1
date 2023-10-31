module com.example.debilwillcry {
    requires javafx.controls;
    requires javafx.fxml;
    requires log4j;
    requires org.apache.commons.lang3;
    requires org.jetbrains.annotations;


    opens com.example.debilwillcry to javafx.fxml;
    exports com.example.debilwillcry;
}