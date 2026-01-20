package masil.backend.modules.chat.enums;

import lombok.Getter;

@Getter
public enum MessageLanguage {
    KOREAN("ko", "한국어"),
    JAPANESE("ja", "日本語");
    
    private final String code;
    private final String displayName;
    
    MessageLanguage(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
