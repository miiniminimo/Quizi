package com.quizi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();

    static {
        // 클래스 로드 시 config.properties 파일을 읽어옴
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.err.println("[ConfigManager] 설정 파일을 로드하는 중 오류가 발생했습니다.");
            ex.printStackTrace();
        }
    }

    /**
     * 키 값을 가져오는 메서드
     * 1. 시스템 환경 변수(Environment Variable)를 먼저 확인 (배포 환경용)
     * 2. 없으면 config.properties 파일 확인 (로컬 개발용)
     */
    public static String getProperty(String key) {
        // 환경 변수는 보통 대문자와 언더바를 사용함 (예: openai.api.key -> OPENAI_API_KEY)
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);

        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        return properties.getProperty(key);
    }
}