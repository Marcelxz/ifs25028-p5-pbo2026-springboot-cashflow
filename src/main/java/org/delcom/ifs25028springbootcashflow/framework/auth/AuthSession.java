package org.delcom.ifs25028springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.delcom.ifs25028springbootcashflow.domain.entity.User;
import org.delcom.ifs25028springbootcashflow.domain.repository.IUserRepository;

/** Sesi login: VaadinSession + HTTP session + cookie bertanda tangan (cashflow_uid). */
public final class AuthSession {

    public static final String USER_ID_KEY = "cashflow.userId";
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    public static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    // kunci tanda tangan cookie dibuat acak setiap aplikasi start
    private static final byte[] SECRET = new byte[32];

    static {
        new SecureRandom().nextBytes(SECRET);
    }

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, null);
        session.setAttribute(USER_ID_KEY, user.getId());
        if (session.getSession() != null) {
            session.getSession().setAttribute(USER_ID_KEY, user.getId());
            session.getSession().removeAttribute(LOGGED_OUT_KEY);
        }
        writeCookie(user.getId() + "." + sign(user.getId()), COOKIE_MAX_AGE);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }
        session.setAttribute(USER_ID_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        if (session.getSession() != null) {
            session.getSession().removeAttribute(USER_ID_KEY);
            session.getSession().setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    /** Mengembalikan user yang sedang login, atau null. Memulihkan sesi setelah refresh. */
    public static User currentUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        Integer id = (Integer) session.getAttribute(USER_ID_KEY);
        if (id == null && session.getSession() != null) {
            id = (Integer) session.getSession().getAttribute(USER_ID_KEY);
        }
        if (id == null) {
            boolean loggedOut = Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY));
            if (loggedOut) {
                return null;
            }
            id = idFromCookie();
        }
        if (id == null) {
            return null;
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            session.setAttribute(USER_ID_KEY, null);
            return null;
        }
        session.setAttribute(USER_ID_KEY, id);
        return user;
    }

    private static Integer idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (!COOKIE_NAME.equals(cookie.getName())) {
                continue;
            }
            String[] parts = cookie.getValue().split("\\.");
            if (parts.length != 2) {
                return null;
            }
            try {
                int id = Integer.parseInt(parts[0]);
                boolean valid = MessageDigest.isEqual(
                        sign(id).getBytes(StandardCharsets.UTF_8),
                        parts[1].getBytes(StandardCharsets.UTF_8));
                return valid ? id : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static String sign(int id) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(String.valueOf(id).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Gagal membuat tanda tangan cookie", e);
        }
    }
}