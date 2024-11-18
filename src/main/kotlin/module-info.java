module com.gblm {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.xmlbeans;
    requires org.apache.commons.collections4;
    requires com.google.zxing.javase;
    requires com.google.zxing;
    requires javafx.swing;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;
    requires commons.math3;
    requires java.logging;
    requires java.management;
    requires org.apache.pdfbox;


    opens com.labellingprogram to javafx.fxml;

    exports com.labellingprogram;
}