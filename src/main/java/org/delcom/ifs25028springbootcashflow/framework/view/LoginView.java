package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25028springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setId("login-username");
        username.setWidthFull();
        password.setId("login-password");
        password.setWidthFull();

        loginButton.setId("login-submit");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(e -> login());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        PageCard card = new PageCard("Masuk ke Cashflow");
        card.addClassName("auth-card");
        card.add(username, password, progress, loginButton, new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        add(card);
    }

    private void login() {
        progress.setVisible(true);
        loginButton.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException ex) {
            AppNotification.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            loginButton.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}