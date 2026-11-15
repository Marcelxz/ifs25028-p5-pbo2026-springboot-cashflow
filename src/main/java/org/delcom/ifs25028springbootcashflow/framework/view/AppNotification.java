package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public final class AppNotification {

    private AppNotification() {
    }

    public static void ok(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String message) {
        Notification n = Notification.show(message, 5000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}