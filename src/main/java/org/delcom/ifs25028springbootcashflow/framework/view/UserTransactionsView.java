package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25028springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25028springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25028springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25028springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25028springbootcashflow.usecase.UserUseCase;

/** Khusus ADMIN: melihat transaksi user lain (read-only). Dibuka lewat /user-transactions?userId=N. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    public UserTransactionsView(IUserRepository userRepository, UserUseCase userUseCase,
                                FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(userRepository);
        if (current == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!current.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }

        int targetId;
        try {
            targetId = Integer.parseInt(event.getLocation().getQueryParameters()
                    .getSingleParameter("userId").orElse(""));
        } catch (NumberFormatException e) {
            event.forwardTo(UsersView.class);
            return;
        }

        try {
            buildUi(userUseCase.getUser(targetId));
        } catch (DomainException ex) {
            AppNotification.error(ex.getMessage());
            event.forwardTo(UsersView.class);
        }
    }

    private void buildUi(User target) {
        removeAll();
        setPadding(true);

        Button back = new Button("Kembali", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(UsersView.class));

        HorizontalLayout summaryRow = new HorizontalLayout();
        summaryRow.setWidthFull();
        summaryRow.getStyle().set("flex-wrap", "wrap");
        FinanceUseCase.Summary s = financeUseCase.getSummary(target.getId());
        summaryRow.add(
                PageCard.stat("Total Transaksi", String.valueOf(s.count()), "stat-total"),
                PageCard.stat("Total Pemasukan", presenter.formatMoney(s.income()), "stat-income"),
                PageCard.stat("Total Pengeluaran", presenter.formatMoney(s.expense()), "stat-expense"),
                PageCard.stat("Saldo", presenter.formatMoney(s.balance()), "stat-balance"));

        Grid<Transaction> grid = GridStyles.transactionGrid(presenter);
        grid.setItems(financeUseCase.search(target.getId(), "", SortOption.NEWEST_FIRST));

        add(back, new H2("Transaksi: " + target.getFullName() + " (" + target.getUsername() + ")"),
                summaryRow, grid);
    }
}