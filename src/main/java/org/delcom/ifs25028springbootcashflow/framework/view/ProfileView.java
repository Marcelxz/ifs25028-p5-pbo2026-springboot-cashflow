package org.delcom.ifs25028springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25028springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25028springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25028springbootcashflow.usecase.UserUseCase;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final UserUseCase userUseCase;
    private final FileStorage fileStorage;

    private User user;
    private Avatar avatar;

    public ProfileView(IUserRepository userRepository, UserUseCase userUseCase, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.fileStorage = fileStorage;
        // Aksesibilitas: landmark <main>
        getElement().setAttribute("role", "main");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        buildUi();
    }

    private void buildUi() {
        removeAll();
        setPadding(true);

        avatar = new Avatar(user.getFullName());
        avatar.setHeight("96px");
        avatar.setWidth("96px");
        updateAvatar();

        TextField username = new TextField("Username");
        username.setValue(user.getUsername());
        username.setReadOnly(true);
        username.setWidthFull();

        TextField fullName = new TextField("Nama lengkap");
        fullName.setValue(user.getFullName());
        fullName.setWidthFull();

        PasswordField newPassword = new PasswordField("Password baru (kosongkan jika tidak diubah)");
        newPassword.setWidthFull();
        PasswordField confirm = new PasswordField("Konfirmasi password baru");
        confirm.setWidthFull();

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button save = new Button("Simpan Perubahan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                if (!newPassword.getValue().equals(confirm.getValue())) {
                    AppNotification.error("Konfirmasi password tidak cocok");
                    return;
                }
                user = userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
                newPassword.clear();
                confirm.clear();
                AppNotification.ok("Profil berhasil diperbarui");
                syncLayout();
            } catch (DomainException ex) {
                AppNotification.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });

        UI ui = UI.getCurrent();
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            String stored = null;
            try {
                stored = fileStorage.store(metadata.fileName(), data);
                String oldPhoto = user.getPhotoPath();
                User updated = userUseCase.updatePhoto(user.getId(), stored);
                fileStorage.deleteQuietly(oldPhoto);
                ui.access(() -> {
                    user = updated;
                    updateAvatar();
                    syncLayout();
                    AppNotification.ok("Foto profil berhasil diperbarui");
                });
            } catch (DomainException ex) {
                fileStorage.deleteQuietly(stored);
                ui.access(() -> AppNotification.error(ex.getMessage()));
            }
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(2 * 1024 * 1024);
        upload.setDropAllowed(false);
        upload.setUploadButton(new Button("Unggah Foto Profil"));
        upload.addFileRejectedListener(e -> AppNotification.error(e.getErrorMessage()));

        PageCard card = new PageCard(null);
        card.setMaxWidth("520px");
        card.add(new H2("Profil Saya"), avatar, upload, username, fullName, newPassword, confirm, progress, save);
        add(card);
    }

    private void updateAvatar() {
        avatar.setName(user.getFullName());
        avatar.setImage(user.getPhotoPath() == null ? null : "/uploads/" + user.getPhotoPath());
    }

    /** Menyegarkan avatar dan nama di sidebar/navbar. */
    private void syncLayout() {
        Component c = this;
        while ((c = c.getParent().orElse(null)) != null) {
            if (c instanceof MainLayout layout) {
                layout.render(user);
                return;
            }
        }
    }
}