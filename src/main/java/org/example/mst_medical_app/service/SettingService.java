package org.example.mst_medical_app.service;

import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.SettingRepository;
import org.example.mst_medical_app.model.UserModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý logic cho các Cài đặt Ứng dụng (Theme, Language).
 */
public class SettingService {

    private final SettingRepository repository;
    private final UserModel currentUser;
    private Map<String, String> userSettingsCache;

    public static final String KEY_THEME = "APP_THEME";
    public static final String KEY_LANGUAGE = "APP_LANGUAGE";

    public static final String THEME_LIGHT = "Light";
    public static final String THEME_DARK = "Dark";
    public static final String LANG_VI = "vi_VN";
    public static final String LANG_EN = "en_US";

    public SettingService() {
        this.repository = new SettingRepository();
        this.currentUser = AuthManager.getCurUser();
        loadSettings();
    }

    /**
     * Tải cài đặt của người dùng vào bộ nhớ cache
     */
    private void loadSettings() {
        if (currentUser != null) {
            this.userSettingsCache = repository.loadSettingsForUser(currentUser.getId());
        } else {
            this.userSettingsCache = new HashMap<>();
        }
    }

    /**
     * Lấy cài đặt Theme hiện tại
     */
    public String getTheme() {
        return userSettingsCache.getOrDefault(KEY_THEME, THEME_LIGHT);
    }

    /**
     * Lưu cài đặt Theme mới
     */
    public void saveTheme(String theme) {
        if (theme == null || currentUser == null) return;

        userSettingsCache.put(KEY_THEME, theme);

        repository.saveSettingForUser(currentUser.getId(), KEY_THEME, theme);
    }

    /**
     * Lấy cài đặt Ngôn ngữ hiện tại
     */
    public String getLanguage() {
        return userSettingsCache.getOrDefault(KEY_LANGUAGE, LANG_VI);
    }

    /**
     * Lưu cài đặt Ngôn ngữ mới
     */
    public void saveLanguage(String language) {
        if (language == null || currentUser == null) return;

        userSettingsCache.put(KEY_LANGUAGE, language);
        repository.saveSettingForUser(currentUser.getId(), KEY_LANGUAGE, language);

    }
}