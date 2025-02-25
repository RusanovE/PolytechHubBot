package org.example.polytechhubbot.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*

@Configuration
class LocaleConfig {

    @Bean
    fun localeResolver(): LocaleResolver {
        val localeResolver = AcceptHeaderLocaleResolver()
        localeResolver.setDefaultLocale(Locale.of("en"))
        return localeResolver
    }

    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")
        messageSource.setCacheSeconds(500)
        return messageSource
    }
}
