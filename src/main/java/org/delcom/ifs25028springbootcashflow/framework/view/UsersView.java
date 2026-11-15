package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Map;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25028springbootcashflow.usecase.UserUseCase;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;

    public UsersView(IUserRepository userRepository, UserUseCase userUseCase) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        // Aksesibilitas: landmark <main>
        getElement().setAttribute("role", "main");
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
        buildUi();
    }

    private void buildUi() {
        removeAll();
        setPadding(true);

        Grid<User> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("460px");
        grid.addColumn(User::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setSortable(true).setAutoWidth(true);
        grid.addComponentColumn(u -> {
            Button view = new Button("Lihat Transaksi", e -> UI.getCurrent().navigate("user-transactions",
                    QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return view;
        }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        grid.setItems(userUseCase.listUsers());

        add(new H2("Daftar Pengguna"), grid);
    }
}