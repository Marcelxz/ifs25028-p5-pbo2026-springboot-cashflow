package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.Comparator;
import org.delcom.ifs25028springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;

/** Gaya grid yang konsisten untuk tabel transaksi (dipakai FinanceView dan UserTransactionsView). */
public final class GridStyles {

    private GridStyles() {
    }

    public static Grid<Transaction> transactionGrid(FinancePresenter presenter) {
        Grid<Transaction> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("460px");

        grid.addColumn(Transaction::getId).setHeader("ID")
                .setSortable(true).setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(Transaction::getDescription).setHeader("Keterangan")
                .setSortable(true).setFlexGrow(1);

        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::getAmount))
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(t -> {
            Span badge = new Span(presenter.typeLabel(t.getType()));
            badge.getElement().getThemeList().add("badge " + (t.isIncome() ? "success" : "error"));
            return badge;
        })).setHeader("Tipe")
                .setComparator(Comparator.comparing(Transaction::getType))
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(t -> {
            if (t.getProofPath() == null) {
                return new Span("-");
            }
            String src = "/uploads/" + t.getProofPath();
            Image thumb = new Image(src, "Bukti transaksi");
            thumb.setWidth("48px");
            thumb.setHeight("48px");
            thumb.getStyle().set("object-fit", "cover").set("border-radius", "6px").set("cursor", "pointer");
            thumb.addClickListener(e -> new ImagePreviewDialog(src, "Bukti transaksi").open());
            return thumb;
        })).setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(t -> presenter.formatDateTime(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt))
                .setAutoWidth(true);

        return grid;
    }
}