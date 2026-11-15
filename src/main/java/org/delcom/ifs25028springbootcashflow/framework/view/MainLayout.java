package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final Div menuHolder = new Div();
    private final Div drawerContent = new Div();

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;

        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        HorizontalLayout bar = new HorizontalLayout(toggle, title, menuHolder);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.expand(title);
        bar.getStyle().set("padding-right", "var(--lumo-space-m)");

        addToNavbar(bar);
        addToDrawer(drawerContent);
        setPrimarySection(Section.DRAWER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        render(user);
    }

    /** Menggambar ulang sidebar dan menu avatar (juga dipanggil ProfileView setelah profil berubah). */
    public void render(User user) {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }
        drawerContent.removeAll();
        drawerContent.add(nav);

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        Span name = new Span(user.getFullName());
        HorizontalLayout trigger = new HorizontalLayout(avatar, name);
        trigger.setAlignItems(FlexComponent.Alignment.CENTER);

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menuBar.addItem(trigger);
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().getPage().setLocation("/login");
        });

        menuHolder.removeAll();
        menuHolder.add(menuBar);
    }
}