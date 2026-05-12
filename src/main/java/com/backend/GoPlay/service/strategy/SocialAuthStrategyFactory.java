package com.backend.GoPlay.service.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SocialAuthStrategyFactory {

    private final Map<String, SocialAuthStrategy> strategies;

    public SocialAuthStrategyFactory(List<SocialAuthStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(s -> s.getProviderName().toUpperCase(), Function.identity()));
    }

    public SocialAuthStrategy getStrategy(String provider) {
        SocialAuthStrategy strategy = strategies.get(provider.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Nhà cung cấp không được hỗ trợ: " + provider);
        }
        return strategy;
    }
}
