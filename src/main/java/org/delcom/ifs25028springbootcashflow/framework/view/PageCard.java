package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Kartu konten halaman. */
public class PageCard extends VerticalLayout {

    public PageCard(String title) {
        addClassName("page-card");
        setPadding(true);
        setSpacing(true);
        if (title != null) {
            H3 heading = new H3(title);
            heading.addClassName("page-card-title");
            add(heading);
        }
    }

    /** Kartu angka ringkasan (total, pemasukan, pengeluaran, saldo). */
    public static Div stat(String title, String value, String cssClass) {
        Span titleSpan = new Span(title);
        titleSpan.addClassName("stat-title");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value");

        Div card = new Div(titleSpan, valueSpan);
        card.addClassNames("stat-card", cssClass);
        card.getStyle().set("display", "flex").set("flex-direction", "column");
        return card;
    }
}