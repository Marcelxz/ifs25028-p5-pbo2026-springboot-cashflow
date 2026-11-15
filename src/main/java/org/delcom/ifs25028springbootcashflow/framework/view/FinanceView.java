package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25028springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25028springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25028springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25028springbootcashflow.usecase.FinanceUseCase;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {

    /** Penampung file bukti yang dipilih sebelum tombol Simpan ditekan. */
    private static class PendingFile {
        byte[] data;
        String name;
    }

    private final IUserRepository userRepository;
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private User user;
    private Grid<Transaction> grid;
    private final HorizontalLayout summaryRow = new HorizontalLayout();
    private final TextField search = new TextField();
    private final ComboBox<SortOption> sort = new ComboBox<>();

    public FinanceView(IUserRepository userRepository, FinanceUseCase financeUseCase,
                       FinancePresenter presenter, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        buildUi();
        refresh();
    }

    private void buildUi() {
        removeAll();
        setPadding(true);
        setWidthFull();

        summaryRow.setWidthFull();
        summaryRow.setSpacing(true);
        summaryRow.getStyle().set("flex-wrap", "wrap");

        search.setPlaceholder("Cari keterangan...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::getLabel);
        sort.setValue(SortOption.NEWEST_FIRST);
        sort.setAllowCustomValue(false);
        sort.setLabel("Urutkan");
        sort.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refresh());

        Button addIncome = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openAddDialog(TransactionType.INCOME));
        addIncome.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button addExpense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openAddDialog(TransactionType.EXPENSE));
        addExpense.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(search, sort, reload, addIncome, addExpense);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.getStyle().set("flex-wrap", "wrap");

        grid = GridStyles.transactionGrid(presenter);
        grid.addComponentColumn(t -> {
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(t));
            delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            delete.setAriaLabel("Hapus");
            return delete;
        }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);

        add(new H2("Catatan Keuangan"), summaryRow, toolbar, grid);
    }

    private void refresh() {
        grid.setItems(financeUseCase.search(user.getId(), search.getValue(), sort.getValue()));

        FinanceUseCase.Summary s = financeUseCase.getSummary(user.getId());
        summaryRow.removeAll();
        summaryRow.add(
                PageCard.stat("Total Transaksi", String.valueOf(s.count()), "stat-total"),
                PageCard.stat("Total Pemasukan", presenter.formatMoney(s.income()), "stat-income"),
                PageCard.stat("Total Pengeluaran", presenter.formatMoney(s.expense()), "stat-expense"),
                PageCard.stat("Saldo", presenter.formatMoney(s.balance()), "stat-balance"));
    }

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;
        AppDialog dialog = new AppDialog(income ? "Tambah Pemasukan" : "Tambah Pengeluaran",
                presenter.typeLabel(type));
        dialog.setWidth("440px");

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        description.setMaxLength(500);

        TextField amount = new TextField("Jumlah (Rp)");
        amount.setPlaceholder("contoh: 150000");
        amount.setWidthFull();

        PendingFile pending = new PendingFile();
        Span fileInfo = new Span("Belum ada bukti dipilih");
        UI ui = UI.getCurrent();

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pending.data = data;
            pending.name = metadata.fileName();
            ui.access(() -> fileInfo.setText("Bukti: " + metadata.fileName()));
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(2 * 1024 * 1024);
        upload.setDropAllowed(false);
        upload.setUploadButton(new Button("Pilih Bukti (opsional)"));
        upload.addFileRejectedListener(e -> AppNotification.error(e.getErrorMessage()));

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        VerticalLayout form = new VerticalLayout(description, amount, upload, fileInfo, progress);
        form.setPadding(false);
        dialog.add(form);

        Button cancel = new Button("Batal", e -> dialog.close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            String stored = null;
            try {
                if (pending.data != null) {
                    stored = fileStorage.store(pending.name, pending.data);
                }
                Transaction t = financeUseCase.add(user.getId(), description.getValue(),
                        amount.getValue(), type, stored);
                AppNotification.ok(presenter.addedMessage(t));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                fileStorage.deleteQuietly(stored);
                AppNotification.error(presenter.errorMessage(ex.getMessage()));
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });
        dialog.addActions(cancel, save);
        dialog.open();
    }

    private void confirmDelete(Transaction transaction) {
        AppDialog dialog = new AppDialog("Hapus Transaksi", "Konfirmasi");
        dialog.add(new Span("Hapus transaksi \"" + transaction.getDescription() + "\" ("
                + presenter.formatMoney(transaction.getAmount()) + ")? Tindakan ini tidak dapat dibatalkan."));

        Button cancel = new Button("Batal", e -> dialog.close());
        Button delete = new Button("Hapus");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> {
            try {
                Transaction removed = financeUseCase.delete(user.getId(), transaction.getId());
                fileStorage.deleteQuietly(removed.getProofPath());
                AppNotification.ok(presenter.deletedMessage(removed));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                AppNotification.error(presenter.errorMessage(ex.getMessage()));
            }
        });
        dialog.addActions(cancel, delete);
        dialog.open();
    }
}