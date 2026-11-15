package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.value.ValueChangeMode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(LoginView.class);

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final Input username = new Input();
    private final Input password = new Input();
    private final NativeButton loginButton = new NativeButton("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        configure(username, "login-username", "text", "username");
        configure(password, "login-password", "password", "current-password");

        loginButton.setId("login-submit");
        loginButton.getElement().setAttribute("type", "submit");
        loginButton.addClassName("auth-submit");
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(e -> readFormThenLogin());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        PageCard card = new PageCard("Masuk ke Cashflow");
        card.addClassName("auth-card");
        card.add(field("Username", username), field("Password", password), progress, loginButton,
                new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        add(card);
    }

    private void configure(Input input, String id, String type, String autocomplete) {
        input.setId(id);
        input.setType(type);
        input.setValueChangeMode(ValueChangeMode.EAGER);
        input.addClassName("auth-input");
        input.getElement().setAttribute("autocomplete", autocomplete);
    }

    private Div field(String labelText, Input input) {
        NativeLabel label = new NativeLabel(labelText);
        label.setFor(input.getId().orElseThrow());
        Div wrapper = new Div(label, input);
        wrapper.addClassName("auth-field");
        return wrapper;
    }

    /** Membaca nilai kolom langsung dari browser saat tombol ditekan, lalu login. */
    private void readFormThenLogin() {
        UI ui = UI.getCurrent();
        String serverUser = username.getValue();
        int serverPasswordLength = password.getValue().length();

        ui.getPage().executeJs("return document.getElementById('login-username').value;")
                .then(String.class, browserUser ->
                        ui.getPage().executeJs("return document.getElementById('login-password').value;")
                                .then(String.class, browserPassword -> {
                                    String name = browserUser == null ? "" : browserUser;
                                    String pass = browserPassword == null ? "" : browserPassword;
                                    log.info("Form login: server(username='{}', panjang password={}) "
                                                    + "browser(username='{}', panjang password={})",
                                            serverUser, serverPasswordLength, name, pass.length());
                                    doLogin(name, pass);
                                }));
    }

    private void doLogin(String name, String pass) {
        progress.setVisible(true);
        loginButton.setEnabled(false);
        try {
            User user = authUseCase.login(name, pass);
            AuthSession.login(user);
            log.info("Login berhasil: username '{}'", user.getUsername());
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException ex) {
            log.warn("Login gagal: username '{}': {}", name, ex.getMessage());
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