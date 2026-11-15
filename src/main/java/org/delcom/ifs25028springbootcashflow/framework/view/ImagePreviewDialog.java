package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import java.util.Locale;

/** Modal preview gambar: klik gambar untuk toggle zoom 1.0x <-> 1.8x, plus tombol Perkecil/Perbesar/Tutup. */
public class ImagePreviewDialog extends AppDialog {

    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 1.8;
    private static final double STEP = 0.4;

    private final Image image;
    private final Button zoomOutButton;
    private final Button zoomInButton;
    private double zoom = MIN_ZOOM;

    public ImagePreviewDialog(String src, String altText) {
        super("Pratinjau Gambar", "Zoom");
        setWidth("720px");

        image = new Image(src, altText);
        image.getStyle()
                .set("display", "block")
                .set("max-width", "none")
                .set("margin", "0 auto");
        image.addClickListener(e -> setZoom(zoom >= MAX_ZOOM ? MIN_ZOOM : MAX_ZOOM));

        Div box = new Div(image);
        box.getStyle().set("overflow", "auto").set("max-height", "65vh");
        add(box);

        zoomOutButton = new Button("Perkecil", e -> setZoom(zoom - STEP));
        zoomInButton = new Button("Perbesar", e -> setZoom(zoom + STEP));
        Button closeButton = new Button("Tutup", e -> close());
        addActions(zoomOutButton, zoomInButton, closeButton);

        applyZoom();
    }

    private void setZoom(double value) {
        double bounded = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, value));
        zoom = Math.round(bounded * 10) / 10.0;
        applyZoom();
    }

    private void applyZoom() {
        image.setWidth(String.format(Locale.US, "%.0f%%", zoom * 100));
        image.getStyle().set("cursor", zoom >= MAX_ZOOM ? "zoom-out" : "zoom-in");
        zoomOutButton.setEnabled(zoom > MIN_ZOOM);
        zoomInButton.setEnabled(zoom < MAX_ZOOM);
    }
}