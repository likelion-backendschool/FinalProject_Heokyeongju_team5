package com.mutbook.week4_mission.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mutbook.week4_mission.app.domain.member.MemberResponse;
import com.mutbook.week4_mission.app.domain.member.entity.Member;
import com.mutbook.week4_mission.app.domain.myBook.MyBooksResponse;
import com.mutbook.week4_mission.app.domain.myBook.entity.MyBook;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;


import javax.persistence.EntityManager;

import static org.modelmapper.convention.MatchingStrategies.STRICT;

@Configuration
public class AppConfig {
    @Getter
    private static ApplicationContext context;
    private static String activeProfile;
    @Getter
    private static String siteName;
    @Getter
    private static String siteBaseUrl;

    @Autowired
    public void setContext(ApplicationContext context) {
        AppConfig.context = context;
    }

    @Value("${spring.profiles.active:}")
    public void setActiveProfile(String value) {
        activeProfile = value;
    }

    @Value("${custom.site.name}")
    public void setSiteName(String siteName) {
        AppConfig.siteName = siteName;
    }

    @Value("${custom.site.baseUrl}")
    public void setSiteBaseUrl(String siteBaseUrl) {
        AppConfig.siteBaseUrl = siteBaseUrl;
    }

    public static boolean isNotProd() {
        return isProd() == false;
    }

    public static boolean isProd() {
        return activeProfile.equals("prod");
    }

    public static boolean isNotDev() {
        return isLocal() == false;
    }

    public static boolean isLocal() {
        return activeProfile.equals("local");
    }

    public static boolean isNotTest() {
        return isLocal() == false;
    }

    public static boolean isTest() {
        return activeProfile.equals("test");
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(STRICT).setSkipNullEnabled(true);
        mapper.typeMap(MyBook.class, MyBooksResponse.class).addMappings(m -> {
            m.map(source -> source.getId(), MyBooksResponse::setId);
            m.map(source -> source.getCreateDate(), MyBooksResponse::setCreateDate);
            m.map(source -> source.getModifyDate(), MyBooksResponse::setModifyDate);
            m.map(source -> source.getOwner(), MyBooksResponse::setOwner); // 여기에서 안됨
            m.map(source -> source.getProduct(), MyBooksResponse::setProduct);
        });
        mapper.typeMap(Member.class, MemberResponse.class).addMappings(m -> {
            m.map(source -> source.getId(), MemberResponse::setId);
            m.map(source -> source.getCreateDate(), MemberResponse::setCreateDate);
            m.map(source -> source.getModifyDate(), MemberResponse::setModifyDate);
            m.map(source -> source.getUsername(), MemberResponse::setUsername);
            m.map(source -> source.getNickname(), MemberResponse::setNickname);
            m.map(source -> source.getEmail(), MemberResponse::setEmail);
        });
        return mapper;
    }
}