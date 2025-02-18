package com.mutbook.week4_mission.app.domain.member.service;

import com.mutbook.week4_mission.app.base.dto.RsData;
import com.mutbook.week4_mission.app.domain.cash.entity.CashLog;
import com.mutbook.week4_mission.app.domain.cash.service.CashService;
import com.mutbook.week4_mission.app.domain.mail.service.EmailService;
import com.mutbook.week4_mission.app.domain.member.entity.AuthLevel;
import com.mutbook.week4_mission.app.domain.member.entity.Member;
import com.mutbook.week4_mission.app.base.exception.AlreadyExistException;
import com.mutbook.week4_mission.app.domain.member.repository.MemberRepository;
import com.mutbook.week4_mission.app.security.dto.MemberContext;
import com.mutbook.week4_mission.app.security.jwt.JwtProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CashService cashService;
    private final JwtProvider jwtProvider;



    public Optional<Member> findByEmail(String email){
            return memberRepository.findByEmail(email);
    }

    public Member join(String username, String password, String email, String nickname){
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new AlreadyExistException();
        }


        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .authLevel(AuthLevel.NORMAL)
                .build();

        member.genAuthorities();
        memberRepository.save(member);
        //emailService.sendJoinMail(member);
        return member;
    }



    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }


    @Transactional
    public RsData beAuthor(Member member, String nickname) {
        Optional<Member> opMember = memberRepository.findByNickname(nickname);

        if (opMember.isPresent()) {
            return RsData.of("F-1", "해당 필명은 이미 사용중입니다.");
        }

        opMember = memberRepository.findById(member.getId());

        opMember.get().setNickname(nickname);
        forceAuthentication(opMember.get());

        return RsData.of("S-1", "해당 필명으로 활동을 시작합니다.");
    }
    private void forceAuthentication(Member member) {
        MemberContext memberContext = new MemberContext(member, member.genAuthorities());

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        memberContext,
                        member.getPassword(),
                        memberContext.getAuthorities()
                );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public Long getCash(Member member) {
        Member foundMember = findByUsername(member.getUsername()).get();
        return foundMember.getCash();
    }

    @Transactional
    public RsData<AddCashRsDataBody> addCash(Member member, long price, String eventType) {
        CashLog cashLog = cashService.addCash(member, price, eventType);

        long newRestCash = member.getCash() + cashLog.getPrice();
        member.setCash(newRestCash);
        memberRepository.save(member);

        return RsData.of(
                "S-1",
                "성공",
                new AddCashRsDataBody(cashLog, newRestCash)
        );
    }

    @Transactional
    public String genAccessToken(Member member) {
        String accessToken = member.getAccessToken();

        if (StringUtils.hasLength(accessToken) == false) {
            accessToken = jwtProvider.generateAccessToken(member.getAccessTokenClaims(), 60L * 60 * 24 * 365 * 100);
            member.setAccessToken(accessToken);
        }

        return accessToken;
    }

    public boolean verifyWithWhiteList(Member member, String token) {
        return member.getAccessToken().equals(token);
    }
    @Data
    @AllArgsConstructor
    public static class AddCashRsDataBody {
        CashLog cashLog;
        long newRestCash;
    }
}
