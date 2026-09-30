module ni.uam.edu.practicabd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;

    opens ni.uam.edu.practicabd to javafx.fxml;
    opens ni.uam.edu.practicabd.Controllers to javafx.fxml;
    opens ni.uam.edu.practicabd.Modelos to javafx.base;

    exports ni.uam.edu.practicabd;
    exports ni.uam.edu.practicabd.Controllers;
    exports ni.uam.edu.practicabd.Modelos;
    exports ni.uam.edu.practicabd.DAO;
    exports ni.uam.edu.practicabd.Util;
}