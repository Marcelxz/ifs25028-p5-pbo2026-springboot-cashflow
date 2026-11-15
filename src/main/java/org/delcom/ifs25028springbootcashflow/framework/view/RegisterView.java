package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25028springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25028springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Konfirmasi password");
    private final Button registerButton = new Button("Daftar");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        // Aksesibilitas: landmark <main> untuk seluruh isi halaman
        getElement().setAttribute("role", "main");

        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();

        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> register());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        // Aksesibilitas: setiap halaman wajib memiliki satu <h1>
        H1 heading = new H1("Daftar Akun Baru");
        heading.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");

        PageCard card = new PageCard(null);
        card.addClassName("auth-card");
        card.add(heading, fullName, username, password, confirm, progress, registerButton,
                new RouterLink("Sudah punya akun? Masuk", LoginView.class));
        add(card);
    }

    private void register() {
        progress.setVisible(true);
        registerButton.setEnabled(false);
        try {
            if (!password.getValue().equals(confirm.getValue())) {
                AppNotification.error("Konfirmasi password tidak cocok");
                return;
            }
            authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            AppNotification.ok("Registrasi berhasil, silakan masuk");
            UI.getCurrent().navigate(LoginView.class);
        } catch (DomainException ex) {
            AppNotification.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            registerButton.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}