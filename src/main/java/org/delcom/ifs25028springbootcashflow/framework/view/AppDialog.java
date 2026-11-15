package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;

/** Dialog dasar: header biru primer + badge, footer abu-abu, tombol di kanan (lihat styles.css). */
public class AppDialog extends Dialog {

    public AppDialog(String title, String badge) {
        addClassName("app-dialog");
        setHeaderTitle(title);
        setCloseOnOutsideClick(false);
        if (badge != null) {
            Span span = new Span(badge);
            span.getElement().getThemeList().add("badge contrast small");
            getHeader().add(span);
        }
    }

    public void addActions(Component... components) {
        getFooter().add(components);
    }
}